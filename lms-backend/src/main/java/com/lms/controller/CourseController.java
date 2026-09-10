package com.lms.controller;

import com.lms.dto.CourseRequest;
import com.lms.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    // Public — anyone can browse the catalog
    @GetMapping
    public ResponseEntity<?> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    // Public, but videoUrl is gated based on enrollment/ownership
    @GetMapping("/{id}")
    public ResponseEntity<?> getCourse(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(courseService.getCourseForViewer(id, auth));
    }

    // Admin only
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createCourse(@RequestBody CourseRequest req, Authentication auth) {
        return ResponseEntity.ok(courseService.createCourse(req, auth.getName()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateCourse(@PathVariable Long id, @RequestBody CourseRequest req) {
        return ResponseEntity.ok(courseService.updateCourse(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok("Deleted successfully");
    }
}
