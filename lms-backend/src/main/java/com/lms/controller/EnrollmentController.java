package com.lms.controller;

import com.lms.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/{courseId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> enroll(@PathVariable Long courseId, Authentication auth) {
        return ResponseEntity.ok(enrollmentService.enroll(auth.getName(), courseId));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> myEnrollments(Authentication auth) {
        return ResponseEntity.ok(enrollmentService.getMyEnrollments(auth.getName()));
    }
}
