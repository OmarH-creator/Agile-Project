package com.university.backend.entity;

import java.util.ArrayList;
import java.util.List;

public class Student {

    private String studentId;
    private String name;
    private String email;
    private String department;

    private List<CourseRecord> completedCourses;
    private List<String> currentCourses;
    private List<String> allocatedResources;

    public Student(String studentId, String name, String email, String department) {
        this.studentId = studentId;
        this.name = name;
        this.email = email;
        this.department = department;

        this.allocatedResources = new ArrayList<>();
        this.completedCourses = new ArrayList<>();
        this.currentCourses = new ArrayList<>();
    }



    public void enrollCourse(String courseName) {
        currentCourses.add(courseName);
    }

    public void addCompletedCourse(String courseName, double grade, int credits) {
        completedCourses.add(new CourseRecord(courseName, grade, credits));
    }

    public double getGPA() {
        double totalPoints = 0;
        int totalCredits = 0;

        for (CourseRecord cr : completedCourses) {
            totalPoints += (cr.grade * cr.credits);
            totalCredits += cr.credits;
        }

        return totalCredits == 0 ? 0 : totalPoints / totalCredits;
    }

    public List<CourseRecord> getCompletedCourses() {
        return completedCourses;
    }


    public static class CourseRecord {
        public String courseName;
        public double grade;
        public int credits;

        public CourseRecord(String courseName, double grade, int credits) {
            this.courseName = courseName;
            this.grade = grade;
            this.credits = credits;
        }
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

    // Getters for ID needed by Admin ????
    public String getStudentId() {
        return this.studentId;
    }

    public String getName() {
        return this.name;
    }

    public String getEmail() {
        return this.email;
    }
    public String getDepartment() {
        return this.department;
    }
    public List<String> getAllocatedResources() {
        return this.allocatedResources;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public void setDepartment(String department) {
        this.department = department;
    }
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
}
