package com.university.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.university.backend.entity.Student;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional; // Import needed for deletion
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

// T is Student, ID is String (Assuming studentId is the PK or you rely on findByStudentId)
public interface StudentRepository extends JpaRepository<Student, String> {

    List<Student> findByStudentIdIn(List<String> studentIds);
    // --- ESSENTIAL FOR STUDENT CONTROLLER ---

    // 1. Login/Profile lookup by ID
    Optional<Student> findByStudentId(String studentId);

    // 2. Login/Profile lookup by Email
    Optional<Student> findByEmail(String email);

    // --- VALIDATION CHECKS ---

    // Check if student exists (Used during creation)
    boolean existsByStudentId(String studentId);

    // Check for unique phone numbers
    Optional<Student> findByPhone(String phone);

    // --- SEARCH & PAGINATION (For Admin View) ---
    Page<Student> findByStudentIdStartingWith(String prefix, Pageable pageable);

    // --- MAINTENANCE ---
    @Transactional
    void deleteByStudentId(String studentId);
 //FOR Parent View
    List<Student> findByStudentIdIn(List<String> studentIds);
}