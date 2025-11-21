package com.university.backend.repository;

import com.university.backend.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList; // Add this import
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, String> {

    // Custom finder to search by business key (courseCode)
    Optional<Course> findByCourseCode(String courseCode);

    // Check if course exists by business key
    boolean existsByCourseCode(String courseCode);

    // Find by course name
    Optional<Course> findByCourseName(String courseName);

    // Check if course exists by course name
    boolean existsByCourseName(String courseName);

    // NEW METHODS ADDED for viewing courses:
    List<Course> findByCourseNameContainingIgnoreCase(String courseName);
    List<Course> findByCreditHoursBetween(int minCredits, int maxCredits);
    List<Course> findByCreditHoursGreaterThanEqual(int minCredits);
    List<Course> findByCreditHoursLessThanEqual(int maxCredits);
}