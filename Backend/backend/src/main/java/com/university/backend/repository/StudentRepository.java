package com.university.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.university.backend.entity.Student;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional; // Import needed for deletion

// T is Student, ID is String
public interface StudentRepository extends JpaRepository<Student, Long> {
// These methods MUST be defined here for the Controller to see them

    // Custom finder to search by business key (studentId)
    Optional<Student> findByStudentId(String studentId);

    // Check if student exists by business key
    boolean existsByStudentId(String studentId);

    // Find by email
    Optional<Student> findByEmail(String email);

    // --- NEW: Deletion Method ---
    @Transactional // Required for modifying operations
    void deleteByStudentId(String studentId);
}