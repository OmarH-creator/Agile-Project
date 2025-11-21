package com.university.backend.controllers;

import com.university.backend.entity.Hall;
import com.university.backend.entity.Booking; // Import the new Booking Entity
import com.university.backend.repository.HallRepository;
import com.university.backend.repository.ProfessorRepository;
import com.university.backend.repository.BookingRepository; // Import the new Booking Repository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Optional;

@RestController
@RequestMapping("/api/professor")
public class ProfessorController {

    @Autowired
    private HallRepository hallRepository;
    
    @Autowired
    private ProfessorRepository professorRepository;
    
    // --- NEW: Inject BookingRepository ---
    @Autowired
    private BookingRepository bookingRepository; 

    /**
     * Endpoint for a professor to book a hall for a specific time slot.
     * Maps to: POST /api/professor/halls/book
     */
    @PostMapping("/halls/book")
    public ResponseEntity<String> bookHallByProfessor(@RequestBody ProfessorBookingRequest request) {

        // 1. Validate Professor Existence
        if (!professorRepository.existsByProfessorId(request.getProfessorId())) {
            return ResponseEntity.status(401).body("Error: Professor with ID " + request.getProfessorId() + " not authorized or not found.");
        }

        // 2. Find Hall using HallRepository
        Optional<Hall> hallOpt = hallRepository.findByHallName(request.getHallName());

        if (hallOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Hall '" + request.getHallName() + "' not found.");
        }

        Hall hall = hallOpt.get();
        
        
        
        // 3. Conflict Check using Hall Entity's logic
        // This logic remains within the Hall entity, ensuring business rules are centralized.
        if (!hall.isAvailable(request.getStart(), request.getEnd())) {
            return ResponseEntity.badRequest().body("Booking failed: Time conflict or hall is unavailable.");
        }

        // 4. Create and Save the NEW Booking Entity
        
        // Create the new booking object with the Hall relationship set
        Booking newBooking = new Booking(
            request.getStart(), 
            request.getEnd(), 
            request.getPurpose(), 
            request.getReservationId(), 
            request.getProfessorId(), 
            hall // Link the Booking to the Hall object
        );
        
        // Add the new booking to the hall's list and persist the booking directly.
        // We still need to use the Hall entity's list management to ensure
        // the list is updated in memory for subsequent operations (though
        // we could bypass this if we only saved the Booking entity).
        // Let's rely on the save method in the repository for now.
        
        // Persist the Booking record using the BookingRepository
        bookingRepository.save(newBooking);

        // Update the Hall's in-memory list and save the Hall to ensure the @OneToMany relationship
        // is maintained correctly, although the primary persistence is the Booking entity itself.
        // We rely on the cascade/persistence context, but explicitly saving the Hall is safest 
        // if the list update logic is complex. For simplicity, let's keep the Hall entity 
        // clean and only rely on the BookingRepository.
        
        return ResponseEntity.ok("Success: Hall '" + hall.getHallName() + 
                                 "' booked successfully by Professor " + request.getProfessorId() + 
                                 ". Reservation ID: " + newBooking.getReservationId());
    }

    // Helper class for the booking JSON body (specific to Professor Controller)
    public static class ProfessorBookingRequest {
        private String professorId;
        private String hallName;
        private Date start;
        private Date end;
        private String purpose;
        private long reservationId;

        // Getters and Setters (omitted for brevity)
        public String getProfessorId() { return professorId; }
        public void setProfessorId(String professorId) { this.professorId = professorId; }
        public String getHallName() { return hallName; }
        public void setHallName(String hallName) { this.hallName = hallName; }
        public Date getStart() { return start; }
        public void setStart(Date start) { this.start = start; }
        public Date getEnd() { return end; }
        public void setEnd(Date end) { this.end = end; }
        public String getPurpose() { return purpose; }
        public void setPurpose(String purpose) { this.purpose = purpose; }
        public long getReservationId() { return reservationId; }
        public void setReservationId(long reservationId) { this.reservationId = reservationId; }
    }
}