package com.university.backend.repository;

import com.university.backend.entity.CourseGradingItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseGradingItemRepository extends JpaRepository<CourseGradingItem, Long> {

    // Fetch all grading buckets for a specific course (e.g., "CSE111")
    // Note: 'CourseCode' matches the field name in your Course entity
    List<CourseGradingItem> findByCourse_CourseCode(String courseCode);
}