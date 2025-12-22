package com.university.backend.entity;

import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "assignment_submissions")
public class AssignmentSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @Column(nullable = false)
    private String studentId;

    private Double score;

    private String feedback;

    // EAV Relationship: One Submission has many details
    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubmissionValue> submissionDetails = new ArrayList<>();

    // Default Constructor
    public AssignmentSubmission() {}

    // Parameterized Constructor
    public AssignmentSubmission(Assignment assignment, String studentId) {
        this.assignment = assignment;
        this.studentId = studentId;
    }

    // Helper method to add detail
    public void addDetail(SubmissionValue detail) {
        submissionDetails.add(detail);
        detail.setSubmission(this);
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public List<SubmissionValue> getSubmissionDetails() {
        return submissionDetails;
    }

    public void setSubmissionDetails(List<SubmissionValue> submissionDetails) {
        this.submissionDetails = submissionDetails;
    }
}