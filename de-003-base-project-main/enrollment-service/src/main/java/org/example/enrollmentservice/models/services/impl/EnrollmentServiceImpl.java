package org.example.enrollmentservice.models.services.impl;

import lombok.RequiredArgsConstructor;
import org.example.enrollmentservice.models.constants.EnrollmentStatus;
import org.example.enrollmentservice.exceptions.DuplicateCourseException;
import org.example.enrollmentservice.models.dto.requests.CreateEnrollmentDetailRequest;
import org.example.enrollmentservice.models.dto.requests.CreateEnrollmentRequest;
import org.example.enrollmentservice.models.dto.responses.EnrollmentDetailResponse;
import org.example.enrollmentservice.models.dto.responses.EnrollmentResponse;
import org.example.enrollmentservice.models.dto.responses.CourseResponse;
import org.example.enrollmentservice.models.entities.Enrollment;
import org.example.enrollmentservice.models.entities.EnrollmentDetail;
import org.example.enrollmentservice.models.repositories.EnrollmentDetailRepository;
import org.example.enrollmentservice.models.repositories.EnrollmentRepository;
import org.example.enrollmentservice.models.services.EnrollmentService;
import org.jspecify.annotations.NonNull;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

        private final EnrollmentRepository enrollmentRepository;
        private final EnrollmentDetailRepository enrollmentDetailRepository;
        private final CourseGatewayService courseGatewayService;

        private KafkaTemplate<String, String> kafkaTemplate;

        @Override
        @Transactional
        @CachePut(value = "enrollments", key = "'all'")
        public EnrollmentResponse createEnrollment(CreateEnrollmentRequest request) {
                if (request.items().stream().map(CreateEnrollmentDetailRequest::courseId).distinct().count()
                                != request.items().size()) {
                        throw new DuplicateCourseException();
                }

                List<ResolvedItem> resolvedItems = request.items().stream()
                                .map(this::resolveCourse)
                                .toList();

                double total = resolvedItems.stream()
                                .mapToDouble(item -> item.course().courseFee())
                                .sum();

                Enrollment savedEnrollment = enrollmentRepository.save(Enrollment.builder()
                                .studentName(request.studentName().trim())
                                .studentEmail(request.studentEmail().trim())
                                .totalFee(total)
                                .status(EnrollmentStatus.PENDING)
                                .build());

                List<EnrollmentDetail> details = resolvedItems.stream()
                                .map(item -> EnrollmentDetail.builder()
                                                .enrollment(savedEnrollment)
                                                .courseId(item.course().courseId())
                                                .courseFee(item.course().courseFee())
                                                .build())
                                .toList();

                List<EnrollmentDetail> savedDetails = enrollmentDetailRepository.saveAll(details);
                List<EnrollmentDetailResponse> itemResponses = getEnrollmentDetailResponses(savedDetails, resolvedItems);

                kafkaTemplate.send("enrollment-created", savedEnrollment.getStudentEmail());

                return new EnrollmentResponse(
                        savedEnrollment.getId(),
                        savedEnrollment.getStudentName(),
                        savedEnrollment.getStudentEmail(),
                        savedEnrollment.getTotalFee(),
                        savedEnrollment.getStatus(),
                                itemResponses);
        }

        private static @NonNull List<EnrollmentDetailResponse> getEnrollmentDetailResponses(List<EnrollmentDetail> savedDetails,
                        List<ResolvedItem> resolvedItems) {
                List<EnrollmentDetailResponse> itemResponses = new ArrayList<>(savedDetails.size());

                for (int index = 0; index < savedDetails.size(); index++) {
                        EnrollmentDetail detail = savedDetails.get(index);
                        CourseResponse course = resolvedItems.get(index).course();
                        itemResponses.add(new EnrollmentDetailResponse(
                                        detail.getId(),
                                        detail.getCourseId(),
                                        course.courseName(),
                                        detail.getCourseFee(),
                                        detail.getCourseFee()));
                }
                return itemResponses;
        }


        private ResolvedItem resolveCourse(CreateEnrollmentDetailRequest item) {
                CourseResponse course = courseGatewayService.getCourseById(item.courseId());
                return new ResolvedItem(item, course);
        }

        private record ResolvedItem(CreateEnrollmentDetailRequest request, CourseResponse course) {
        }
}
