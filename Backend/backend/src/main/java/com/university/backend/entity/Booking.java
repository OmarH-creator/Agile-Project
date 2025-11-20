package com.university.backend.entity;

import jakarta.persistence.Embeddable;
import java.time.LocalDateTime;

@Embeddable
public class Booking {
    
    private LocalDateTime start;
    private LocalDateTime end;
    private String purpose;

    // Default constructor (Required by JPA)
    public Booking() {
    }

    public Booking(LocalDateTime start, LocalDateTime end, String purpose) {
        this.start = start;
        this.end = end;
        this.purpose = purpose;
    }

    // Logic preserved exactly as requested
    public boolean conflictsWith(Booking other) {
        return start.isBefore(other.end) && end.isAfter(other.start);
    }

    public LocalDateTime getStart() { return start; }
    public void setStart(LocalDateTime start) { this.start = start; } // Setter needed for JPA mapping

    public LocalDateTime getEnd() { return end; }
    public void setEnd(LocalDateTime end) { this.end = end; } // Setter needed for JPA mapping

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; } // Setter needed for JPA mapping
}