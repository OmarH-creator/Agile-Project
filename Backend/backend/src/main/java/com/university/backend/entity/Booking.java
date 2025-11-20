package com.university.backend.entity;

import java.time.LocalDateTime;
public class Booking {
    private LocalDateTime start;
    private LocalDateTime end;
    private String purpose;

    public Booking(LocalDateTime start, LocalDateTime end, String purpose) {
        this.start = start;
        this.end = end;
        this.purpose = purpose;
    }

    public boolean conflictsWith(Booking other) {
        return start.isBefore(other.end) && end.isAfter(other.start);
    }

    public LocalDateTime getStart() { return start; }
    public LocalDateTime getEnd() { return end; }
    public String getPurpose() { return purpose; }
}
