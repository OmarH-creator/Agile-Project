package com.university.backend.controllers;

import com.university.backend.entity.*;
import com.university.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private HallRepository hallRepository;

    // --- Student Management ---

    @PostMapping("/students")
    public ResponseEntity<String> createStudentRecord(@RequestBody Student student) {
        // Check using the Repo method
        if (studentRepository.existsByStudentId(student.getStudentId())) {
            return ResponseEntity.badRequest().body("Student with this ID already exists.");
        }
        studentRepository.save(student);
        return ResponseEntity.ok("Student created successfully.");
    }

    @GetMapping("/students/{studentId}")
    public ResponseEntity<?> getStudent(@PathVariable String studentId) {
        Optional<Student> student = studentRepository.findByStudentId(studentId);
        if (student.isPresent()) {
            return ResponseEntity.ok(student.get());
        }
        return ResponseEntity.status(404).body("Student not found.");
    }

    @GetMapping("/students/{studentId}/transcript")
    public ResponseEntity<String> generateTranscript(@PathVariable String studentId) {
        Optional<Student> studentOpt = studentRepository.findByStudentId(studentId);
        
        if (studentOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Student not found.");
        }

        Student s = studentOpt.get();
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
        
        return ResponseEntity.ok(sb.toString());
    }

    // --- Professor Management ---

    @PostMapping("/professors")
    public ResponseEntity<String> createProfessorRecord(@RequestBody Professor professor) {
        if (professorRepository.existsByProfessorId(professor.getProfessorId())) {
            return ResponseEntity.badRequest().body("Professor with this ID already exists.");
        }
        professorRepository.save(professor);
        return ResponseEntity.ok("Professor created successfully.");
    }

    @GetMapping("/professors/{professorId}")
    public ResponseEntity<?> getProfessor(@PathVariable String professorId) {
        Optional<Professor> prof = professorRepository.findByProfessorId(professorId);
        if (prof.isPresent()) {
            return ResponseEntity.ok(prof.get());
        }
        return ResponseEntity.status(404).body("Professor not found.");
    }

    @PutMapping("/professors/{professorId}/assign-course")
    public ResponseEntity<String> assignCourseToProfessor(
            @PathVariable String professorId, 
            @RequestParam String courseName) {
        
        Optional<Professor> profOpt = professorRepository.findByProfessorId(professorId);
        
        if (profOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Error: Professor with ID " + professorId + " not found.");
        }

        Professor targetProf = profOpt.get();
        targetProf.assignCourse(courseName);
        professorRepository.save(targetProf); // Save changes to DB
        
        return ResponseEntity.ok("Success: Course '" + courseName + "' assigned to " + targetProf.getProfessorName());
    }

    // --- Hall Management ---

    @PostMapping("/halls")
    public ResponseEntity<String> addHall(@RequestBody Hall hall) {
        if (hallRepository.findByHallName(hall.getHallName()).isPresent()) {
            return ResponseEntity.badRequest().body("Hall with this name already exists.");
        }
        hallRepository.save(hall);
        return ResponseEntity.ok("Hall added successfully.");
    }

    @PostMapping("/halls/book")
    public ResponseEntity<String> bookHall(@RequestBody BookingRequest request) {
        Optional<Hall> hallOpt = hallRepository.findByHallName(request.getHallName());

        if (hallOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Hall not found.");
        }

        Hall hall = hallOpt.get();
        boolean success = hall.book(request.getStart(), request.getEnd(), request.getPurpose());

        if (success) {
            hallRepository.save(hall); // Save the new booking to DB
            return ResponseEntity.ok("Hall booked successfully.");
        } else {
            return ResponseEntity.badRequest().body("Booking failed: Time conflict.");
        }
    }

    // Helper class for the booking JSON body
    public static class BookingRequest {
        private String hallName;
        private LocalDateTime start;
        private LocalDateTime end;
        private String purpose;

        // Getters and Setters
        public String getHallName() { return hallName; }
        public void setHallName(String hallName) { this.hallName = hallName; }
        public LocalDateTime getStart() { return start; }
        public void setStart(LocalDateTime start) { this.start = start; }
        public LocalDateTime getEnd() { return end; }
        public void setEnd(LocalDateTime end) { this.end = end; }
        public String getPurpose() { return purpose; }
        public void setPurpose(String purpose) { this.purpose = purpose; }
    }
}