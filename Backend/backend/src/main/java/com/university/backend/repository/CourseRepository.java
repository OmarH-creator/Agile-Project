package com.university.backend.repository;

import com.university.backend.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

// ID is String (e.g., "CS101")
public interface CourseRepository extends JpaRepository<Course, String> {

    // --- ESSENTIAL FOR REGISTRATION ---
    // Note: 'findById' is provided by JpaRepository automatically.
    // We use it in the controller as: courseRepository.findById(courseId)

    // --- LOOKUPS ---

    // Custom finder if 'courseCode' is different from the @Id
    Optional<Course> findByCourseCode(String courseCode);

    // Check if course exists
    boolean existsByCourseCode(String courseCode);

    // Find by exact Name
    Optional<Course> findByCourseName(String courseName);

    boolean existsByCourseName(String courseName);

    // --- ADVANCED SEARCH (For Course Catalog Filtering) ---

    // Search by name (case insensitive, e.g., "intro")
    List<Course> findByCourseNameContainingIgnoreCase(String courseName);

    // Filter by Credits (e.g., 3-4 credits)
    List<Course> findByCreditHoursBetween(int minCredits, int maxCredits);

    List<Course> findByCreditHoursGreaterThanEqual(int minCredits);

    List<Course> findByCreditHoursLessThanEqual(int maxCredits);
}