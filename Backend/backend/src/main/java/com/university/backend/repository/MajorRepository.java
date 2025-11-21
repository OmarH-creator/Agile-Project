package com.university.backend.repository;

import com.university.backend.entity.Major;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

// T is Major, ID is String (because majorId is the @Id)
public interface MajorRepository extends JpaRepository<Major, String> {

    // --- Finder Methods ---

    // Since majorId is the Primary Key, findById() works, but we can keep
    // this naming convention for consistency with your other repos.
    Optional<Major> findByMajorId(String majorId);

    // Find by the unique major name (e.g., "Computer Science")
    Optional<Major> findByMajorName(String majorName);

    // --- Existence Checks ---

    boolean existsByMajorId(String majorId);

    boolean existsByMajorName(String majorName);

    // --- Deletion Method ---

    @Transactional // Required for modifying operations (delete/update)
    void deleteByMajorId(String majorId);

    @Transactional
    void deleteByMajorName(String majorName);
}