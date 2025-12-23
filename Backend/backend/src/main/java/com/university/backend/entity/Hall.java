package com.university.backend.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "halls")
public class Hall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long hallId;

    // --- STATIC COLUMNS (Real Relationships) ---

    @Column(nullable = false)
    private String hallName;

    // --- DYNAMIC EAV MAPPING ---

    @OneToMany(mappedBy = "hall", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HallAttribute> attributes = new ArrayList<>();

    @OneToMany(mappedBy = "hall", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HallValue> values = new ArrayList<>();

    // --- CONSTRUCTORS ---

    public Hall() {
        // Universal dynamic attributes
        this.addAttribute("Name", "STRING");
        this.addAttribute("Location", "STRING");
        this.addAttribute("Capacity", "INTEGER");
    }

    public Hall(String hallName) {
        this(); // Initialize attributes
        this.hallName = hallName;
    }

    // Helper to add attribute
    private void addAttribute(String name, String type) {
        HallAttribute attr = new HallAttribute(this, name, type);
        this.attributes.add(attr);
    }

    // --- GETTERS AND SETTERS ---

    public Long getId() { return hallId; }
    public void setId(Long id) { this.hallId = id; }

    public String getHallName() { return hallName; }
    public void setHallName(String hallName) { this.hallName = hallName; }

    public List<HallAttribute> getAttributes() { return attributes; }
    public void setAttributes(List<HallAttribute> attributes) { this.attributes = attributes; }

    public List<HallValue> getValues() { return values; }
    public void setValues(List<HallValue> values) { this.values = values; }
}