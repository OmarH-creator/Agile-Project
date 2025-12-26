package com.university.backend.controllers;

import com.university.backend.dto.HallResponseDTO;
import com.university.backend.entity.*;
import com.university.backend.entity.Hall.Hall;
import com.university.backend.entity.Hall.HallAttribute;
import com.university.backend.entity.Hall.HallValue;
import com.university.backend.entity.StaffRequests.StaffRequest;
import com.university.backend.repository.*;
import com.university.backend.services.HallService;
import org.springframework.beans.factory.annotation.Autowired;
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
    private StudentRepository studentRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private HallRepository hallRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private MajorRepository majorRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private final HallService hallService;

    public AdminController(HallService hallService) {
        this.hallService = hallService;
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
            if (payload.containsKey("name"))
                student.setName((String) payload.get("name"));
            // Checks Email uniqueness
            if (payload.containsKey("email")) {
                String newEmail = (String) payload.get("email");
                Optional<Student> emailOwner = studentRepository.findByEmail(newEmail);

                // If a student exists with this email, AND their ID is not the ID of the
                // student we are currently editing
                if (emailOwner.isPresent() && !emailOwner.get().getStudentId().equals(id)) {
                    return ResponseEntity.badRequest()
                            .body("Error: The email '" + newEmail + "' is already used by another student.");
                }
                student.setEmail(newEmail);
            }
            // Check Phone Uniqueness
            if (payload.containsKey("phone")) {
                String newPhone = (String) payload.get("phone");
                Optional<Student> phoneOwner = studentRepository.findByPhone(newPhone);

                // If phone exists AND belongs to someone else (different ID)
                if (phoneOwner.isPresent() && !phoneOwner.get().getStudentId().equals(id)) {
                    return ResponseEntity.badRequest()
                            .body("Error: The phone number '" + newPhone + "' is already used by another student.");
                }
                student.setPhone(newPhone);
            }
            if (payload.containsKey("address"))
                student.setAddress((String) payload.get("address"));
            if (payload.containsKey("militaryStatus"))
                student.setMilitaryStatus((String) payload.get("militaryStatus"));

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
            return ResponseEntity.badRequest()
                    .body("Error: Duplicate entry detected. Please check Email, Phone, or ID.");
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
    @GetMapping("/students")
    public ResponseEntity<Page<Student>> getAllStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "studentId") String sortBy,
            @RequestParam(required = false) String search) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
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

    @DeleteMapping("/students/{studentId}")
    public ResponseEntity<String> deleteStudentRecord(@PathVariable String studentId) {
        if (studentRepository.existsByStudentId(studentId)) {
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

    @GetMapping("/professors")
    public ResponseEntity<Page<Professor>> getAllProfessors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "professorId") String sortBy,
            @RequestParam(required = false) String search) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));

        if (search != null && !search.trim().isEmpty()) {
            return ResponseEntity.ok(professorRepository.findByProfessorIdStartingWith(search, pageable));
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

    @PutMapping("/professors/{professorId}")
    public ResponseEntity<String> updateProfessorRecord(@PathVariable String professorId,
            @RequestBody Professor updatedProfessor) {
        Optional<Professor> profOpt = professorRepository.findByProfessorId(professorId);

        if (profOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Error: Professor with ID " + professorId + " not found.");
        }

        Professor existingProfessor = profOpt.get();

        existingProfessor.setProfessorName(updatedProfessor.getProfessorName());
        existingProfessor.setProfessorEmail(updatedProfessor.getProfessorEmail());
        existingProfessor.setProfessorDepartment(updatedProfessor.getProfessorDepartment());

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
        professorRepository.save(targetProf);

        return ResponseEntity.ok("Success: Course '" + courseName + "' assigned to " + targetProf.getProfessorName());
    }

    @DeleteMapping("/professors/{professorId}")
    public ResponseEntity<String> deleteProfessorRecord(@PathVariable String professorId) {
        if (professorRepository.existsByProfessorId(professorId)) {
            professorRepository.deleteByProfessorId(professorId);
            return ResponseEntity.ok("Professor with ID " + professorId + " removed successfully.");
        }
        return ResponseEntity.status(404).body("Professor with ID " + professorId + " not found.");
    }

    // --- Hall Management (UPDATED FOR EAV) ---

    @GetMapping("/halls")
    public ResponseEntity<?> getAllHalls() {
        return ResponseEntity.ok(hallRepository.findAll());
    }

    @GetMapping("/halls/{id}")
    public ResponseEntity<HallResponseDTO> getHall(@PathVariable Long id) {
        HallResponseDTO hallDto = hallService.getHallById(id);
        return ResponseEntity.ok(hallDto);
    }

    // UPDATED: Use HallService to create Hall with EAV attributes
    @PostMapping("/halls")
    public ResponseEntity<HallResponseDTO> addHall(@RequestBody Map<String, Object> payload) {
        // Check if hall name exists (using the custom EAV lookup)
        String hallName = (String) payload.get("Hall_Name");
        if (hallRepository.findByName(hallName).isPresent()) {
            // Return conflict or bad request
            // return ResponseEntity.badRequest().body("Hall with this name already
            // exists.");
            // For now, let's just proceed or throw exception.
            // Ideally, HallService should handle this check.
        }

        HallResponseDTO newHall = hallService.createHall(payload);
        return ResponseEntity.ok(newHall);
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

        // 1. Find the Hall Object using the String name from Frontend (EAV Lookup)
        Optional<Hall> hallOpt = hallRepository.findByName(request.getHallName());

        if (hallOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Hall not found: " + request.getHallName());
        }

        // 2. Check Conflicts
        boolean hasConflict = bookingRepository.existsByHallAndOverlap(
                request.getHallName(),
                request.getStart(),
                request.getEnd());

        if (hasConflict) {
            return ResponseEntity.badRequest().body("Booking failed: Time conflict.");
        }

        try {
            Booking newBooking = new Booking();

            newBooking.setStartTime(request.getStart());
            newBooking.setEndTime(request.getEnd());
            newBooking.setPurpose(request.getPurpose());
            newBooking.setStaffId(String.valueOf(request.getStaffId()));

            newBooking.setHall(hallOpt.get());

            bookingRepository.save(newBooking);

            return ResponseEntity.ok("Hall booked successfully.");

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error saving booking: " + e.getMessage());
        }
    }

    // --- UPDATE BOOKING ---
    @PutMapping("/bookings/{id}")
    public ResponseEntity<String> updateBooking(@PathVariable Long id, @RequestBody BookingRequest request) {

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + id));

        // EAV Lookup
        Optional<Hall> hallOpt = hallRepository.findByName(request.getHallName());
        if (hallOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Hall '" + request.getHallName() + "' not found.");
        }

        boolean hasConflict = bookingRepository.existsByHallAndOverlapExcludingId(
                request.getHallName(),
                request.getStart(),
                request.getEnd(),
                id);

        if (hasConflict) {
            return ResponseEntity.badRequest().body("Update failed: Time conflict in hall " + request.getHallName());
        }

        try {
            booking.setHall(hallOpt.get());
            booking.setStartTime(request.getStart());
            booking.setEndTime(request.getEnd());
            booking.setPurpose(request.getPurpose());

            if (request.getStaffId() != null) {
                booking.setStaffId(String.valueOf(request.getStaffId()));
            }

            bookingRepository.save(booking);
            return ResponseEntity.ok("Booking updated successfully.");

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error updating booking: " + e.getMessage());
        }
    }

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

    // --- UPDATE HALL (EAV Style) ---
    @PutMapping("/halls/{originalName}")
    public ResponseEntity<String> updateHall(
            @PathVariable String originalName,
            @RequestBody Map<String, Object> payload) {

        // 1. Find existing Hall by Name (EAV Lookup)
        Optional<Hall> hallOpt = hallRepository.findByName(originalName);

        if (hallOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Hall '" + originalName + "' not found.");
        }

        Hall hall = hallOpt.get();

        // 2. Update EAV Attributes (Dynamic Loop)
        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // Skip static keys or keys unrelated to EAV
            if (key.equals("Hall_Name") || key.equals("hallName")) {
                continue;
            }

            updateHallValue(hall, key, value);
        }

        // 3. Update Name (Static Field)
        String newName = (String) payload.get("Hall_Name");
        if (newName != null && !newName.equals(hall.getHallName())) {
            // Check conflict
            if (hallRepository.findByName(newName).isPresent()) {
                return ResponseEntity.badRequest().body("Name '" + newName + "' is already taken.");
            }
            hall.setHallName(newName);
            // Also update the "Name" attribute for consistency if you are storing it twice
            updateHallValue(hall, "Name", newName);
        }

        hallRepository.save(hall);
        return ResponseEntity.ok("Hall updated successfully.");
    }

    // Helper to update EAV values
    private void updateHallValue(Hall hall, String key, Object value) {
        Optional<HallAttribute> attrOpt = hall.getAttributes().stream()
                .filter(a -> a.getAttributeName().equals(key))
                .findFirst();

        if (attrOpt.isPresent()) {
            HallAttribute attr = attrOpt.get();
            // Check if value exists, if so update it, else create new
            // Simplified: Just adding a new value row for now or finding existing one
            // Ideally you iterate values to find the one matching this attribute
            Optional<HallValue> existingVal = hall.getValues().stream()
                    .filter(v -> v.getAttribute().getAttributeName().equals(key))
                    .findFirst();

            HallValue val;
            if (existingVal.isPresent()) {
                val = existingVal.get();
            } else {
                val = new HallValue();
                val.setHall(hall);
                val.setAttribute(attr);
                hall.getValues().add(val);
            }

            if (value instanceof Integer)
                val.setValInt((Integer) value);
            else if (value instanceof String)
                val.setValString((String) value);
            // Add other types as needed
        }
    }

    // --- Course Management ---

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

    @GetMapping("/courses")
    public ResponseEntity<?> getAllCourses() {
        return ResponseEntity.ok(courseRepository.findAll());
    }

    @GetMapping("/courses/search")
    public ResponseEntity<?> searchCourses(@RequestParam(required = false) String name) {
        if (name != null && !name.trim().isEmpty()) {
            return ResponseEntity.ok(courseRepository.findByCourseNameContainingIgnoreCase(name));
        } else {
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
        // EAV Lookup
        Optional<Hall> hallOpt = hallRepository.findByName(hallName);
        if (hallOpt.isPresent()) {
            hallRepository.delete(hallOpt.get());
            return ResponseEntity.ok("Hall '" + hallName + "' removed successfully.");
        }
        return ResponseEntity.status(404).body("Hall '" + hallName + "' not found.");
    }

    // --- Request Management ---
    @Autowired
    private StaffRequestRepository staffRequestRepository;

    @GetMapping("/requests")
    public ResponseEntity<?> getAllRequests() {
        try {
            return ResponseEntity.ok(staffRequestRepository.findAll());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error fetching requests: " + e.getMessage());
        }
    }

    @PutMapping("/requests/{id}/approve")
    public ResponseEntity<String> approveRequest(@PathVariable Long id) {
        Optional<StaffRequest> reqOpt = staffRequestRepository.findById(id);
        if (reqOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Request not found.");
        }
        StaffRequest req = reqOpt.get();
        req.setStatus("Approved");
        staffRequestRepository.save(req);
        return ResponseEntity.ok("Request approved.");
    }

    @PutMapping("/requests/{id}/reject")
    public ResponseEntity<String> rejectRequest(@PathVariable Long id) {
        Optional<StaffRequest> reqOpt = staffRequestRepository.findById(id);
        if (reqOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Request not found.");
        }
        StaffRequest req = reqOpt.get();
        req.setStatus("Rejected");
        staffRequestRepository.save(req);
        return ResponseEntity.ok("Request rejected.");
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

        public String getStaffId() {
            return staffId;
        }

        public void setStaffId(String staffId) {
            this.staffId = staffId;
        }
    }
}