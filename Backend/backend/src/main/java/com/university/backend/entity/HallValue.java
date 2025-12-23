package com.university.backend.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "hall_values")
public class HallValue {

    //valueId
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ValueId;

    // Redundant FK to Entity (Per your requirement)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hall_id", nullable = false)
    private Hall hall;

    // FK to Attribute
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "attribute_id", nullable = false)
    private HallAttribute attribute;

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

    public HallValue() {}

    // Constructors for different types
    public HallValue(Hall hall, HallAttribute attribute, String val) {
        this.hall = hall; this.attribute = attribute; this.valString = val;
    }
    public HallValue(Hall hall, HallAttribute attribute, Integer val) {
        this.hall = hall; this.attribute = attribute; this.valInt = val;
    }
    public HallValue(Hall hall, HallAttribute attribute, Double val) {
        this.hall = hall; this.attribute = attribute; this.valDouble = val;
    }
    public HallValue(Hall hall, HallAttribute attribute, Boolean val) {
        this.hall = hall; this.attribute = attribute; this.valBool = val;
    }
    public HallValue(Hall hall, HallAttribute attribute, Date val) {
        this.hall = hall; this.attribute = attribute; this.valDate = val;
    }

    // Getters and Setters
    public Long getId() { return ValueId; }
    public void setId(Long id) { this.ValueId = id; }
    public Hall getHall() { return hall; }
    public void setHall(Hall hall) { this.hall = hall; }
    public HallAttribute getAttribute() { return attribute; }
    public void setAttribute(HallAttribute attribute) { this.attribute = attribute; }

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