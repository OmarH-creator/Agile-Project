package com.university.backend.repository;

import com.university.backend.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Find all bookings made by a specific staff member (e.g., a Professor)
    List<Booking> findByStaffId(long staffId);

    // Find a specific reservation by its unique ID
    Optional<Booking> findByReservationId(long reservationId);

    // Find all bookings for a specific Hall (by Hall ID)
    // Spring Data JPA can traverse the 'hall' relationship
    List<Booking> findByHall_Id(Long hallId);
    
    // Find all bookings within a specific time range
    List<Booking> findByStartTimeBetween(Date start, Date end);

    // Find all bookings for a specific purpose
    List<Booking> findByPurposeContainingIgnoreCase(String purpose);
}