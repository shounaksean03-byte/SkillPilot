package com.lms.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CourseRequest {
    private String title;
    private String description;
    private Double price;
    private String videoUrl;
    private String thumbnailUrl;
}
