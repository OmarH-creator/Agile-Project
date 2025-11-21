package com.university.backend.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Primary Key for the Booking record

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "start_time")
    private Date startTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "end_time")
    private Date endTime;

    @Column(length = 255)
    private String purpose;
    private long reservationId;
    
    // staffId is now mandatory to link the Professor or Admin who made the booking
    @Column(nullable = false)
    private String staffId; 

    // Define the relationship: Many Bookings belong to One Hall
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hall_id", nullable = false)
    private Hall hall; // Reference to the Hall this booking is for

    // Default constructor (Required by JPA)
    public Booking() {
    }

    public Booking(Date startTime, Date endTime, String purpose, long reservationId, long staffId, Hall hall) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.purpose = purpose;
        this.reservationId = reservationId;
        this.staffId = staffId;
        this.hall = hall;
    }

    // Logic preserved
    public boolean conflictsWith(Booking other) {
        // A conflict occurs if they overlap in time
        return startTime.before(other.endTime) && endTime.after(other.startTime);
    }
    
    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }
    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public long getReservationId() { return reservationId; }
    public void setReservationId(long reservationId) { this.reservationId = reservationId; }
    public String getStaffId() { return staffId; }
    public void setStaffId(long staffId) { this.staffId = staffId; }
    public Hall getHall() { return hall; }
    public void setHall(Hall hall) { this.hall = hall; }
}