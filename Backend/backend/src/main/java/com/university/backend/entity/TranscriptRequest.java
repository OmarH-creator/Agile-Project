package com.universitymanagement.model.academic;

import jakarta.persistence.*;
import com.universitymanagement.model.BaseEntity;
import com.universitymanagement.model.user.Student;
import java.time.LocalDateTime;

@Entity
public class TranscriptRequest extends BaseEntity {

    @ManyToOne
    private Student student;

    private LocalDateTime requestDate;
    private boolean processed;
    private String status; // "PENDING", "APPROVED", "REJECTED"

    public TranscriptRequest() {}

    public TranscriptRequest(Student student) {
        this.student = student;
        this.requestDate = LocalDateTime.now();
        this.status = "PENDING";
        this.processed = false;
    }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public LocalDateTime getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDateTime requestDate) { this.requestDate = requestDate; }

    public boolean isProcessed() { return processed; }
    public void setProcessed(boolean processed) { this.processed = processed; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
