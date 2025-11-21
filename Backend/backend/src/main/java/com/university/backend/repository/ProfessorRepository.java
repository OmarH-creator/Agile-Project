package com.university.backend.repository;

import com.university.backend.entity.Professor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional; // Import needed for deletion

public interface ProfessorRepository extends JpaRepository<Professor, String> {
    // Find by the business ID (e.g., "P-101")
    Optional<Professor> findByProfessorId(String professorId);

    boolean existsByProfessorId(String professorId);

    @Transactional // Required for modifying operations
    void deleteByProfessorId(String professorId);
}