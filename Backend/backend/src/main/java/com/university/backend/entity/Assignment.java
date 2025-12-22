package com.university.backend.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assignments")
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assignmentId;

    // --- STATIC COLUMNS (Real Relationships) ---

    @Column(nullable = false)
    private String title;

    // CONNECTED: Links to the Course Entity
    // This creates a Foreign Key column 'course_id' in the database
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    // CONNECTED: Links to the Professor/User Entity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id", nullable = false)
    private Professor professor;

    // --- DYNAMIC EAV MAPPING ---

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AssignmentAttributes> attributes = new ArrayList<>();

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AssignmentValue> values = new ArrayList<>();

    // --- CONSTRUCTORS ---

    public Assignment() {
        // Universal dynamic attributes
        this.addAttribute("Description", "STRING");
        this.addAttribute("Due_Date", "DATE");
        this.addAttribute("Max_Grade", "INTEGER");
        this.addAttribute("Attachment_Url", "STRING");
        this.addAttribute("Is_Visible", "BOOLEAN");
    }

    public Assignment(String title, Course course, Professor professor) {
        this(); // Initialize attributes
        this.title = title;
        this.course = course;
        this.professor = professor;
    }

    // Helper to add attribute
    private void addAttribute(String name, String type) {
        AssignmentAttributes attr = new AssignmentAttributes(this, name, type);
        this.attributes.add(attr);
    }

    // --- GETTERS AND SETTERS ---

    public Long getId() { return assignmentId; }
    public void setId(Long id) { this.assignmentId = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public Professor getProfessor() { return professor; }
    public void setProfessor(Professor professor) { this.professor = professor; }

    public List<AssignmentAttributes> getAttributes() { return attributes; }
    public void setAttributes(List<AssignmentAttributes> attributes) { this.attributes = attributes; }

    public List<AssignmentValue> getValues() { return values; }
    public void setValues(List<AssignmentValue> values) { this.values = values; }
}