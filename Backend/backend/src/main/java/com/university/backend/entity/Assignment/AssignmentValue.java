package com.university.backend.entity.Assignment;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "assignment_values")
public class AssignmentValue {

    // valueId
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ValueId;

    // Redundant FK to Entity (Per your requirement)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    // FK to Attribute
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "attribute_id", nullable = false)
    private AssignmentAttributes attribute;

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

    public AssignmentValue() {
    }

    // Constructors for different types
    public AssignmentValue(Assignment asm, AssignmentAttributes attr, String val) {
        this.assignment = asm;
        this.attribute = attr;
        this.valString = val;
    }

    public AssignmentValue(Assignment asm, AssignmentAttributes attr, Integer val) {
        this.assignment = asm;
        this.attribute = attr;
        this.valInt = val;
    }

    public AssignmentValue(Assignment asm, AssignmentAttributes attr, Double val) {
        this.assignment = asm;
        this.attribute = attr;
        this.valDouble = val;
    }

    public AssignmentValue(Assignment asm, AssignmentAttributes attr, Boolean val) {
        this.assignment = asm;
        this.attribute = attr;
        this.valBool = val;
    }

    public AssignmentValue(Assignment asm, AssignmentAttributes attr, Date val) {
        this.assignment = asm;
        this.attribute = attr;
        this.valDate = val;
    }

    // Getters and Setters
    public Long getId() {
        return ValueId;
    }

    public void setId(Long id) {
        this.ValueId = id;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    public AssignmentAttributes getAttribute() {
        return attribute;
    }

    public void setAttribute(AssignmentAttributes attribute) {
        this.attribute = attribute;
    }

    public String getValString() {
        return valString;
    }

    public void setValString(String valString) {
        this.valString = valString;
    }

    public Integer getValInt() {
        return valInt;
    }

    public void setValInt(Integer valInt) {
        this.valInt = valInt;
    }

    public Double getValDouble() {
        return valDouble;
    }

    public void setValDouble(Double valDouble) {
        this.valDouble = valDouble;
    }

    public Boolean getValBool() {
        return valBool;
    }

    public void setValBool(Boolean valBool) {
        this.valBool = valBool;
    }

    public Date getValDate() {
        return valDate;
    }

    public void setValDate(Date valDate) {
        this.valDate = valDate;
    }
}