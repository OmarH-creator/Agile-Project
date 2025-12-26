package com.university.backend.repository;

import com.university.backend.entity.Professor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional; // Import needed for deletion
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface ProfessorRepository extends JpaRepository<Professor, String> {
    // Find by the business ID (e.g., "P-101")
    Optional<Professor> findByProfessorId(String professorId);

    boolean existsByProfessorId(String professorId);

    @Transactional // Required for modifying operations
    void deleteByProfessorId(String professorId);

    Optional<Professor> findByProfessorCoursesContaining(String courseName);
    // --- ADD THIS METHOD ---
    // This enables searching by ID with pagination
    Page<Professor> findByProfessorIdStartingWith(String prefix, Pageable pageable);

    Optional<Professor> findByProfessorEmail(String professorEmail);
    // Optional: If you prefer searching by Name instead of ID, add this:
    // Page<Professor> findByProfessorNameContainingIgnoreCase(String name, Pageable pageable);
}