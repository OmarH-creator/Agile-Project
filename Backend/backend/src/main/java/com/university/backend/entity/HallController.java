package com.universitymanagement.controller.facility;

import com.universitymanagement.model.eav.EntityRecord;
import com.universitymanagement.model.facility.HallReservation;
import com.universitymanagement.model.user.Admin;
import com.universitymanagement.model.user.User;
import com.universitymanagement.service.facility.HallService;
import com.universitymanagement.service.facility.ReservationService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/halls")
public class HallController {

    private final HallService hallService;
    private final ReservationService reservationService;

    public HallController(HallService hallService, ReservationService reservationService) {
        this.hallService = hallService;
        this.reservationService = reservationService;
    }

    // Add new hall
    @PostMapping("/add")
    public EntityRecord addHall(@RequestParam String name,
                                @RequestParam String location,
                                @RequestParam int capacity,
                                @RequestParam String type) {
        return hallService.addHall(name, location, capacity, type);
    }

    // Delete hall
    @DeleteMapping("/{id}")
    public String deleteHall(@PathVariable Long id) {
        hallService.deleteHall(id);
        return "Hall deleted successfully.";
    }

    // List halls
    @GetMapping
    public List<EntityRecord> listHalls() {
        return hallService.listHalls();
    }

    // Approve or reject reservation
    @PutMapping("/approve/{id}")
    public HallReservation approve(@PathVariable Long id, @RequestBody Admin admin) {
        return reservationService.approveReservation(id, admin);
    }

    @PutMapping("/reject/{id}")
    public HallReservation reject(@PathVariable Long id, @RequestBody Admin admin) {
        return reservationService.rejectReservation(id, admin);
    }

    @GetMapping("/pending")
    public List<HallReservation> getPending() {
        return reservationService.getPendingRequests();
    }
}
