package com.lms.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class EnrollmentResponse {
    private Long id;
    private LocalDateTime enrolledAt;
    private CourseResponse course;
}
