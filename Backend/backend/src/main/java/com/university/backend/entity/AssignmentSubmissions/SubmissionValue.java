package com.university.backend.entity.AssignmentSubmissions;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "submission_values")
public class SubmissionValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ValueId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private AssignmentSubmission submission;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "attribute_id", nullable = false)
    private SubmissionAttributes attribute;

    // --- Sparse Columns ---
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

    // Constructors for different types
    public SubmissionValue(AssignmentSubmission sub, SubmissionAttributes attr, String val) {
        this.submission = sub; this.attribute = attr; this.valString = val;
    }
    public SubmissionValue(AssignmentSubmission sub, SubmissionAttributes attr, Integer val) {
        this.submission = sub; this.attribute = attr; this.valInt = val;
    }
    public SubmissionValue(AssignmentSubmission sub, SubmissionAttributes attr, Double val) {
        this.submission = sub; this.attribute = attr; this.valDouble = val;
    }
    public SubmissionValue(AssignmentSubmission sub, SubmissionAttributes attr, Boolean val) {
        this.submission = sub; this.attribute = attr; this.valBool = val;
    }
    public SubmissionValue(AssignmentSubmission sub, SubmissionAttributes attr, Date val) {
        this.submission = sub; this.attribute = attr; this.valDate = val;
    }

    // Getters and Setters
    public Long getId() { return ValueId; }
    public void setId(Long id) { this.ValueId = id; }
    public AssignmentSubmission getSubmission() { return submission; }
    public void setSubmission(AssignmentSubmission submission) { this.submission = submission; }
    public SubmissionAttributes getAttribute() { return attribute; }
    public void setAttribute(SubmissionAttributes attribute) { this.attribute = attribute; }

    public String getValString() { return valString; }
    public void setValString(String valString) { this.valString = valString; }
    public Integer getValInt() { return valInt; }
    public void setValInt(Integer valInt) { this.valInt = valInt; }
    public Double getValDouble() { return valDouble; }
    public void setValDouble(Double valDouble) { this.valDouble = valDouble; }
    public Boolean getValBool() { return valBool; }
    public void setValBool(Boolean valBool) { this.valBool = valBool; }
    public Date getValDate() { return valDate; }
    public void setValDate(Date valDate) { this.valDate = valDate; }
}