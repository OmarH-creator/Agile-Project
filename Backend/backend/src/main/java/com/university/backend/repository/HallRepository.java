package com.university.backend.repository;

import com.university.backend.entity.Hall;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional; // Import needed for deletion

public interface HallRepository extends JpaRepository<Hall, Long> {
    // Find hall by its name (e.g., "Main Auditorium")
    Optional<Hall> findByHallName(String hallName);
    
 // --- NEW: Deletion Method ---
    @Transactional // Required for modifying operations
    void deleteByHallName(String hallName);
}