package com.universitymanagement.repository.facility;

import com.universitymanagement.model.facility.HallReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HallReservationRepository extends JpaRepository<HallReservation, Long> {
    List<HallReservation> findByStatus(String status);
}
