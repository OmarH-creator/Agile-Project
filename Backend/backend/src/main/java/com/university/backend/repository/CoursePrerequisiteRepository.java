package com.university.backend.repository;

import com.university.backend.entity.CoursePrerequisite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CoursePrerequisiteRepository extends JpaRepository<CoursePrerequisite, Long> {
    // Get all prerequisites for a specific course
    List<CoursePrerequisite> findByCourse_CourseCode(String courseCode);
    
}