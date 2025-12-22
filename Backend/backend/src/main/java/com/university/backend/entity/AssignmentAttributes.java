package com.university.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "assignment_attributes")
public class AssignmentAttributes {

    //attributeId
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long AttributeId;

    //EntityId
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @Column(nullable = false)
    private String attributeName;

    @Column(nullable = false)
    private String dataType;

    public AssignmentAttributes() {}

    public AssignmentAttributes(Assignment assignment, String attributeName, String dataType) {
        this.assignment = assignment;
        this.attributeName = attributeName;
        this.dataType = dataType;
    }

    // Getters and Setters
    public Long getId() { return AttributeId; }
    public void setId(Long id) { this.AttributeId = id; }
    public Assignment getAssignment() { return assignment; }
    public void setAssignment(Assignment assignment) { this.assignment = assignment; }
    public String getAttributeName() { return attributeName; }
    public void setAttributeName(String attributeName) { this.attributeName = attributeName; }
    public String getDataType() { return dataType; }
    public void setDataType(String dataType) { this.dataType = dataType; }
}