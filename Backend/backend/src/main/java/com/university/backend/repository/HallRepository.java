package com.university.backend.repository;

import com.university.backend.entity.Hall;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface HallRepository extends JpaRepository<Hall, Long> {
    // Find hall by its name (e.g., "Main Auditorium")
    Optional<Hall> findByHallName(String hallName);
}