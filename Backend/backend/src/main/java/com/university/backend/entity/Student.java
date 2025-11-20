package com.university.backend.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String studentId;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String department;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Course_record> completedCourses = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "student_current_courses",
            joinColumns = @JoinColumn(name = "student_id"))
    @Column(name = "course_name")
    private List<String> currentCourses = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "student_allocated_resources",
            joinColumns = @JoinColumn(name = "student_id"))
    @Column(name = "resource")
    private List<String> allocatedResources = new ArrayList<>();

    // Default constructor (required by JPA)
    public Student() {
    }

    public Student(String studentId, String name, String email, String department) {
        this.studentId = studentId;
        this.name = name;
        this.email = email;
        this.department = department;
    }

    public void enrollCourse(String courseName) {
        currentCourses.add(courseName);
    }

    public void addCompletedCourse(String courseName, double grade, int credits) {
        Course_record record = new Course_record(courseName, grade, credits);
        record.setStudent(this);
        completedCourses.add(record);
    }

    public double getGPA() {
        double totalPoints = 0;
        int totalCredits = 0;

        for (Course_record cr : completedCourses) {
            totalPoints += (cr.getGrade() * cr.getCredits());
            totalCredits += cr.getCredits();
        }

        return totalCredits == 0 ? 0 : totalPoints / totalCredits;
    }

    @Override
    public String toString() {
        return "Student {" +
                "ID='" + studentId + '\'' +
                ", name='" + name + '\'' +
                ", department='" + department + '\'' +
                ", resources=" + allocatedResources +
                '}';
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public List<Course_record> getCompletedCourses() {
        return completedCourses;
    }

    public void setCompletedCourses(List<Course_record> completedCourses) {
        this.completedCourses = completedCourses;
    }

    public List<String> getCurrentCourses() {
        return currentCourses;
    }

    public void setCurrentCourses(List<String> currentCourses) {
        this.currentCourses = currentCourses;
    }

    public List<String> getAllocatedResources() {
        return allocatedResources;
    }

    public void setAllocatedResources(List<String> allocatedResources) {
        this.allocatedResources = allocatedResources;
    }
}