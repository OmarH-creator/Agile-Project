package com.university.backend.repository;

import com.university.backend.entity.StaffRequests.StaffRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaffRequestRepository extends JpaRepository<StaffRequest, Long> {

    /**
     * Fetches the Request, joins the Values, and joins the Attribute definitions
     * in a single optimized SQL query.
     */
    @Query("SELECT r FROM StaffRequest r " +
            "LEFT JOIN FETCH r.values v " +
            "LEFT JOIN FETCH v.attribute " +
            "WHERE r.requestId = :id")
    Optional<StaffRequest> findFullRequestById(@Param("id") Long id);
}