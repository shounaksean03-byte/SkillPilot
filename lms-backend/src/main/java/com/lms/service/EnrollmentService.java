package com.lms.service;

import com.lms.dto.CourseResponse;
import com.lms.dto.EnrollmentResponse;
import com.lms.entity.Course;
import com.lms.entity.Enrollment;
import com.lms.entity.User;
import com.lms.repository.CourseRepository;
import com.lms.repository.EnrollmentRepository;
import com.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public EnrollmentResponse enroll(String studentEmail, Long courseId) {
        User student = userRepository.findByEmail(studentEmail)
            .orElseThrow(() -> new RuntimeException("Student not found"));
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found"));

        if (enrollmentRepository.existsByStudentAndCourse(student, course)) {
            throw new RuntimeException("Already enrolled in this course");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        Enrollment saved = enrollmentRepository.save(enrollment);
        return toEnrollmentResponse(saved);
    }

    // videoUrl and the student object are deliberately omitted from the
    // response: the caller already knows who they are, and the list view
    // doesn't need video access (only the single-course detail view does).
    public List<EnrollmentResponse> getMyEnrollments(String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
            .orElseThrow(() -> new RuntimeException("Student not found"));
        return enrollmentRepository.findByStudent(student).stream()
            .map(this::toEnrollmentResponse)
            .toList();
    }

    private EnrollmentResponse toEnrollmentResponse(Enrollment enrollment) {
        Course course = enrollment.getCourse();
        CourseResponse courseResponse = new CourseResponse();
        courseResponse.setId(course.getId());
        courseResponse.setTitle(course.getTitle());
        courseResponse.setDescription(course.getDescription());
        courseResponse.setPrice(course.getPrice());
        courseResponse.setThumbnailUrl(course.getThumbnailUrl());
        courseResponse.setVideoUrl(null);

        EnrollmentResponse response = new EnrollmentResponse();
        response.setId(enrollment.getId());
        response.setEnrolledAt(enrollment.getEnrolledAt());
        response.setCourse(courseResponse);
        return response;
    }

    public boolean isEnrolled(String studentEmail, Long courseId) {
        User student = userRepository.findByEmail(studentEmail).orElseThrow();
        Course course = courseRepository.findById(courseId).orElseThrow();
        return enrollmentRepository.existsByStudentAndCourse(student, course);
    }
}
