package com.university.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.university.backend.entity.Hall.Hall;
import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long reservationId;// Primary Key for the Booking record

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "start_time")
    private Date startTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "end_time")
    private Date endTime;

    @Column(length = 255)
    private String purpose;

    
    // staffId is now mandatory to link the Professor or Admin who made the booking
    @Column(nullable = false)
    private String staffId; 

    // Define the relationship: Many Bookings belong to One Hall
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hallName", nullable = false)
    @JsonIgnore
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Hall hall; // Reference to the Hall this booking is for

    // Transient field: This allows the JSON to send "hallName" as a string,
    // and we can read it, even though we don't save it to this column directly.
    @Transient
    private String hallName;

    // Default constructor (Required by JPA)
    public Booking() {
    }

    public Booking(Date startTime, Date endTime, String purpose, long reservationId, String staffId, Hall hall) {
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

    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }
    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public long getReservationId() { return reservationId; }
    public void setReservationId(long reservationId) { this.reservationId = reservationId; }
    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }
    public Hall getHall() { return hall; }
    public void setHall(Hall hall) { this.hall = hall;
        // Automatically set the transient name for convenience
        if(hall != null) this.hallName = hall.getHallName();
    }

    // This Getter allows the Frontend to see "hallName": "Lecture Hall A" in the response
    public String getHallName() {
        if (hall != null) return hall.getHallName();
        return hallName;
    }

    public void setHallName(String hallName) {
        this.hallName = hallName;
    }

}