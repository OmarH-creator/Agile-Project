package com.university.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "halls")
public class Hall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String hallName;

    private int capacity;

    @ElementCollection
    @CollectionTable(name = "hall_bookings",
            joinColumns = @JoinColumn(name = "hall_id"))
    private List<Booking> bookings = new ArrayList<>();

    // Default constructor
    public Hall() {
    }

    public Hall(String hallName, int capacity) {
        this.hallName = hallName;
        this.capacity = capacity;
    }

    public boolean book(LocalDateTime start, LocalDateTime end, String purpose) {
        Booking newBooking = new Booking(start, end, purpose);

        // Check for conflict with existing bookings
        for (Booking b : bookings) {
            if (b.conflictsWith(newBooking)) {
                return false; // Overlapping
            }
        }

        bookings.add(newBooking);
        return true;
    }

    public boolean isAvailable(LocalDateTime start, LocalDateTime end) {
        Booking temp = new Booking(start, end, "check");

        for (Booking b : bookings) {
            if (b.conflictsWith(temp)) {
                return false;
            }
        }
        return true;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getHallName() { return hallName; }
    public void setHallName(String hallName) { this.hallName = hallName; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setBookings(List<Booking> bookings) { this.bookings = bookings; }
}