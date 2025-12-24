package com.university.backend.repository;

import com.university.backend.entity.Course_record;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface CourseRecordRepository extends JpaRepository<Course_record, Long> {

    // Find all course records for a specific student
    List<Course_record> findByStudentStudentId(String studentId);

    // Find course records for multiple students
    List<Course_record> findByStudentStudentIdIn(List<String> studentIds);

    // Find by course name (optional)
    List<Course_record> findByCourseNameContainingIgnoreCase(String courseName);

    // Find by semester (optional)
    List<Course_record> findBySemester(String semester);

    // Custom JPQL query
    @Query("SELECT cr FROM Course_record cr WHERE cr.student.studentId = :studentId")
    List<Course_record> findCoursesByStudentId(@Param("studentId") String studentId);

    // For multiple students
    @Query("SELECT cr FROM Course_record cr WHERE cr.student.studentId IN :studentIds")
    List<Course_record> findCoursesByStudentIds(@Param("studentIds") List<String> studentIds);
}