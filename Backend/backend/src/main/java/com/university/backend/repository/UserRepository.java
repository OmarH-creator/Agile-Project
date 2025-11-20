package com.university.backend.repository;

import com.university.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Find user by email (useful for login)
    Optional<User> findByEmail(String email);
    
    // Check if email exists (useful for registration)
    boolean existsByEmail(String email);
}