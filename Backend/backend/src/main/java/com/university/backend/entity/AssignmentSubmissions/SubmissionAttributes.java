package com.university.backend.entity.AssignmentSubmissions;

import jakarta.persistence.*;

@Entity
@Table(name = "submission_attributes")
public class SubmissionAttributes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long AttributeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private AssignmentSubmission submission;

    @Column(nullable = false)
    private String attributeName;

    @Column(nullable = false)
    private String dataType;

    public SubmissionAttributes() {}

    public SubmissionAttributes(AssignmentSubmission submission, String attributeName, String dataType) {
        this.submission = submission;
        this.attributeName = attributeName;
        this.dataType = dataType;
    }

    // Getters and Setters
    public Long getId() { return AttributeId; }
    public void setId(Long id) { this.AttributeId = id; }
    public AssignmentSubmission getSubmission() { return submission; }
    public void setSubmission(AssignmentSubmission submission) { this.submission = submission; }
    public String getAttributeName() { return attributeName; }
    public void setAttributeName(String attributeName) { this.attributeName = attributeName; }
    public String getDataType() { return dataType; }
    public void setDataType(String dataType) { this.dataType = dataType; }
}