package com.university.backend.entity.AssignmentSubmissions;

import com.university.backend.entity.Assignment.Assignment;
import com.university.backend.entity.Student;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assignment_submissions")
public class AssignmentSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long submissionId;

    // --- STATIC COLUMNS ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    // --- DYNAMIC EAV MAPPING ---

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubmissionAttributes> attributes = new ArrayList<>();

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubmissionValue> values = new ArrayList<>();

    // --- CONSTRUCTORS ---

    public AssignmentSubmission() {
        // Universal dynamic attributes for a submission
        this.addAttribute("Submission_Date", "DATE");
        this.addAttribute("Content", "STRING"); // Text content or URL
        this.addAttribute("Grade", "DOUBLE");
        this.addAttribute("Feedback", "STRING");
    }

    public AssignmentSubmission(Assignment assignment, Student student) {
        this(); // Initialize attributes
        this.assignment = assignment;
        this.student = student;
    }

    // Helper to add attribute
    private void addAttribute(String name, String type) {
        SubmissionAttributes attr = new SubmissionAttributes(this, name, type);
        this.attributes.add(attr);
    }

    // --- GETTERS AND SETTERS ---

    public Long getId() {
        return submissionId;
    }

    public void setId(Long id) {
        this.submissionId = id;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public List<SubmissionAttributes> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<SubmissionAttributes> attributes) {
        this.attributes = attributes;
    }

    public List<SubmissionValue> getValues() {
        return values;
    }

    public void setValues(List<SubmissionValue> values) {
        this.values = values;
    }
}