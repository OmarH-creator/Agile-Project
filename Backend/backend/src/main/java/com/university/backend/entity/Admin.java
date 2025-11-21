package com.university.backend.entity;

import jakarta.persistence.*;
import java.util.Date;
import java.util.ArrayList;

import java.util.List;

import com.university.backend.repository.UniversityRepository;

@Entity
@Table(name = "admin")
public class Admin {

    @Id
    private String adminId; // <--- ADDED THIS FIELD. This fixes the error at the bottom.

    @Column(nullable = false, length = 255)
    private String name;

    @Column(unique = true, nullable = false, length = 255)
    private String email;

    // Default constructor (required by JPA)
    public Admin() {
    }

    public Admin(String adminId, String name, String email) {
        this.adminId = adminId;
        this.name = name;
        this.email = email;
    }

    // --- Existing Logic Preserved ---

    public void createStudentRecord(String studentId, String name, String email, Major major, String phone, String address, Date dateOfBirth, String militaryStatus) {
        Student newStudent = new Student(studentId, name, email, major , phone, address, dateOfBirth, militaryStatus);
        UniversityRepository.students.add(newStudent);
    }

    public void createProfessorRecord(String id, String name, String email, String department) {
        Professor newProfessor = new Professor(id, name, email, department);
        UniversityRepository.professors.add(newProfessor);
    }

    public void addStudent(Student student) {
        UniversityRepository.students.add(student);
    }

    public Student getStudent(String studentId) {
        return UniversityRepository.students.stream()
                .filter(s -> s.getStudentId().equals(studentId))
                .findFirst()
                .orElse(null);
    }

    public Professor getProfessor(String profId){
        return UniversityRepository.professors.stream()
                .filter(s -> s.getProfessorId().equals(profId))
                .findFirst()
                .orElse(null);
    }

    public void addHall(Hall hall) {
        UniversityRepository.halls.add(hall);
    }

    public boolean bookHall(String hallName,Date start, Date end, String purpose, long reservationId, String staffId) {
        for (Hall hall : UniversityRepository.halls) {
            if (hall.getHallName().equals(hallName)) {
                return hall.book(start, end, purpose, reservationId, staffId);
            }
        }
        return false;
    }

    public String generateTranscript(String studentId) {
        Student s = getStudent(studentId);
        if (s == null) return "Student not found.";

        StringBuilder sb = new StringBuilder();
        sb.append("===== Transcript for ").append(s.getName()).append(" =====\n");

        for (Course_record cr : s.getCompletedCourses()) {
            sb.append(cr.getCourseName())
                    .append(" | Grade: ").append(cr.getGrade())
                    .append(" | Credits: ").append(cr.getCredits())
                    .append("\n");
        }

        sb.append("GPA: ").append(s.getGPA()).append("\n");
        sb.append("======================================");
        return sb.toString();
    }

    public String assignCourseToProfessor(String professorId, String courseName) {
        Professor targetProf = UniversityRepository.professors.stream()
                .filter(p -> p.getProfessorId().equals(professorId))
                .findFirst()
                .orElse(null);

        if (targetProf == null) {
            return "Error: Professor with ID " + professorId + " not found.";
        }

        targetProf.assignCourse(courseName);
        return "Success: Course '" + courseName + "' assigned to " + targetProf.getProfessorName();
    }

    // Add these methods to your Admin class

    public String addCourse(String courseCode, String courseName, int creditHours,String semester) {
        // Check if course already exists
        Course existingCourse = UniversityRepository.courses.stream()
                .filter(c -> c.getCourseCode().equals(courseCode))
                .findFirst()
                .orElse(null);

        if (existingCourse != null) {
            return "Error: Course with code " + courseCode + " already exists.";
        }

        // Create and add new course
        Course newCourse = new Course(courseCode, courseName, creditHours,semester);
        UniversityRepository.courses.add(newCourse);
        return "Success: Course '" + courseName + "' (" + courseCode + ") added successfully.";
    }

    public String removeCourse(String courseCode) {
        // Find the course to remove
        Course courseToRemove = UniversityRepository.courses.stream()
                .filter(c -> c.getCourseCode().equals(courseCode))
                .findFirst()
                .orElse(null);

        if (courseToRemove == null) {
            return "Error: Course with code " + courseCode + " not found.";
        }

        // Check if course has any students enrolled (optional safety check)
//        boolean hasEnrollments = UniversityRepository.students.stream()
//                .anyMatch(student -> student.getCurrentCourses().stream()
//                        .anyMatch(course -> course.getCourseCode().equals(courseCode)));
//
//        if (hasEnrollments) {
//            return "Error: Cannot remove course " + courseCode + " because students are currently enrolled.";
//        }

        // Remove the course
        UniversityRepository.courses.remove(courseToRemove);
        return "Success: Course '" + courseToRemove.getCourseName() + "' (" + courseCode + ") removed successfully.";
    }

    // Method to get all courses
    public List<Course> getAllCourses() {
        return new ArrayList<>(UniversityRepository.courses);
    }

    // Method to find course by code
    public Course getCourse(String courseCode) {
        return UniversityRepository.courses.stream()
                .filter(c -> c.getCourseCode().equals(courseCode))
                .findFirst()
                .orElse(null);
    }

    // Method to update course information
    public String updateCourse(String courseCode, String newCourseName, Integer newCreditHours) {
        Course courseToUpdate = getCourse(courseCode);

        if (courseToUpdate == null) {
            return "Error: Course with code " + courseCode + " not found.";
        }

        if (newCourseName != null && !newCourseName.trim().isEmpty()) {
            courseToUpdate.setCourseName(newCourseName);
        }

        if (newCreditHours != null && newCreditHours > 0) {
            courseToUpdate.setCreditHours(newCreditHours);
        }

        return "Success: Course " + courseCode + " updated successfully.";
    }

    // FIXED CORRUPTED METHOD - Removed duplicate datasource configurations and fixed structure
    @Transient
    private List<String> assignedCourses = new ArrayList<>();

    public List<String> getAssignedCourses() {
        return assignedCourses;
    }

    public void setAssignedCourses(List<String> assignedCourses) {
        this.assignedCourses = assignedCourses;
    }

    public void addAssignedCourse(String courseCode) {
        if (this.assignedCourses == null) {
            this.assignedCourses = new ArrayList<>();
        }
        this.assignedCourses.add(courseCode); // Just add the string directly
    }

    // Getters and Setters for JPA

    public String getAdminId() { return adminId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}