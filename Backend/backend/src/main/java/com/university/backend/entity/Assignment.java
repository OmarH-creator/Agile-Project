package com.university.backend.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assignments")
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long AssignmentId;

    // 1. Attributes are auto-generated here
    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AssignmentAttributes> attributes = new ArrayList<>();

    // 2. Values link here
    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AssignmentValue> values = new ArrayList<>();

    // --- CONSTRUCTOR: The Magic Happens Here ---
    public Assignment() {
        // Automatically create the required attribute rows for this instance
        this.addAttribute("Title", "STRING");
        this.addAttribute("Description", "STRING");
        this.addAttribute("Course_Id", "STRING");
        this.addAttribute("Professor_Id", "STRING");
        this.addAttribute("Deadline", "DATE");
    }

    // Helper to add attribute to the list
    private void addAttribute(String name, String type) {
        AssignmentAttributes attr = new AssignmentAttributes(this, name, type);
        this.attributes.add(attr);
    }

    // Getters and Setters
    public Long getId() { return AssignmentId; }
    public void setId(Long id) { this.AssignmentId = id; }
    public List<AssignmentAttributes> getAttributes() { return attributes; }
    public void setAttributes(List<AssignmentAttributes> attributes) { this.attributes = attributes; }
    public List<AssignmentValue> getValues() { return values; }
    public void setValues(List<AssignmentValue> values) { this.values = values; }
}