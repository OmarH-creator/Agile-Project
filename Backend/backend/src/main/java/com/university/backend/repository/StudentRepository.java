package com.university.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.university.backend.entity.Student;

import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional; // Import needed for deletion
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

// T is Student, ID is String
public interface StudentRepository extends JpaRepository<Student, String> {
// These methods MUST be defined here for the Controller to see them

    // Custom finder to search by business key (studentId)
    Optional<Student> findByStudentId(String studentId);

    // Check if student exists by business key
    boolean existsByStudentId(String studentId);

    // Find by email
    Optional<Student> findByEmail(String email);

    // ADDED THIS to check for unique phone numbers
    Optional<Student> findByPhone(String phone);

    Page<Student> findByStudentIdStartingWith(String prefix, Pageable pageable);
 // --- NEW: Deletion Method ---
    @Transactional // Required for modifying operations
    void deleteByStudentId(String studentId);

    // NEW: Find students where the 'currentCourses' collection contains the specific course name
    List<Student> findByCurrentCoursesContaining(String courseName);
}