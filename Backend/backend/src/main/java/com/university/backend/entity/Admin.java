package com.university.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.university.backend.repository.UniversityRepository;

@Entity
@Table(name = "admins")
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // <--- ADDED THIS FIELD. This fixes the error at the bottom.

    @Column(unique = true, nullable = false)
    private String adminId;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
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

    public void createStudentRecord(String id, String name, String email, String department) {
        Student newStudent = new Student(id, name, email, department);
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

    public boolean bookHall(String hallName, LocalDateTime start, LocalDateTime end, String purpose) {
        for (Hall hall : UniversityRepository.halls) {
            if (hall.getHallName().equals(hallName)) {
                return hall.book(start, end, purpose);
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

    // Getters and Setters for JPA
    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    
    public String getAdminId() { return adminId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}