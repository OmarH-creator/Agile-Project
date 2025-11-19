package com.university.backend.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Hall {

    private String hallName;
    private List<Booking> bookings;   // List of time ranges

    public Hall(String hallName, int capacity) {
        this.hallName = hallName;
        this.bookings = new ArrayList<>();
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

    public String getHallName() {
        return hallName;
    }
}
