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
import java.text.SimpleDateFormat;
import com.university.backend.dto.AssignmentResponseDTO;
import com.university.backend.dto.StaffRequestResponseDTO;
import com.university.backend.services.AssignmentService;
import com.university.backend.services.StaffRequestService;
import com.university.backend.entity.Hall.Hall;
import com.university.backend.entity.Booking;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/professor")
@CrossOrigin(origins = "http://localhost:3000")
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

    @Autowired
    private final AssignmentService assignmentService;

    @Autowired
    private final com.university.backend.services.AssignmentSubmissionService submissionService;

    public ProfessorController(AssignmentService assignmentService,
            com.university.backend.services.AssignmentSubmissionService submissionService) {
        this.assignmentService = assignmentService;
        this.submissionService = submissionService;
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
        // Requires the custom method in StudentRepository:
        // findByCurrentCoursesContaining(String courseName)
        // 1. Fetch the list
        List<Student> students = studentRepository.findByCurrentCoursesContaining(courseName);

        // 2. Return the list directly. Spring Boot handles empty lists automatically
        // (returns [])
        return ResponseEntity.ok(students);
    }

    // =================================================================
    // SECTION 2: GRADING (Final Course Grades)
    // =================================================================

    /**
     * 3. Assign a final grade for a course.
     * This moves the course from 'Current Courses' to 'Completed Courses'
     * (History).
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
            return ResponseEntity.badRequest()
                    .body("Error: Student is not currently enrolled in " + request.getCourseName());
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

    // ------------------------------------------------------------
    // GET: Get all assignments for a specific course
    // URL: http://localhost:8080/api/professor/assignment/{courseId}
    // ------------------------------------------------------------
    @GetMapping("/assignment/{courseId}")
    public ResponseEntity<List<AssignmentResponseDTO>> getAssignmentsByCourse(@PathVariable String courseId) {
        // Query assignments via Service/Repository
        // Note: We need a repo method or service method for this.
        // Assuming assignmentRepository.findByCourse_CourseCode(courseId)

        List<Assignment> assignments = assignmentRepository.findAllByCourseCode(courseId);
        List<AssignmentResponseDTO> dtos = assignments.stream()
                .map(a -> assignmentService.getAssignmentById(a.getId()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // 2. POST: Create Assignment with File Upload (Modified)
    @PostMapping("/create")
    public ResponseEntity<?> createAssignment(
            @RequestParam("payload") String payloadJson,
            @RequestParam(value = "file", required = false) org.springframework.web.multipart.MultipartFile file) {

        System.out.println("DEBUG: Received create assignment request.");
        System.out.println("Payload: " + payloadJson);

        try {
            // 1. Convert Payload String to Map
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> payload = mapper.readValue(payloadJson,
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
                    });

            // 2. Handle File Upload
            if (file != null && !file.isEmpty()) {
                // Save file to a static directory or similar
                String fileName = java.util.UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                String uploadDir = "uploads/assignments/";
                java.io.File directory = new java.io.File(uploadDir);
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                java.nio.file.Path filePath = java.nio.file.Paths.get(uploadDir + fileName);
                java.nio.file.Files.copy(file.getInputStream(), filePath,
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                // Add the file path to the EAV payload
                payload.put("Attachment_Url", "/uploads/assignments/" + fileName);
            }

            // 3. Call Service
            AssignmentResponseDTO newAssignment = assignmentService.createAssignment(payload);
            return ResponseEntity.ok(newAssignment);

        } catch (Exception e) {
            e.printStackTrace();
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error creating assignment: " + e.getMessage());
        }
    }

    // 2.1. UPDATE Assignment
    @PostMapping("/update/{id}")
    public ResponseEntity<?> updateAssignment(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        try {
            AssignmentResponseDTO updated = assignmentService.updateAssignment(id, payload);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating assignment: " + e.getMessage());
        }
    }

    /**
     * 2.2. DELETE Assignment
     * Endpoint: DELETE /api/professor/assignment/{id}
     */
    @DeleteMapping("/assignment/{id}")
    public ResponseEntity<?> deleteAssignment(@PathVariable Long id) {
        try {
            assignmentService.deleteAssignment(id);
            return ResponseEntity.ok("Assignment deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting assignment: " + e.getMessage());
        }
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
        // But we need to find by Assignment AND Student, which the current repo method
        // doesn't do directly.
        // For now, let's assume we are creating a new one or finding it manually if
        // needed.
        // Ideally, you'd have submissionRepository.findByAssignmentAndStudent(...)

        // Simplified logic: Create new submission or update if we had a way to find it
        // by student+assignment
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

            if (value instanceof Double)
                val.setValDouble((Double) value);
            else if (value instanceof String)
                val.setValString((String) value);

            submission.getValues().add(val);
        }
    }

    @GetMapping("/assignment/{assignmentId}/submissions")
    public ResponseEntity<?> getAssignmentSubmissions(
            @PathVariable Long assignmentId) {
        try {
            System.out.println("DEBUG: Fetching submissions for assignment " + assignmentId);
            if (submissionService == null) {
                return ResponseEntity.status(500).body("CRITICAL: submissionService is null!");
            }
            List<com.university.backend.dto.AssignmentSubmissionResponseDTO> submissions = submissionService
                    .getSubmissionsByAssignment(assignmentId);
            return ResponseEntity.ok(submissions);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // =================================================================
    // SECTION 6: STAFF REQUESTS (EAV Integration)
    // =================================================================

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StaffRequestService staffRequestService;

    /**
     * Get all requests made by this professor.
     * Uses Email to link Professor -> User -> StaffRequests
     */
    @GetMapping("/{professorId}/requests")
    public ResponseEntity<?> getProfessorRequests(@PathVariable String professorId) {
        // 1. Find Professor
        Optional<Professor> profOpt = professorRepository.findByProfessorId(professorId);
        if (profOpt.isEmpty())
            return ResponseEntity.status(404).body("Professor not found");

        // 2. Find User by Email
        Optional<User> userOpt = userRepository.findByEmail(profOpt.get().getProfessorEmail());
        if (userOpt.isEmpty())
            return ResponseEntity.status(404).body("Linked User account not found for this professor.");

        // 3. Fetch Requests
        List<StaffRequestResponseDTO> requests = staffRequestService.getRequestsByRequester(userOpt.get().getId());
        return ResponseEntity.ok(requests);
    }

    /**
     * Create a new Staff Request.
     * Expects "professorId" in payload to link to User.
     */
    @PostMapping("/request/create")
    public ResponseEntity<?> createProfessorRequest(@RequestBody Map<String, Object> payload) {
        String professorId = (String) payload.get("professorId");
        if (professorId == null)
            return ResponseEntity.badRequest().body("professorId is required");

        // 1. Find Professor & User
        Optional<Professor> profOpt = professorRepository.findByProfessorId(professorId);
        if (profOpt.isEmpty())
            return ResponseEntity.status(404).body("Professor not found");

        Optional<User> userOpt = userRepository.findByEmail(profOpt.get().getProfessorEmail());
        if (userOpt.isEmpty())
            return ResponseEntity.status(404).body("Linked User account not found.");

        // 2. Inject Requester_Id into payload
        payload.put("Requester_Id", userOpt.get().getId());

        // 3. Create Request
        try {
            StaffRequestResponseDTO response = staffRequestService.createRequest(payload);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating request: " + e.getMessage());
        }
    }

    // =================================================================
    // SECTION 4: HALL BOOKING (Enhanced)
    // =================================================================

    /**
     * Book a hall for a specific Day of Week (Next occurrence).
     * Endpoint: POST /api/professor/halls/book
     */
    @PostMapping("/halls/book")
    public ResponseEntity<?> bookHallByProfessor(@RequestBody Map<String, String> request) {
        System.out.println("DEBUG: Booking Request Payload: " + request); // Debug log

        try {
            String professorId = request.get("professorId");
            String rawHallName = request.get("hallName");
            String dayOfWeek = request.get("dayOfWeek");
            String startTimeStr = request.get("startTime"); // HH:mm
            String endTimeStr = request.get("endTime"); // HH:mm

            // 1. Validation: Null Checks
            if (professorId == null || rawHallName == null || dayOfWeek == null || startTimeStr == null
                    || endTimeStr == null) {
                return ResponseEntity.badRequest()
                        .body("Error: Missing required fields. Received: " + request.keySet());
            }

            // 1.1 Handle "HallName - Building" format from Frontend dropdown
            String hallName = rawHallName;
            if (rawHallName.contains(" - ")) {
                hallName = rawHallName.split(" - ")[0].trim();
            }

            // 2. Validate Professor
            if (!professorRepository.existsByProfessorId(professorId)) {
                return ResponseEntity.status(401).body("Error: Professor not found with ID: " + professorId);
            }

            // 3. Find Hall (Handle Duplicates)
            java.util.List<com.university.backend.entity.Hall.Hall> halls = hallRepository.findByHallName(hallName);
            if (halls.isEmpty()) {
                // Try finding by EAV name if static fails
                halls = hallRepository.findByName(hallName);
                if (halls.isEmpty()) {
                    return ResponseEntity.status(404).body("Hall '" + hallName + "' not found.");
                }
            }
            // If duplicates exist, we take the first one (or log a warning)
            com.university.backend.entity.Hall.Hall hall = halls.get(0);

            // 4. Calculate Date/Time
            Date start;
            Date end;
            try {
                start = calculateNextDate(dayOfWeek, startTimeStr);
                end = calculateNextDate(dayOfWeek, endTimeStr);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Error parsing date/time: " + e.getMessage());
            }

            if (start.after(end)) {
                return ResponseEntity.badRequest().body("Error: Start time must be before end time.");
            }

            // 5. Conflict Check
            List<Booking> hallBookings = bookingRepository.findAll();
            Date finalStart = start;
            Date finalEnd = end;
            boolean conflict = hallBookings.stream()
                    .filter(b -> b.getHall() != null && b.getHall().getId().equals(hall.getId()))
                    .anyMatch(b -> finalStart.before(b.getEndTime()) && finalEnd.after(b.getStartTime()));

            if (conflict) {
                return ResponseEntity.badRequest().body("Error: Hall is already booked for this time.");
            }

            // 6. Create Booking
            Booking newBooking = new Booking();
            newBooking.setStaffId(professorId);
            newBooking.setHall(hall);
            newBooking.setStartTime(start);
            newBooking.setEndTime(end);
            newBooking.setPurpose("Professor Booking");

            bookingRepository.save(newBooking);

            return ResponseEntity.ok("Success: Hall booked for " + start.toString());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error processing booking: " + e.getMessage());
        }
    }

    /**
     * 11. Get all Halls for selection
     * Endpoint: GET /api/professor/halls
     */
    @GetMapping("/halls")
    public ResponseEntity<List<Hall>> getAllHalls() {
        return ResponseEntity.ok(hallRepository.findAll());
    }

    private Date calculateNextDate(String dayOfWeek, String timeHHMM) throws java.text.ParseException {
        // Map day string to Calendar constant
        int targetDay;
        switch (dayOfWeek.toLowerCase()) {
            case "sunday":
                targetDay = java.util.Calendar.SUNDAY;
                break;
            case "monday":
                targetDay = java.util.Calendar.MONDAY;
                break;
            case "tuesday":
                targetDay = java.util.Calendar.TUESDAY;
                break;
            case "wednesday":
                targetDay = java.util.Calendar.WEDNESDAY;
                break;
            case "thursday":
                targetDay = java.util.Calendar.THURSDAY;
                break;
            case "friday":
                targetDay = java.util.Calendar.FRIDAY;
                break;
            case "saturday":
                targetDay = java.util.Calendar.SATURDAY;
                break;
            default:
                throw new IllegalArgumentException("Invalid day of week: " + dayOfWeek);
        }

        java.util.Calendar cal = java.util.Calendar.getInstance();
        int currentDay = cal.get(java.util.Calendar.DAY_OF_WEEK);

        // Calculate days to add (If today is Monday and we want Monday, assume next
        // Monday? Or today?
        // Let's assume: if today is the day, book for today IF time hasn't passed,
        // otherwise next week.
        // For simplicity: Always book upcoming. If today matches, check time?
        // Let's just logic: (target - current + 7) % 7. If 0, it means today.

        int daysToAdd = (targetDay - currentDay + 7) % 7;
        if (daysToAdd == 0) {
            // If it's today, check if time passed? Or just book today.
            // Requirement didn't specify, we'll default to Today.
        }
        cal.add(java.util.Calendar.DATE, daysToAdd);

        // Set Time
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        Date time = timeFormat.parse(timeHHMM);
        java.util.Calendar timeCal = java.util.Calendar.getInstance();
        timeCal.setTime(time);

        cal.set(java.util.Calendar.HOUR_OF_DAY, timeCal.get(java.util.Calendar.HOUR_OF_DAY));
        cal.set(java.util.Calendar.MINUTE, timeCal.get(java.util.Calendar.MINUTE));
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

    // =================================================================
    // SECTION 7: DTOs
    // =================================================================

    // ... Existing DTOs ...

    // DTO for Booking a Hall
    public static class ProfessorBookingRequest {
        private String professorId;
        private String hallName;
        private Date start;
        private Date end;
        private String purpose;
        private long reservationId;

        // Getters & Setters
        public String getProfessorId() {
            return professorId;
        }

        public void setProfessorId(String professorId) {
            this.professorId = professorId;
        }

        public String getHallName() {
            return hallName;
        }

        public void setHallName(String hallName) {
            this.hallName = hallName;
        }

        public Date getStart() {
            return start;
        }

        public void setStart(Date start) {
            this.start = start;
        }

        public Date getEnd() {
            return end;
        }

        public void setEnd(Date end) {
            this.end = end;
        }

        public String getPurpose() {
            return purpose;
        }

        public void setPurpose(String purpose) {
            this.purpose = purpose;
        }

        public long getReservationId() {
            return reservationId;
        }

        public void setReservationId(long reservationId) {
            this.reservationId = reservationId;
        }
    }

    // DTO for Final Course Grading
    public static class FinalGradeRequest {
        private String studentId;
        private String courseName;
        private double grade;
        private String semester;

        // Getters & Setters
        public String getStudentId() {
            return studentId;
        }

        public void setStudentId(String studentId) {
            this.studentId = studentId;
        }

        public String getCourseName() {
            return courseName;
        }

        public void setCourseName(String courseName) {
            this.courseName = courseName;
        }

        public double getGrade() {
            return grade;
        }

        public void setGrade(double grade) {
            this.grade = grade;
        }

        public String getSemester() {
            return semester;
        }

        public void setSemester(String semester) {
            this.semester = semester;
        }
    }

    // DTO for Assignment Grading
    public static class AssignmentGradeRequest {
        private Long assignmentId;
        private String studentId;
        private Double score;
        private String feedback;

        // Getters & Setters
        public Long getAssignmentId() {
            return assignmentId;
        }

        public void setAssignmentId(Long assignmentId) {
            this.assignmentId = assignmentId;
        }

        public String getStudentId() {
            return studentId;
        }

        public void setStudentId(String studentId) {
            this.studentId = studentId;
        }

        public Double getScore() {
            return score;
        }

        public void setScore(Double score) {
            this.score = score;
        }

        public String getFeedback() {
            return feedback;
        }

        public void setFeedback(String feedback) {
            this.feedback = feedback;
        }
    }

    // =================================================================
// SECTION 8: PAYMENT TAB (Simple)
// =================================================================

    @GetMapping("/{professorId}/payment")
    public ResponseEntity<?> getProfessorPayment(@PathVariable String professorId) {
        try {
            Optional<Professor> profOpt = professorRepository.findByProfessorId(professorId);
            if (profOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Professor not found");
            }

            Professor professor = profOpt.get();

            // Simple response - just return the payment amount
            Map<String, Object> response = new HashMap<>();
            response.put("payment", professor.getPayment());
            response.put("currency", "USD");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching payment");
        }
    }

    @GetMapping("/course/{courseName}/info")
    public ResponseEntity<?> getProfessorByCourse(@PathVariable String courseName) {
        try {
            Optional<Professor> professorOpt = professorRepository.findByProfessorCoursesContaining(courseName);

            if (professorOpt.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "professorName", "Not Assigned",
                        "professorEmail", "",
                        "message", "No professor assigned to this course"
                ));
            }

            Professor professor = professorOpt.get();
            return ResponseEntity.ok(Map.of(
                    "professorId", professor.getProfessorId(),
                    "professorName", professor.getProfessorName(),
                    "professorEmail", professor.getProfessorEmail(),
                    "department", professor.getProfessorDepartment()
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error fetching professor info: " + e.getMessage());
        }
    }

    /**
     * Bulk fetch professors for multiple courses.
     * Used by Parent Dashboard to get all professors at once.
     * Endpoint: POST /api/professor/courses/professors
     */
    @PostMapping("/courses/professors")
    public ResponseEntity<?> getProfessorsForCourses(@RequestBody Map<String, List<String>> request) {
        try {
            List<String> courseNames = request.get("courses");
            Map<String, Object> response = new HashMap<>();

            for (String courseName : courseNames) {
                Optional<Professor> professorOpt = professorRepository.findByProfessorCoursesContaining(courseName);

                if (professorOpt.isPresent()) {
                    Professor prof = professorOpt.get();
                    response.put(courseName, Map.of(
                            "professorName", prof.getProfessorName(),
                            "professorEmail", prof.getProfessorEmail()
                    ));
                } else {
                    response.put(courseName, Map.of(
                            "professorName", "Not Assigned",
                            "professorEmail", ""
                    ));
                }
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error fetching professors: " + e.getMessage());
        }
    }
}