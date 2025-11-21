package com.university.backend.entity;

import jakarta.persistence.*;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "halls")
public class Hall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 255)
    private String hallName;

    private int capacity;

    // CHANGED: Use @OneToMany to manage the collection of Booking entities
    @OneToMany(mappedBy = "hall", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookings = new ArrayList<>();

    // Default constructor
    public Hall() {
    }

    public Hall(String hallName, int capacity) {
        this.hallName = hallName;
        this.capacity = capacity;
    }

    /**
     * Attempts to book the hall. If successful, it creates the new Booking entity
     * and adds the reference to this Hall.
     */
    public boolean book(Date start, Date end, String purpose, long reservationId, long staffId) {
        // Create a temporary booking object for conflict checking
        Booking newBooking = new Booking(start, end, purpose, reservationId, staffId, this);

        // Check for conflict with existing bookings
        for (Booking b : bookings) {
            if (b.conflictsWith(newBooking)) {
                return false; // Overlapping
            }
        }

        // Set the bidirectional relationship and add to the list
        newBooking.setHall(this);
        bookings.add(newBooking);
        return true;
    }

    /**
     * Checks availability without creating a new booking.
     * Note: This method now only needs the dates for conflict checking.
     */
    public boolean isAvailable(Date start, Date end) {
        // Since we only need dates for checking, we can use a dummy Booking object
        // for the conflictsWith logic.
        Booking temp = new Booking();
        temp.setStartTime(start);
        temp.setEndTime(end);

        for (Booking b : bookings) {
            if (b.conflictsWith(temp)) {
                return false;
            }
        }
        return true;
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getHallName() { return hallName; }
    public void setHallName(String hallName) { this.hallName = hallName; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public List<Booking> getBookings() { return bookings; }
    public void setBookings(List<Booking> bookings) { this.bookings = bookings; }
}