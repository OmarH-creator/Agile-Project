package com.university.backend.repository;

import com.university.backend.entity.AssignmentAttributes;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AssignmentMetaKeyRepository extends JpaRepository<AssignmentAttributes, Long> {
    Optional<AssignmentAttributes> findByKeyName(String keyName);
}