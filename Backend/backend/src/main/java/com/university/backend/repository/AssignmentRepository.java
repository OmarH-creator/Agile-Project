package com.university.backend.repository;

import com.university.backend.entity.Assignment.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    /**
     * Fetches the Assignment, joins the Values, and joins the Attribute definitions
     * in a single optimized SQL query.
     */
    @Query("SELECT a FROM Assignment a " +
            "LEFT JOIN FETCH a.values v " +
            "LEFT JOIN FETCH v.attribute " +
            "WHERE a.assignmentId = :id")
    Optional<Assignment> findFullAssignmentById(@Param("id") Long id);

    // Inside AssignmentRepository interface
    @Query("SELECT a FROM Assignment a WHERE a.course.courseCode = :courseCode")
    List<Assignment> findAllByCourseCode(@Param("courseCode") String courseCode);

}


