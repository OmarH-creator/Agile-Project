package com.university.backend.repository;

import com.university.backend.entity.AssignmentSubmissions.AssignmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {

    /**
     * 1. HEAVY FETCH: Fetches Submission + Student + Values + Attributes in one query.
     * Used when viewing a single submission to avoid "Lazy Loading" errors.
     */
    @Query("SELECT s FROM AssignmentSubmission s " +
            "LEFT JOIN FETCH s.student st " +
            "LEFT JOIN FETCH s.values v " +
            "LEFT JOIN FETCH v.attribute " +
            "WHERE s.submissionId = :id")
    Optional<AssignmentSubmission> findFullSubmissionById(@Param("id") Long id);

    /**
     * 2. FILTER: Find all submissions for a specific Assignment (Professor View).
     * Returns a simple list (lazy loaded) which the Service then processes.
     */
    List<AssignmentSubmission> findByAssignment_Id(Long assignmentId);

    /**
     * 3. CHECK: Find if a specific Student has submitted a specific Assignment.
     * Used to prevent duplicate submissions or show "Already Submitted" status.
     */
    Optional<AssignmentSubmission> findByAssignment_IdAndStudent_StudentId(Long assignmentId, String studentId);

    /**
     * 4. GRADING ENGINE: Find all submissions for a specific grading bucket (e.g., "Labs").
     * Used by the GradingService to calculate weighted scores.
     */
    @Query("SELECT s FROM AssignmentSubmission s " +
            "WHERE s.student.studentId = :studentId " +
            "AND s.assignment.gradingItem.id = :gradingItemId")
    List<AssignmentSubmission> findByStudentAndGradingItem(
            @Param("studentId") String studentId,
            @Param("gradingItemId") Long gradingItemId
    );
}