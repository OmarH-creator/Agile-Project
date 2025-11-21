package com.university.backend.repository;

import com.university.backend.entity.Professor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProfessorRepository extends JpaRepository<Professor, String> {
    // Find by the business ID (e.g., "P-101")
    Optional<Professor> findByProfessorId(String professorId);
    
    boolean existsByProfessorId(String professorId);
}