package com.university.backend.repository;

import com.university.backend.entity.AssignmentSubmissions.SubmissionAttributes;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SubmissionMetaKeyRepository extends JpaRepository<SubmissionAttributes, Long> {
    Optional<SubmissionAttributes> findByKeyName(String keyName);
}