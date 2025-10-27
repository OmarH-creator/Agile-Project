package com.universitymanagement.service.facility;

import com.universitymanagement.model.facility.HallReservation;
import com.universitymanagement.model.eav.EntityRecord;
import com.universitymanagement.model.user.Admin;
import com.universitymanagement.model.user.User;
import com.universitymanagement.repository.facility.HallReservationRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationService {

    private final HallReservationRepository reservationRepository;

    public ReservationService(HallReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public HallReservation requestReservation(EntityRecord hall, User faculty, LocalDateTime start, LocalDateTime end) {
        HallReservation reservation = new HallReservation(hall, faculty, start, end);
        return reservationRepository.save(reservation);
    }

    public HallReservation approveReservation(Long reservationId, Admin admin) {
        HallReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        reservation.setStatus("APPROVED");
        reservation.setApprovedBy(admin);
        return reservationRepository.save(reservation);
    }

    public HallReservation rejectReservation(Long reservationId, Admin admin) {
        HallReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        reservation.setStatus("REJECTED");
        reservation.setApprovedBy(admin);
        return reservationRepository.save(reservation);
    }

    public List<HallReservation> getPendingRequests() {
        return reservationRepository.findByStatus("PENDING");
    }
}
