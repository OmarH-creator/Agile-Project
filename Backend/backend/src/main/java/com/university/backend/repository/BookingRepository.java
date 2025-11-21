package com.university.backend.repository;

import com.university.backend.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {


    // Check if any booking exists for this hall that overlaps with the requested start/end
    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.hall.hallName = :hallName " +
            "AND ((b.startTime < :end) AND (b.endTime > :start))")
    boolean existsByHallAndOverlap(String hallName, Date start, Date end);

    // 2. For Updating Existing Bookings (Conflict Check Excluding Self)
    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.hall.hallName = :hallName " +
            "AND ((b.startTime < :end) AND (b.endTime > :start)) " +
            "AND b.reservationId <> :excludeId")
    boolean existsByHallAndOverlapExcludingId(String hallName, Date start, Date end, Long excludeId);
    // Find all bookings made by a specific staff member (e.g., a Professor)
    List<Booking> findByStaffId(String staffId);

    // Find a specific reservation by its unique ID
    Optional<Booking> findByReservationId(long reservationId);


    // Find all bookings within a specific time range
    List<Booking> findByStartTimeBetween(Date start, Date end);

    // Find all bookings for a specific purpose
    List<Booking> findByPurposeContainingIgnoreCase(String purpose);
}