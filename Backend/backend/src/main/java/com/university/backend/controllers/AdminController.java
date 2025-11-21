package com.university.backend.controllers;

import com.university.backend.entity.*;
import com.university.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
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
    
 // --- REFINED: Updates specific fields of an existing student record ---
    @PutMapping("/students/{studentId}")
    public ResponseEntity<String> updateStudentRecord(@PathVariable String studentId, @RequestBody Student updatedStudent) {
        Optional<Student> studentOpt = studentRepository.findByStudentId(studentId);

        if (studentOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Error: Student with ID " + studentId + " not found.");
        }

        Student existingStudent = studentOpt.get();

        // Update the fields based on the incoming JSON body
        // We only update mutable fields like name, email, phone, etc.
        existingStudent.setName(updatedStudent.getName());
        existingStudent.setEmail(updatedStudent.getEmail());
        existingStudent.setPhone(updatedStudent.getPhone());
        existingStudent.setAddress(updatedStudent.getAddress());
        existingStudent.setDateOfBirth(updatedStudent.getDateOfBirth());
        existingStudent.setMilitaryStatus(updatedStudent.getMilitaryStatus());
        // Note: Major and ID fields are usually handled separately or not updated via this endpoint.

        studentRepository.save(existingStudent);
        return ResponseEntity.ok("Student record updated successfully for ID " + studentId + ".");
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
    
    /** NEW: Deletes a student record by studentId **/
    @DeleteMapping("/students/{studentId}")
    public ResponseEntity<String> deleteStudentRecord(@PathVariable String studentId) {
        if (studentRepository.existsByStudentId(studentId)) {
            // Use the new deleteByStudentId method
            studentRepository.deleteByStudentId(studentId); 
            return ResponseEntity.ok("Student with ID " + studentId + " removed successfully.");
        }
        return ResponseEntity.status(404).body("Student with ID " + studentId + " not found.");
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
    
 // --- REFINED: Updates specific fields of an existing professor record ---
    @PutMapping("/professors/{professorId}")
    public ResponseEntity<String> updateProfessorRecord(@PathVariable String professorId, @RequestBody Professor updatedProfessor) {
        Optional<Professor> profOpt = professorRepository.findByProfessorId(professorId);

        if (profOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Error: Professor with ID " + professorId + " not found.");
        }

        Professor existingProfessor = profOpt.get();

        // Update the fields based on the incoming JSON body
        existingProfessor.setProfessorName(updatedProfessor.getProfessorName());
        existingProfessor.setProfessorEmail(updatedProfessor.getProfessorEmail());
        existingProfessor.setProfessorDepartment(updatedProfessor.getProfessorDepartment());
        // Note: ProfessorCourses list is managed by the assign-course endpoint or a separate update.

        professorRepository.save(existingProfessor);
        return ResponseEntity.ok("Professor record updated successfully for ID " + professorId + ".");
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
    
    /** NEW: Deletes a professor record by professorId **/
    @DeleteMapping("/professors/{professorId}")
    public ResponseEntity<String> deleteProfessorRecord(@PathVariable String professorId) {
        if (professorRepository.existsByProfessorId(professorId)) {
            // Use the new deleteByProfessorId method
            professorRepository.deleteByProfessorId(professorId);
            return ResponseEntity.ok("Professor with ID " + professorId + " removed successfully.");
        }
        return ResponseEntity.status(404).body("Professor with ID " + professorId + " not found.");
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
        boolean success = hall.book(request.getStart(), request.getEnd(), request.getPurpose(),request.getReservationId(),request.getStaffId());

        if (success) {
            hallRepository.save(hall); // Save the new booking to DB
            return ResponseEntity.ok("Hall booked successfully.");
        } else {
            return ResponseEntity.badRequest().body("Booking failed: Time conflict.");
        }
    }
    
 // --- REFINED: Updates capacity and/or name of an existing hall record ---
    @PutMapping("/halls/{hallName}")
    public ResponseEntity<String> updateHallRecord(@PathVariable String hallName, @RequestBody Hall updatedHall) {
        Optional<Hall> existingHallOpt = hallRepository.findByHallName(hallName);
        
        if (existingHallOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Error: Hall '" + hallName + "' not found.");
        }
        
        Hall existingHall = existingHallOpt.get();
        
        // Update the capacity
        existingHall.setCapacity(updatedHall.getCapacity());
        
        // Allow updating the name as well
        if (updatedHall.getHallName() != null && !updatedHall.getHallName().equals(existingHall.getHallName())) {
            // Check if the new name is already taken by a different hall (important for unique constraints)
            if (hallRepository.existsByHallName(updatedHall.getHallName())) {
                 return ResponseEntity.badRequest().body("Error: New hall name '" + updatedHall.getHallName() + "' is already in use.");
            }
            existingHall.setHallName(updatedHall.getHallName()); 
        }

        hallRepository.save(existingHall);
        return ResponseEntity.ok("Hall record updated successfully.");
    }
    
    /** NEW: Deletes a hall record by hallName **/
    @DeleteMapping("/halls/{hallName}")
    public ResponseEntity<String> deleteHall(@PathVariable String hallName) {
        if (hallRepository.existsByHallName(hallName)) {
            // Use the new deleteByHallName method
            hallRepository.deleteByHallName(hallName);
            return ResponseEntity.ok("Hall '" + hallName + "' removed successfully.");
        }
        return ResponseEntity.status(404).body("Hall '" + hallName + "' not found.");
    }

    // Helper class for the booking JSON body
    public static class BookingRequest {
        private String hallName;
        private Date start;
        private Date end;
        private String purpose;
        private long reservationId;
        private String staffId;

        // Getters and Setters
        public String getHallName() { return hallName; }
        public void setHallName(String hallName) { this.hallName = hallName; }
        public Date getStart() { return start; }
        public void setStart(Date start) { this.start = start; }
        public Date getEnd() { return end; }
        public void setEnd(Date end) { this.end = end; }
        public String getPurpose() { return purpose; }
        public void setPurpose(String purpose) { this.purpose = purpose; }
        public long getReservationId() { return reservationId; }
        public void setReservationId(long reservationId) { this.reservationId = reservationId; }
        public String  getStaffId() { return staffId; }
        public void setStaffId(String staffId) { this.staffId = staffId; }
    }
}