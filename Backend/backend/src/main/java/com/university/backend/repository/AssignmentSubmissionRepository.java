package com.university.backend.repository;

import com.university.backend.entity.AssignmentSubmissions.AssignmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {

    /**
     * Fetches the Submission, joins the Student, joins the Values, and joins the Attribute definitions
     * in a single optimized SQL query.
     */
    @Query("SELECT s FROM AssignmentSubmission s " +
            "LEFT JOIN FETCH s.student st " +
            "LEFT JOIN FETCH s.values v " +
            "LEFT JOIN FETCH v.attribute " +
            "WHERE s.submissionId = :id")
    Optional<AssignmentSubmission> findFullSubmissionById(@Param("id") Long id);
}