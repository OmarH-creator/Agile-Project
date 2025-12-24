package com.university.backend.controllers;

import com.university.backend.entity.*;
import com.university.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private HallRepository hallRepository;

    @Autowired
    private CourseRepository courseRepository; // ADDED

    @Autowired
    private MajorRepository majorRepository; // ADDED


    @Autowired
    private BookingRepository bookingRepository; // ADDED

    @GetMapping("/{email}")
    public ResponseEntity<String> fetchAdminByEmail(@PathVariable String email) {
        Optional<Admin> admin = adminRepository.findByEmail(email);

        if (admin.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Admin not found");
        }
        return ResponseEntity.ok(admin.get().getName());

    }
    // --- Student Management ---

    // CREATE STUDENT
    @PostMapping("/students")
    public ResponseEntity<?> createStudentRecord(@RequestBody Map<String, Object> payload) {
        String studentId = (String) payload.get("studentId");

        // 1. Check if student already exists
        if (studentRepository.existsByStudentId(studentId)) {
            return ResponseEntity.badRequest().body("Student with this ID already exists.");
        }

        try {
            // 2. Create new Student object and map simple fields
            Student student = new Student();
            student.setStudentId(studentId);
            student.setName((String) payload.get("name"));
            student.setEmail((String) payload.get("email"));
            student.setPhone((String) payload.get("phone"));
            student.setAddress((String) payload.get("address"));
            student.setMilitaryStatus((String) payload.get("militaryStatus"));

//            // Handle Numbers safely (JSON numbers can be Integer or Double)
//            if (payload.get("gradYear") != null)
//                student.setGradYear(((Number) payload.get("gradYear")).intValue());
//
//            if (payload.get("completedHours") != null)
//                student.setCompletedHours(((Number) payload.get("completedHours")).intValue());
//
//            if (payload.get("fees") != null)
//                student.setFees(((Number) payload.get("fees")).doubleValue());
//
//            if (payload.get("gpa") != null)
//                student.setGpa(((Number) payload.get("gpa")).doubleValue());

            // 3. THE FIX: Look up the Major manually
            String majorId = (String) payload.get("majorId");
            if (majorId != null) {
                Major major = majorRepository.findById(majorId)
                        .orElseThrow(() -> new RuntimeException("Major not found: " + majorId));
                student.setMajor(major); // Set the relationship
            } else {
                return ResponseEntity.badRequest().body("Major ID is required.");
            }

            // 4. Save
            studentRepository.save(student);
            return ResponseEntity.ok("Student created successfully.");

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error saving student: " + e.getMessage());
        }
    }

    // UPDATE STUDENT
    @PutMapping("/students/{id}")
    public ResponseEntity<?> updateStudentRecord(@PathVariable String id, @RequestBody Map<String, Object> payload) {
        // 1. Find existing student
        Optional<Student> studentOpt = studentRepository.findByStudentId(id);
        if (studentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Student student = studentOpt.get();

        try {
            // 2. Update simple fields
            if (payload.containsKey("name")) student.setName((String) payload.get("name"));
            //Checks Email uniqueness
            if (payload.containsKey("email")) {
                String newEmail = (String) payload.get("email");
                Optional<Student> emailOwner = studentRepository.findByEmail(newEmail);

                // If a student exists with this email, AND their ID is not the ID of the student we are currently editing
                if (emailOwner.isPresent() && !emailOwner.get().getStudentId().equals(id)) {
                    return ResponseEntity.badRequest().body("Error: The email '" + newEmail + "' is already used by another student.");
                }
                student.setEmail(newEmail);
            }
            // Check Phone Uniqueness
            if (payload.containsKey("phone")) {
                String newPhone = (String) payload.get("phone");
                Optional<Student> phoneOwner = studentRepository.findByPhone(newPhone);

                // If phone exists AND belongs to someone else (different ID)
                if (phoneOwner.isPresent() && !phoneOwner.get().getStudentId().equals(id)) {
                    return ResponseEntity.badRequest().body("Error: The phone number '" + newPhone + "' is already used by another student.");
                }
                student.setPhone(newPhone);
            }
            if (payload.containsKey("address")) student.setAddress((String) payload.get("address"));
            if (payload.containsKey("militaryStatus")) student.setMilitaryStatus((String) payload.get("militaryStatus"));
//            if (payload.containsKey("status")) student.setStatus((String) payload.get("status"));

//            if (payload.containsKey("gradYear"))
//                student.setGradYear(((Number) payload.get("gradYear")).intValue());
//            if (payload.containsKey("completedHours"))
//                student.setCompletedHours(((Number) payload.get("completedHours")).intValue());
//            if (payload.containsKey("fees"))
//                student.setFees(((Number) payload.get("fees")).doubleValue());
//            if (payload.containsKey("gpa"))
//                student.setGpa(((Number) payload.get("gpa")).doubleValue());

            // 3. Update Major if it changed
            if (payload.containsKey("majorId")) {
                String newMajorId = (String) payload.get("majorId");
                Major major = majorRepository.findById(newMajorId)
                        .orElseThrow(() -> new RuntimeException("Major not found: " + newMajorId));
                student.setMajor(major);
            }

            // 4. Save updates
            studentRepository.save(student);
            return ResponseEntity.ok("Student updated successfully.");

        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // This is the "Safety Net" catch block
            // If we missed a check above, this catches the ugly DB error and makes it readable
            return ResponseEntity.badRequest().body("Error: Duplicate entry detected. Please check Email, Phone, or ID.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("System Error: " + e.getMessage());
        }
    }
    @GetMapping("/students/{studentId}")
    public ResponseEntity<?> getStudent(@PathVariable String studentId) {
        Optional<Student> student = studentRepository.findByStudentId(studentId);
        if (student.isPresent()) {
            return ResponseEntity.ok(student.get());
        }
        return ResponseEntity.status(404).body("Student not found.");
    }

    // GET ALL STUDENTS (With Pagination)
    // Usage: GET /api/admin/students?page=0&size=10
    @GetMapping("/students")
    public ResponseEntity<Page<Student>> getAllStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "studentId") String sortBy,
            @RequestParam(required = false) String search) { // Optional Search PARAMETER

        // 1. Create a Pageable object (Page number, Size per page, Sorting)
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        // If search is present, filter by ID Prefix. Otherwise, return all.
        if (search != null && !search.trim().isEmpty()) {
            return ResponseEntity.ok(studentRepository.findByStudentIdStartingWith(search, pageable));
        } else {
            return ResponseEntity.ok(studentRepository.findAll(pageable));
        }
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

    // GET ALL PROFESSORS (With Pagination and Search)
    // Usage: GET /api/admin/professors?page=0&size=10&search=P-1
    @GetMapping("/professors")
    public ResponseEntity<Page<Professor>> getAllProfessors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "professorId") String sortBy,
            @RequestParam(required = false) String search) {

        // 1. Create Pageable object
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));

        // 2. Filter or Return All
        if (search != null && !search.trim().isEmpty()) {
            // Searches for professors whose ID starts with the search string
            return ResponseEntity.ok(professorRepository.findByProfessorIdStartingWith(search, pageable));

            // Note: If you decided to search by Name instead (in Step 1), use this line:
            // return ResponseEntity.ok(professorRepository.findByProfessorNameContainingIgnoreCase(search, pageable));
        } else {
            return ResponseEntity.ok(professorRepository.findAll(pageable));
        }
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

    @GetMapping("/halls")
    public ResponseEntity<?> getAllHalls() {
        // .findAll() is provided automatically by JpaRepository
        return ResponseEntity.ok(hallRepository.findAll());
    }

    @GetMapping("/halls/{hallName}")
    public ResponseEntity<?> getHall(@PathVariable String hallName) {
        Optional<Hall> hallOpt = hallRepository.findByHallName(hallName);

        if (hallOpt.isPresent()) {
            return ResponseEntity.ok(hallOpt.get());
        }
        return ResponseEntity.status(404).body("Hall '" + hallName + "' not found.");
    }

    @PostMapping("/halls")
    public ResponseEntity<String> addHall(@RequestBody Hall hall) {
        if (hallRepository.findByHallName(hall.getHallName()).isPresent()) {
            return ResponseEntity.badRequest().body("Hall with this name already exists.");
        }
        hallRepository.save(hall);
        return ResponseEntity.ok("Hall added successfully.");
    }

    // --- Get All Bookings ---
    @GetMapping("/bookings")
    public ResponseEntity<List<Booking>> getAllBookings() {
        try {
            List<Booking> bookings = bookingRepository.findAll();
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/halls/book")
    public ResponseEntity<String> bookHall(@RequestBody BookingRequest request) {

        // 1. Find the Hall Object using the String name from Frontend
        Optional<Hall> hallOpt = hallRepository.findByHallName(request.getHallName());

        if (hallOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Hall not found: " + request.getHallName());
        }

        // 2. Check Conflicts
        boolean hasConflict = bookingRepository.existsByHallAndOverlap(
                request.getHallName(),
                request.getStart(),
                request.getEnd()
        );

        if (hasConflict) {
//            return ResponseEntity.badRequest().body("Booking failed: Time conflict.");
        }

        try {
            Booking newBooking = new Booking();

            // 3. Set the fields (Matches the new Entity names)
            newBooking.setStartTime(request.getStart());
            newBooking.setEndTime(request.getEnd());
            newBooking.setPurpose(request.getPurpose());
            newBooking.setStaffId(String.valueOf(request.getStaffId())); // Ensure String format

            // 4. CRITICAL: Set the Relationship Object
            newBooking.setHall(hallOpt.get());

            // 5. CRITICAL: Do NOT set reservationId (Let DB Auto-Generate)

            bookingRepository.save(newBooking);

            return ResponseEntity.ok("Hall booked successfully.");

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error saving booking: " + e.getMessage());
        }
    }

    // ... inside AdminController class ...

    // --- UPDATE BOOKING ---
    @PutMapping("/bookings/{id}")
    public ResponseEntity<String> updateBooking(@PathVariable Long id, @RequestBody BookingRequest request) {

        // 1. Find the existing booking by its ID
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + id));

        // 2. Find the Hall by Name (e.g. "219") to ensure it exists
        Optional<Hall> hallOpt = hallRepository.findByHallName(request.getHallName());
        if (hallOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Hall '" + request.getHallName() + "' not found.");
        }

        // 3. Check for conflicts (Excluding this booking's own ID)
        boolean hasConflict = bookingRepository.existsByHallAndOverlapExcludingId(
                request.getHallName(),
                request.getStart(),
                request.getEnd(),
                id
        );

        if (hasConflict) {
//            return ResponseEntity.badRequest().body("Update failed: Time conflict in hall " + request.getHallName());
        }

        // 4. Apply Updates
        try {
            // Update the relationship
            booking.setHall(hallOpt.get());

            // Update fields
            booking.setStartTime(request.getStart());
            booking.setEndTime(request.getEnd());
            booking.setPurpose(request.getPurpose());

            // Update staffId if present
            if (request.getStaffId() != null) {
                booking.setStaffId(String.valueOf(request.getStaffId()));
            }

            bookingRepository.save(booking);
            return ResponseEntity.ok("Booking updated successfully.");

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error updating booking: " + e.getMessage());
        }
    }

    // --- DELETE BOOKING ---
    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<String> deleteBooking(@PathVariable Long id) {
        if (!bookingRepository.existsById(id)) {
            return ResponseEntity.status(404).body("Booking not found with ID: " + id);
        }
        try {
            bookingRepository.deleteById(id);
            return ResponseEntity.ok("Booking deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error deleting booking: " + e.getMessage());
        }
    }

    // --- UPDATE HALL (Fixed Identifier Error) ---
    @PutMapping("/halls/{originalName}")
    public ResponseEntity<String> updateHall(
            @PathVariable String originalName,
            @RequestBody Map<String, Object> payload) {

        // 1. Find the existing Hall by the name in the URL
        Optional<Hall> hallOpt = hallRepository.findByHallName(originalName);

        if (hallOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Hall '" + originalName + "' not found.");
        }

        Hall hall = hallOpt.get();

        // 2. Update Capacity (Only if provided)
        if (payload.get("capacity") != null) {
            // Handle integer conversion safely
            hall.setCapacity(((Number) payload.get("capacity")).intValue());
        }

        // 3. Update Name (Handle Renaming)
        String newName = (String) payload.get("hallName"); // Ensure frontend sends "hallName" (or "name" depending on your map)

        // Fallback if frontend sends "name" instead of "hallName"
        if (newName == null) {
            newName = (String) payload.get("name");
        }

        if (newName != null && !newName.equals(hall.getHallName())) {
            // Check if the NEW name is already taken by a DIFFERENT hall
            if (hallRepository.existsByHallName(newName)) {
                return ResponseEntity.badRequest().body("Name '" + newName + "' is already taken.");
            }
            hall.setHallName(newName);
        }

        // CRITICAL: Do NOT call hall.setHallId(...) or hall.setId(...) here!
        // Leave the ID exactly as it was loaded from the database.

        hallRepository.save(hall);
        return ResponseEntity.ok("Hall updated successfully.");
    }
    // --- Course Management --- ADDED THIS SECTION
    // this is related to the course managment
    @PostMapping("/courses")
    public ResponseEntity<String> addCourse(@RequestBody Course course) {
        if (courseRepository.existsByCourseCode(course.getCourseCode())) {
            return ResponseEntity.badRequest().body("Course with this code already exists.");
        }
        courseRepository.save(course);
        return ResponseEntity.ok("Course added successfully.");
    }

    @PutMapping("/courses/{courseCode}")
    public ResponseEntity<String> updateCourse(@PathVariable String courseCode, @RequestBody Course updatedCourse) {
        Optional<Course> existingCourseOpt = courseRepository.findByCourseCode(courseCode);

        if (existingCourseOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Course not found.");
        }

        Course existingCourse = existingCourseOpt.get();

        // Update fields if provided in the request
        if (updatedCourse.getCourseName() != null) {
            existingCourse.setCourseName(updatedCourse.getCourseName());
        }
        if (updatedCourse.getCreditHours() > 0) {
            existingCourse.setCreditHours(updatedCourse.getCreditHours());
        }

        courseRepository.save(existingCourse);
        return ResponseEntity.ok("Course updated successfully.");
    }

    @DeleteMapping("/courses/{courseCode}")
    public ResponseEntity<String> removeCourse(@PathVariable String courseCode) {
        Optional<Course> courseOpt = courseRepository.findByCourseCode(courseCode);

        if (courseOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Course not found.");
        }

        courseRepository.delete(courseOpt.get());
        return ResponseEntity.ok("Course removed successfully.");
    }

    @GetMapping("/courses/{courseCode}")
    public ResponseEntity<?> getCourse(@PathVariable String courseCode) {
        Optional<Course> course = courseRepository.findByCourseCode(courseCode);
        if (course.isPresent()) {
            return ResponseEntity.ok(course.get());
        }
        return ResponseEntity.status(404).body("Course not found.");
    }

    // Add these methods to the Course Management section in your AdminController

    @GetMapping("/courses")
    public ResponseEntity<?> getAllCourses() {
        return ResponseEntity.ok(courseRepository.findAll());
    }

    @GetMapping("/courses/search")
    public ResponseEntity<?> searchCourses(@RequestParam(required = false) String name) {
        if (name != null && !name.trim().isEmpty()) {
            // Search by course name containing the search term (case-insensitive)
            return ResponseEntity.ok(courseRepository.findByCourseNameContainingIgnoreCase(name));
        } else {
            // If no search term, return all courses
            return ResponseEntity.ok(courseRepository.findAll());
        }
    }

    @GetMapping("/courses/filter/credits")
    public ResponseEntity<?> getCoursesByCreditRange(
            @RequestParam(required = false) Integer minCredits,
            @RequestParam(required = false) Integer maxCredits) {

        if (minCredits != null && maxCredits != null) {
            return ResponseEntity.ok(courseRepository.findByCreditHoursBetween(minCredits, maxCredits));
        } else if (minCredits != null) {
            return ResponseEntity.ok(courseRepository.findByCreditHoursGreaterThanEqual(minCredits));
        } else if (maxCredits != null) {
            return ResponseEntity.ok(courseRepository.findByCreditHoursLessThanEqual(maxCredits));
        } else {
            return ResponseEntity.ok(courseRepository.findAll());
        }
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