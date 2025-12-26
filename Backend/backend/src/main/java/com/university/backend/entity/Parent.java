package com.university.backend.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "parents")
public class Parent {

    @Id
    @Column(unique = true, nullable = false, length = 255)
    private String parentId;   // e.g. PARENT-001

    @Column(unique = true, nullable = false)
    private String parentEmail;

    @Column(nullable = false)
    private String parentName;

    @Column(nullable = false)
    private String parentPassword;

    @Column(nullable = false)
    private String parentPhone;

    // One Parent can be responsible for MANY students
    @ElementCollection
    @CollectionTable(name = "parent_students",
            joinColumns = @JoinColumn(name = "parent_id"))
    @Column(name = "student_id")
    private List<String> studentIds = new ArrayList<>();

    // Required by JPA
    public Parent() {}

    public Parent(String parentId, String parentName, String parentEmail,
                  String parentPassword, String parentPhone) {
        this.parentId = parentId;
        this.parentName = parentName;
        this.parentEmail = parentEmail;
        this.parentPassword = parentPassword;
        this.parentPhone = parentPhone;
    }

    // Relationship helper
    public void assignStudent(String studentId) {
        this.studentIds.add(studentId);
    }

    // Getters & Setters
    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public String getParentName() { return parentName; }
    public void setParentName(String parentName) { this.parentName = parentName; }

    public String getParentEmail() { return parentEmail; }
    public void setParentEmail(String parentEmail) { this.parentEmail = parentEmail; }

    public String getParentPassword() { return parentPassword; }
    public void setParentPassword(String parentPassword) { this.parentPassword = parentPassword; }

    public String getParentPhone() { return parentPhone; }
    public void setParentPhone(String parentPhone) { this.parentPhone = parentPhone; }

    public List<String> getStudentIds() { return studentIds; }
    public void setStudentIds(List<String> studentIds) { this.studentIds = studentIds; }
}
