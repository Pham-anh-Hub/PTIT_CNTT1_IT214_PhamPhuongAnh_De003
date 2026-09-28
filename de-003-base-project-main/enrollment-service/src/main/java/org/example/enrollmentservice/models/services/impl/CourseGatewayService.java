package org.example.enrollmentservice.models.services.impl;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.example.enrollmentservice.clients.CourseClient;
import org.example.enrollmentservice.exceptions.CourseNotFoundException;
import org.example.enrollmentservice.exceptions.CourseServiceException;
import org.example.enrollmentservice.models.dto.responses.CourseResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourseGatewayService {

    private final CourseClient courseClient;

    @CircuitBreaker(name = "courseService", fallbackMethod = "getCourseFallback")
    @Cacheable(value = "courses", key = "#courseId")
    public CourseResponse getCourseById(Long courseId) {
        CourseResponse course = courseClient.getCourseById(courseId);

        if (course == null || course.courseId() == null || course.courseFee() == null) {
            throw new CourseServiceException(
                    "Course service returned invalid data for course " + courseId
            );
        }
        if (course.courseFee() < 0) {
            throw new CourseServiceException(
                    "Course fee must not be negative for course " + courseId
            );
        }

        return course;
    }

    private CourseResponse getCourseFallback(Long courseId, Throwable throwable) {
        if (throwable instanceof FeignException.NotFound) {
            throw new CourseNotFoundException(courseId);
        }
        if (throwable instanceof CourseNotFoundException courseNotFoundException) {
            throw courseNotFoundException;
        }
        if (throwable instanceof CourseServiceException courseServiceException) {
            throw courseServiceException;
        }

        throw new CourseServiceException(
                "Course service is unavailable for course " + courseId,
                throwable
        );

    }
}
