package com.university.backend.repository;

import com.university.backend.entity.MajorReq;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MajorReqRepository extends JpaRepository<MajorReq, Long> {
    // Check if a course is allowed for a specific major
    boolean existsByMajor_MajorIdAndCourse_CourseCode(String majorId, String courseCode);

    // NEW: Find the MajorReq entry for a specific course
    Optional<MajorReq> findFirstByCourse_CourseCode(String courseCode);
}