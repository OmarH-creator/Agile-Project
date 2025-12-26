package com.university.backend.entity.StaffRequests;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "request_values")
public class RequestValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ValueId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private StaffRequest request;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "attribute_id", nullable = false)
    private RequestAttributes attribute;

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

    public RequestValue() {
    }

    // Constructors for different types
    public RequestValue(StaffRequest request, RequestAttributes attr, String val) {
        this.request = request;
        this.attribute = attr;
        this.valString = val;
    }

    public RequestValue(StaffRequest request, RequestAttributes attr, Integer val) {
        this.request = request;
        this.attribute = attr;
        this.valInt = val;
    }

    public RequestValue(StaffRequest request, RequestAttributes attr, Double val) {
        this.request = request;
        this.attribute = attr;
        this.valDouble = val;
    }

    public RequestValue(StaffRequest request, RequestAttributes attr, Boolean val) {
        this.request = request;
        this.attribute = attr;
        this.valBool = val;
    }

    public RequestValue(StaffRequest request, RequestAttributes attr, Date val) {
        this.request = request;
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

    @com.fasterxml.jackson.annotation.JsonIgnore
    public StaffRequest getRequest() {
        return request;
    }

    public void setRequest(StaffRequest request) {
        this.request = request;
    }

    public RequestAttributes getAttribute() {
        return attribute;
    }

    public void setAttribute(RequestAttributes attribute) {
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