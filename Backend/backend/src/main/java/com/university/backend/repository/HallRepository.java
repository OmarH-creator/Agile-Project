package com.university.backend.repository;

import com.university.backend.entity.Hall.Hall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HallRepository extends JpaRepository<Hall, Long> {

    /**
     * Fetches the Hall, joins the Values, and joins the Attribute definitions
     * in a single optimized SQL query.
     */
    @Query("SELECT h FROM Hall h " +
            "LEFT JOIN FETCH h.values v " +
            "LEFT JOIN FETCH v.attribute " +
            "WHERE h.hallId = :id")
    Optional<Hall> findFullHallById(@Param("id") Long id);

    // Static column lookup (More efficient than EAV lookup)
    Optional<Hall> findByHallName(String hallName);

    // Custom lookup to find a Hall by its "Name" attribute in the EAV tables
    @Query("SELECT h FROM Hall h JOIN h.values v JOIN v.attribute a WHERE a.attributeName = 'Name' AND v.valString = :name")
    Optional<Hall> findByName(@Param("name") String name);
}