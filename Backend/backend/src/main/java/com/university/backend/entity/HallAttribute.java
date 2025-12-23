package com.university.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "hall_attributes")
public class HallAttribute {

    //attributeId
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long AttributeId;

    //EntityId
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hall_id", nullable = false)
    private Hall hall;

    @Column(nullable = false)
    private String attributeName;

    @Column(nullable = false)
    private String dataType;

    public HallAttribute() {}

    public HallAttribute(Hall hall, String attributeName, String dataType) {
        this.hall = hall;
        this.attributeName = attributeName;
        this.dataType = dataType;
    }

    // Getters and Setters
    public Long getId() { return AttributeId; }
    public void setId(Long id) { this.AttributeId = id; }
    public Hall getHall() { return hall; }
    public void setHall(Hall hall) { this.hall = hall; }
    public String getAttributeName() { return attributeName; }
    public void setAttributeName(String attributeName) { this.attributeName = attributeName; }
    public String getDataType() { return dataType; }
    public void setDataType(String dataType) { this.dataType = dataType; }
}