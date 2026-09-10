package com.lms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "courses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    private Double price;

    private String videoUrl; // link/path to uploaded video

    private String thumbnailUrl;

    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = false)
    private User createdBy;

    private LocalDateTime createdAt = LocalDateTime.now();
}
