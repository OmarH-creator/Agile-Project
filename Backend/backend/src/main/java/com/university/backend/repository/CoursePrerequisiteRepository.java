package com.university.backend.repository;

import com.university.backend.entity.CoursePrerequisite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@org.springframework.transaction.annotation.Transactional
public interface CoursePrerequisiteRepository extends JpaRepository<CoursePrerequisite, Long> {
    List<CoursePrerequisite> findByCourse(com.university.backend.entity.Course course);
    void deleteByCourse(com.university.backend.entity.Course course);
    void deleteByPrerequisite(com.university.backend.entity.Course prerequisite);
    List<CoursePrerequisite> findByCourse_CourseCode(String courseCode);
}
