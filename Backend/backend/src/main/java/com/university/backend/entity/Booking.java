package com.university.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.util.Date;

@Embeddable
public class Booking {

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "start_time")
    private Date startTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "end_time")
    private Date endTime;

    @Column(length = 255)
    private String purpose;
    private long reservationId;
    private long staffId;

    // Default constructor (Required by JPA)
    public Booking() {
    }

    public Booking(Date startTime, Date endTime, String purpose, long reservationId, long staffId) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.purpose = purpose;
        this.reservationId = reservationId;
        this.staffId = staffId;
    }

    // Logic preserved exactly as requested
    public boolean conflictsWith(Booking other) {
        // compare using renamed fields
        return startTime.before(other.endTime) && endTime.after(other.startTime);
    }

    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }

    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public long getReservationId() { return reservationId; }
    public void setReservationId(long reservationId) { this.reservationId = reservationId; }

    public long getStaffId() { return staffId; }
    public void setStaffId(long staffId) { this.staffId = staffId; }
}