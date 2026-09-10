package com.lms.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CourseResponse {
    private Long id;
    private String title;
    private String description;
    private Double price;
    private String thumbnailUrl;
    private String videoUrl; // null unless authorized (enrolled student or creator admin)
}
