package com.university.backend.entity.StaffRequests;

import jakarta.persistence.*;

@Entity
@Table(name = "request_attributes")
public class RequestAttributes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long AttributeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private StaffRequest request;

    @Column(nullable = false)
    private String attributeName;

    @Column(nullable = false)
    private String dataType;

    public RequestAttributes() {
    }

    public RequestAttributes(StaffRequest request, String attributeName, String dataType) {
        this.request = request;
        this.attributeName = attributeName;
        this.dataType = dataType;
    }

    // Getters and Setters
    public Long getId() {
        return AttributeId;
    }

    public void setId(Long id) {
        this.AttributeId = id;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public StaffRequest getRequest() {
        return request;
    }

    public void setRequest(StaffRequest request) {
        this.request = request;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
}