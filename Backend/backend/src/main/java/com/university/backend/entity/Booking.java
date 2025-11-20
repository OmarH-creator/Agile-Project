package com.university.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;


import java.util.Date;

@Embeddable
public class Booking {

    @Temporal(TemporalType.TIMESTAMP)
    private Date start;

    @Temporal(TemporalType.TIMESTAMP)
    private Date end;

    @Column(length = 255)
    private String purpose;
    private long reservationId;
    private long staffId;

    // Default constructor (Required by JPA)
    public Booking() {
    }

    public Booking( Date start, Date end, String purpose, long reservationId, long staffId) {
        this.start = start;
        this.end = end;
        this.purpose = purpose;
        this.reservationId = reservationId;
        this.staffId = staffId;

    }

    // Logic preserved exactly as requested
    public boolean conflictsWith(Booking other) {
        return start.before(other.end) && end.after(other.start);
    }

    public Date getStart() { return start; }
    public void setStart(Date start) { this.start = start; } // Setter needed for JPA mapping

    public Date getEnd() { return end; }
    public void setEnd(Date end) { this.end = end; } // Setter needed for JPA mapping

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; } // Setter needed for JPA mapping

    public long getReservationId() { return reservationId;}

    public void setReservationId(long reservationId) { this.reservationId = reservationId;}

    public long getStaffId() { return staffId;}

    public void setStaffId(long staffId) { this.staffId = staffId;}


}