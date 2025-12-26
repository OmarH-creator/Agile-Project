package com.university.backend.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "course_grading_items")
public class CourseGradingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // LINK: Connects to your Course Entity
    // Uses 'courseCode' (String) as the foreign key
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private String categoryName; // e.g., "Midterm", "Labs", "Final Project"

    @Column(nullable = false)
    private Integer weightPercentage; // e.g., 20 (for 20%)

    // CONNECTED: List of assignments in this bucket
    @OneToMany(mappedBy = "gradingItem")
    private List<com.university.backend.entity.Assignment.Assignment> assignments;

    // --- CONSTRUCTORS ---
    public CourseGradingItem() {
    }

    public CourseGradingItem(Course course, String categoryName, Integer weightPercentage) {
        this.course = course;
        this.categoryName = categoryName;
        this.weightPercentage = weightPercentage;
    }

    // --- GETTERS & SETTERS ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Integer getWeightPercentage() {
        return weightPercentage;
    }

    public void setWeightPercentage(Integer weightPercentage) {
        this.weightPercentage = weightPercentage;
    }

    public java.util.List<com.university.backend.entity.Assignment.Assignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(java.util.List<com.university.backend.entity.Assignment.Assignment> assignments) {
        this.assignments = assignments;
    }
}