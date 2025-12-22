package com.university.backend.repository;

import com.university.backend.entity.AssignmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {
    Optional<AssignmentSubmission> findByAssignmentIdAndStudentId(Long assignmentId, String studentId);
    List<AssignmentSubmission> findByAssignmentId(Long assignmentId);
}