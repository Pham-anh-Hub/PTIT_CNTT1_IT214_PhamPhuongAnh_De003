package org.example.enrollmentservice.models.dto.responses;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder
public record CourseResponse(
        @JsonAlias("id") Long courseId,
        String courseName,
        Double courseFee
) {
}
