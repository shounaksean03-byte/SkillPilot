package com.lms.service;

import com.lms.dto.CourseRequest;
import com.lms.dto.CourseResponse;
import com.lms.entity.Course;
import com.lms.entity.User;
import com.lms.repository.CourseRepository;
import com.lms.repository.EnrollmentRepository;
import com.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    public CourseResponse createCourse(CourseRequest req, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
            .orElseThrow(() -> new RuntimeException("Admin not found"));

        Course course = new Course();
        course.setTitle(req.getTitle());
        course.setDescription(req.getDescription());
        course.setPrice(req.getPrice());
        course.setVideoUrl(req.getVideoUrl());
        course.setThumbnailUrl(req.getThumbnailUrl());
        course.setCreatedBy(admin);

        Course saved = courseRepository.save(course);
        return toCourseResponse(saved, true);
    }

    // Catalog listing: videoUrl is always omitted here (only the single-course
    // detail view exposes it, subject to enrollment/ownership gating).
    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll().stream()
            .map(this::toCourseResponse)
            .toList();
    }

    private CourseResponse toCourseResponse(Course course) {
        return toCourseResponse(course, false);
    }

    private CourseResponse toCourseResponse(Course course, boolean includeVideoUrl) {
        CourseResponse response = new CourseResponse();
        response.setId(course.getId());
        response.setTitle(course.getTitle());
        response.setDescription(course.getDescription());
        response.setPrice(course.getPrice());
        response.setThumbnailUrl(course.getThumbnailUrl());
        response.setVideoUrl(includeVideoUrl ? course.getVideoUrl() : null);
        return response;
    }

    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Course not found"));
    }

    public CourseResponse updateCourse(Long id, CourseRequest req) {
        Course course = getCourseById(id);
        course.setTitle(req.getTitle());
        course.setDescription(req.getDescription());
        course.setPrice(req.getPrice());
        course.setVideoUrl(req.getVideoUrl());
        course.setThumbnailUrl(req.getThumbnailUrl());
        Course saved = courseRepository.save(course);
        return toCourseResponse(saved, true);
    }

    public void deleteCourse(Long id) {
        courseRepository.deleteById(id);
    }

    // Access-controlled single-course view: hides videoUrl unless
    // the requester is enrolled or is the admin who created the course.
    public CourseResponse getCourseForViewer(Long courseId, Authentication auth) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found"));

        boolean hasAccess = false;

        if (auth != null && auth.isAuthenticated()) {
            User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

            boolean isCreatorAdmin = course.getCreatedBy().getId().equals(user.getId());
            boolean isEnrolledStudent = enrollmentRepository.existsByStudentAndCourse(user, course);

            hasAccess = isCreatorAdmin || isEnrolledStudent;
        }

        CourseResponse response = new CourseResponse();
        response.setId(course.getId());
        response.setTitle(course.getTitle());
        response.setDescription(course.getDescription());
        response.setPrice(course.getPrice());
        response.setThumbnailUrl(course.getThumbnailUrl());
        response.setVideoUrl(hasAccess ? course.getVideoUrl() : null);

        return response;
    }
}
