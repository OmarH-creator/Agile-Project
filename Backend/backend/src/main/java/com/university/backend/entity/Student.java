package com.university.backend.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 255)
    private String studentId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(unique = true, nullable = false, length = 255)
    private String email;

    // Persist the Major when persisting a Student to avoid transient-major errors in tests
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "major_id", nullable = false)
    private Major major;

    @Column(unique = true, nullable = false, length = 255)
    private String phone;

    @Column(unique = true, nullable = false, length = 255)
    private String address;

    @Temporal(TemporalType.DATE) // Good practice for java.util.Date
    private Date dateOfBirth;

    @Column(unique = true, nullable = false, length = 255)
    private String militaryStatus; // Renamed to camelCase for standard convention

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Course_record> completedCourses = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "student_current_courses",
            joinColumns = @JoinColumn(name = "student_id"))
    @Column(name = "course_name", length = 255)
    private List<String> currentCourses = new ArrayList<>();

    // Default constructor (required by JPA)
    public Student() {
    }

    public Student(String studentId, String name, String email, Major major, String phone, String address, Date dateOfBirth, String militaryStatus) {
        this.studentId = studentId;
        this.name = name;
        this.email = email;
        this.major = major;
        this.phone = phone;
        this.address = address;
        this.dateOfBirth = dateOfBirth;
        this.militaryStatus = militaryStatus;
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
                "id=" + id +
                ", studentId='" + studentId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", major=" + (major != null ? major.getMajorName() : "N/A") +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", dob=" + dateOfBirth +
                ", militaryStatus='" + militaryStatus + '\'' +
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

    public Major getMajor() {
        return major;
    }

    public void setMajor(Major major) {
        this.major = major;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getMilitaryStatus() {
        return militaryStatus;
    }

    public void setMilitaryStatus(String militaryStatus) {
        this.militaryStatus = militaryStatus;
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
}