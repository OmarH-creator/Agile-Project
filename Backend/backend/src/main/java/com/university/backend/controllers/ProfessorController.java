package com.university.backend.controllers;

import com.university.backend.entity.*;
import com.university.backend.entity.Assignment.Assignment;
import com.university.backend.entity.AssignmentSubmissions.AssignmentSubmission;
import com.university.backend.entity.AssignmentSubmissions.SubmissionAttributes;
import com.university.backend.entity.AssignmentSubmissions.SubmissionValue;
import com.university.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import com.university.backend.dto.AssignmentResponseDTO;
import com.university.backend.services.AssignmentService;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/professor")
public class ProfessorController {

    // --- Repositories ---
    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private HallRepository hallRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private AssignmentSubmissionRepository submissionRepository;

    private final AssignmentService assignmentService;

    public ProfessorController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }
    // =================================================================
    // SECTION 1: PROFESSOR DASHBOARD (Courses & Students)
    // =================================================================

    /**
     * 1. Get the tabs/list of courses a specific professor teaches.
     * Endpoint: GET /api/professor/{professorId}/courses
     */
    @GetMapping("/{professorId}/courses")
    public ResponseEntity<?> getProfessorCourses(@PathVariable String professorId) {
        Optional<Professor> prof = professorRepository.findByProfessorId(professorId);

        if (prof.isEmpty()) {
            return ResponseEntity.status(404).body("Error: Professor not found.");
        }

        // Returns List<String> of course names
        return ResponseEntity.ok(prof.get().getProfessorCourses());
    }

    /**
     * 2. When a course tab is clicked, show students currently taking that course.
     * Endpoint: GET /api/professor/course/{courseName}/students
     */
    @GetMapping("/course/{courseName}/students")
    public ResponseEntity<?> getStudentsInCourse(@PathVariable String courseName) {
        // Requires the custom method in StudentRepository: findByCurrentCoursesContaining(String courseName)
        // 1. Fetch the list
        List<Student> students = studentRepository.findByCurrentCoursesContaining(courseName);

        // 2. Return the list directly. Spring Boot handles empty lists automatically (returns [])
        return ResponseEntity.ok(students);
    }


    // =================================================================
    // SECTION 2: GRADING (Final Course Grades)
    // =================================================================

    /**
     * 3. Assign a final grade for a course.
     * This moves the course from 'Current Courses' to 'Completed Courses' (History).
     * Endpoint: POST /api/professor/course/grade
     */
    @PostMapping("/course/grade")
    public ResponseEntity<?> assignFinalGrade(@RequestBody FinalGradeRequest request) {
        // Validate Student
        Optional<Student> studentOpt = studentRepository.findByStudentId(request.getStudentId());
        if (studentOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Error: Student not found.");
        }

        Student student = studentOpt.get();

        // Validate Enrollment
        if (!student.getCurrentCourses().contains(request.getCourseName())) {
            return ResponseEntity.badRequest().body("Error: Student is not currently enrolled in " + request.getCourseName());
        }

        // Fetch Course details to get accurate credit hours
        Optional<Course> courseOpt = courseRepository.findByCourseName(request.getCourseName());
        int credits = courseOpt.map(Course::getCreditHours).orElse(3); // Default to 3 if not found in DB

        // Remove from Current and Add to History
        student.getCurrentCourses().remove(request.getCourseName());
        student.addCompletedCourse(request.getCourseName(), request.getGrade(), credits, request.getSemester());

        // Save changes
        studentRepository.save(student);

        return ResponseEntity.ok("Success: Grade assigned. Course moved to student history.");
    }


    // =================================================================
    // SECTION 3: ASSIGNMENTS (Create, View, Grade)
    // =================================================================

    // ------------------------------------------------------------
    // GET: Student views a specific assignment
    // URL: http://localhost:8080/api/assignments/1
    // ------------------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<AssignmentResponseDTO> getAssignment(@PathVariable Long id) {
        // 1. Call Service (which calls Repo -> maps to DTO)
        AssignmentResponseDTO assignmentDto = assignmentService.getAssignmentById(id);

        // 2. Return the clean JSON with 200 OK status
        return ResponseEntity.ok(assignmentDto);
    }

    // 2. POST: Create Assignment (NEW)
    @PostMapping("/create")
    public ResponseEntity<AssignmentResponseDTO> createAssignment(@RequestBody Map<String, Object> payload) {
        // payload matches the JSON structure: { "Title": "...", "Course_Id": "...", ... }
        AssignmentResponseDTO newAssignment = assignmentService.createAssignment(payload);
        return ResponseEntity.ok(newAssignment);
    }
    /**
     * 5. Grade a specific student's assignment.
     * Endpoint: POST /api/professor/assignment/grade
     */
    @PostMapping("/assignment/grade")
    public ResponseEntity<?> gradeAssignment(@RequestBody AssignmentGradeRequest request) {
        // Validate Assignment Exists
        Optional<Assignment> assignmentOpt = assignmentRepository.findById(request.getAssignmentId());
        if (assignmentOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Error: Assignment ID not found.");
        }

        // Validate Student Exists
        Optional<Student> studentOpt = studentRepository.findByStudentId(request.getStudentId());
        if (studentOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Error: Student ID not found.");
        }

        // Check if a submission already exists (update it), or create a new one
        // Note: findFullSubmissionById returns Optional<AssignmentSubmission>
        // But we need to find by Assignment AND Student, which the current repo method doesn't do directly.
        // For now, let's assume we are creating a new one or finding it manually if needed.
        // Ideally, you'd have submissionRepository.findByAssignmentAndStudent(...)

        // Simplified logic: Create new submission or update if we had a way to find it by student+assignment
        AssignmentSubmission submission = new AssignmentSubmission(assignmentOpt.get(), studentOpt.get());
        
        // Save first to generate attributes
        submission = submissionRepository.save(submission);

        // Update Score and Feedback using EAV
        updateSubmissionValue(submission, "Grade", request.getScore());
        updateSubmissionValue(submission, "Feedback", request.getFeedback());

        submissionRepository.save(submission);

        return ResponseEntity.ok("Success: Assignment graded.");
    }

    // Helper to update EAV values
    private void updateSubmissionValue(AssignmentSubmission submission, String key, Object value) {
        Optional<SubmissionAttributes> attrOpt = submission.getAttributes().stream()
                .filter(a -> a.getAttributeName().equals(key))
                .findFirst();

        if (attrOpt.isPresent()) {
            SubmissionAttributes attr = attrOpt.get();
            SubmissionValue val = new SubmissionValue();
            val.setSubmission(submission);
            val.setAttribute(attr);
            
            if (value instanceof Double) val.setValDouble((Double) value);
            else if (value instanceof String) val.setValString((String) value);
            
            submission.getValues().add(val);
        }
    }

    @GetMapping("/assignment/{assignmentId}/submissions")
    public ResponseEntity<List<AssignmentSubmission>> getAssignmentSubmissions(@PathVariable Long assignmentId) {
        // This returns all submissions for an assignment
        // Note: This might return a list, but the repo method findFullSubmissionById returns Optional (single).
        // You likely need a findAllByAssignmentId in the repo.
        // For now, returning empty or fixing the repo call is needed.
        // Assuming we want to list all submissions for an assignment:
        // List<AssignmentSubmission> submissions = submissionRepository.findAllByAssignmentId(assignmentId);
        return ResponseEntity.ok(List.of()); // Placeholder until repo is updated
    }

    // =================================================================
    // SECTION 4: HALL BOOKING (Legacy Feature)
    // =================================================================

    /**
     * Book a hall for a lecture/event.
     * Endpoint: POST /api/professor/halls/book
     */
//    @PostMapping("/halls/book")
//    public ResponseEntity<String> bookHallByProfessor(@RequestBody ProfessorBookingRequest request) {
//
//        // 1. Validate Professor Existence
//        if (!professorRepository.existsByProfessorId(request.getProfessorId())) {
//            return ResponseEntity.status(401).body("Error: Professor with ID " + request.getProfessorId() + " not authorized or not found.");
//        }
//
//        // 2. Find Hall
//        Optional<Hall> hallOpt = hallRepository.findByHallName(request.getHallName());
//        if (hallOpt.isEmpty()) {
//            return ResponseEntity.status(404).body("Hall '" + request.getHallName() + "' not found.");
//        }
//
//        Hall hall = hallOpt.get();
//
//        // 3. Conflict Check (Delegate to Hall entity logic)
//        if (!hall.isAvailable(request.getStart(), request.getEnd())) {
//            return ResponseEntity.badRequest().body("Booking failed: Time conflict or hall is unavailable.");
//        }
//
//        // 4. Create and Save Booking
//        Booking newBooking = new Booking(
//                request.getStart(),
//                request.getEnd(),
//                request.getPurpose(),
//                request.getReservationId(),
//                request.getProfessorId(),
//                hall
//        );
//
//        bookingRepository.save(newBooking);
//
//        return ResponseEntity.ok("Success: Hall '" + hall.getHallName() +
//                "' booked successfully by Professor " + request.getProfessorId());
//    }


    // =================================================================
    // SECTION 5: DTOs (Data Transfer Objects)
    // =================================================================

    // DTO for Booking a Hall
    public static class ProfessorBookingRequest {
        private String professorId;
        private String hallName;
        private Date start;
        private Date end;
        private String purpose;
        private long reservationId;

        // Getters & Setters
        public String getProfessorId() { return professorId; }
        public void setProfessorId(String professorId) { this.professorId = professorId; }
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
    }

    // DTO for Final Course Grading
    public static class FinalGradeRequest {
        private String studentId;
        private String courseName;
        private double grade;
        private String semester;

        // Getters & Setters
        public String getStudentId() { return studentId; }
        public void setStudentId(String studentId) { this.studentId = studentId; }
        public String getCourseName() { return courseName; }
        public void setCourseName(String courseName) { this.courseName = courseName; }
        public double getGrade() { return grade; }
        public void setGrade(double grade) { this.grade = grade; }
        public String getSemester() { return semester; }
        public void setSemester(String semester) { this.semester = semester; }
    }

    // DTO for Assignment Grading
    public static class AssignmentGradeRequest {
        private Long assignmentId;
        private String studentId;
        private Double score;
        private String feedback;

        // Getters & Setters
        public Long getAssignmentId() { return assignmentId; }
        public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }
        public String getStudentId() { return studentId; }
        public void setStudentId(String studentId) { this.studentId = studentId; }
        public Double getScore() { return score; }
        public void setScore(Double score) { this.score = score; }
        public String getFeedback() { return feedback; }
        public void setFeedback(String feedback) { this.feedback = feedback; }
    }
}