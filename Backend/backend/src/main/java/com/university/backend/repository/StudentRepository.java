package com.university.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.university.backend.entity.Student;

// T is Student, ID is String
public interface StudentRepository extends JpaRepository<Student, String> {
    // You can add custom queries here if needed, e.g.:
    // Optional<Student> findByEmail(String email);
}
