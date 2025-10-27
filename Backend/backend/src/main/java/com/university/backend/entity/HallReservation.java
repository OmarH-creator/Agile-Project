package com.university.backend.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.universitymanagement.model.user.Admin;
import com.universitymanagement.model.user.User;
import com.universitymanagement.model.BaseEntity;
import com.universitymanagement.model.eav.EntityRecord;

@Entity
public class HallReservation extends BaseEntity {

    @ManyToOne
    private EntityRecord hall;

    @ManyToOne
    private User requestedBy;   // Faculty Member

    @ManyToOne
    private Admin approvedBy;   // Admin

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private String status; // PENDING, APPROVED, REJECTED

    public HallReservation() {}

    public HallReservation(EntityRecord hall, User requestedBy, LocalDateTime startTime, LocalDateTime endTime) {
        this.hall = hall;
        this.requestedBy = requestedBy;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = "PENDING";
    }

    // Getters and Setters
    public EntityRecord getHall() { return hall; }
    public void setHall(EntityRecord hall) { this.hall = hall; }

    public User getRequestedBy() { return requestedBy; }
    public void setRequestedBy(User requestedBy) { this.requestedBy = requestedBy; }

    public Admin getApprovedBy() { return approvedBy; }
    public void setApprovedBy(Admin approvedBy) { this.approvedBy = approvedBy; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}