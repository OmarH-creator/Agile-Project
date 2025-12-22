package com.university.backend.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "submission_values")
public class SubmissionValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private AssignmentSubmission submission;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "attribute_id", nullable = false)
    private SubmissionAttributes attribute;

    @Column(name = "val_string")
    private String valString;
    @Column(name = "val_int")
    private Integer valInt;
    @Column(name = "val_double")
    private Double valDouble;
    @Column(name = "val_bool")
    private Boolean valBool;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "val_date")
    private Date valDate;

    public SubmissionValue() {}

    // Constructor example for Double (Score)
    public SubmissionValue(AssignmentSubmission sub, SubmissionAttributes attr, Double val) {
        this.submission = sub; this.attribute = attr; this.valDouble = val;
    }
    // Constructor example for String (StudentId, Feedback)
    public SubmissionValue(AssignmentSubmission sub, SubmissionAttributes attr, String val) {
        this.submission = sub; this.attribute = attr; this.valString = val;
    }

    // Getters/Setters (Similar to AssignmentValue, omitted for brevity but required)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AssignmentSubmission getSubmission() { return submission; }
    public void setSubmission(AssignmentSubmission submission) { this.submission = submission; }
    public SubmissionAttributes getAttribute() { return attribute; }
    public void setAttribute(SubmissionAttributes attribute) { this.attribute = attribute; }
    public String getValString() { return valString; }
    public void setValString(String valString) { this.valString = valString; }
    public Double getValDouble() { return valDouble; }
    public void setValDouble(Double valDouble) { this.valDouble = valDouble; }
    // ... add remaining getters/setters for Int, Bool, Date
}