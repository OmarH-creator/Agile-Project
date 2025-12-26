package com.university.backend.controllers;

import com.university.backend.dto.CourseDTO;
import com.university.backend.entity.*;
import com.university.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/student")
@CrossOrigin(origins = "http://localhost:3000") // Allow React Frontend to access
public class StudentController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CoursePrerequisiteRepository prerequisiteRepository;

    @Autowired
    private MajorReqRepository majorReqRepository;

    // --- 1. GET STUDENT PROFILE & CONTEXT ---
    // Returns student details including completed courses and current schedule
    @GetMapping("/{studentId}/profile")
    public ResponseEntity<?> getStudentProfile(@PathVariable String studentId) {
        Optional<Student> studentOpt = studentRepository.findByStudentId(studentId); //

        if (studentOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student not found");
        }

        Student student = studentOpt.get();
        return ResponseEntity.ok(student);
    }

    // --- 2. GET ALL AVAILABLE COURSES ---
    // Frontend uses this to populate the registration table
    @GetMapping("/courses")
    public ResponseEntity<List<CourseDTO>> getAllCourses() {
        List<Course> courses = courseRepository.findAll(); //
        List<CourseDTO> responseList = new ArrayList<>();

        for (Course c : courses) {
            // 1. Find the Major for this course
            // If no major is found (e.g. general elective), we return "N/A" or "General"
            Optional<MajorReq> majorReq = majorReqRepository.findFirstByCourse_CourseCode(c.getCourseCode());
            String majorId = majorReq.map(req -> req.getMajor().getMajorId()).orElse("N/A");

            // 2. Find Prerequisites
            List<CoursePrerequisite> prereqs = prerequisiteRepository.findByCourse_CourseCode(c.getCourseCode());

            // Convert list of objects to a single string like "CS101, MATH101" or "None"
            String prereqString;
            if (prereqs.isEmpty()) {
                prereqString = null; // Or "None"
            } else {
                // Join all prereq codes with commas
                prereqString = prereqs.stream()
                        .map(p -> p.getPrerequisite().getCourseCode())
                        .collect(Collectors.joining(", "));
            }

            // 3. Create DTO
            responseList.add(new CourseDTO(
                    c.getCourseCode(),
                    c.getCourseName(),
                    c.getCreditHours(),
                    c.getSemester(),
                    majorId,
                    prereqString
            ));
        }

        return ResponseEntity.ok(responseList);
    }

    // --- 3. REGISTER FOR A COURSE ---
    @PostMapping("/{studentId}/register")
    public ResponseEntity<?> registerCourse(@PathVariable String studentId, @RequestBody Map<String, String> payload) {
        String courseCode = payload.get("courseCode");

        // A. Fetch Entities
        Optional<Student> studentOpt = studentRepository.findByStudentId(studentId);
        Optional<Course> courseOpt = courseRepository.findByCourseCode(courseCode); //

        if (studentOpt.isEmpty() || courseOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid Student ID or Course Code");
        }

        Student student = studentOpt.get();
        Course course = courseOpt.get();

        // B. Check: Is already registered?
        // Note: We check if the courseCode exists in the currentCourses list
        if (student.getCurrentCourses().contains(courseCode)) {
            return ResponseEntity.badRequest().body("You are already registered for this course.");
        }

        // C. Check: Is already completed?
        boolean completed = student.getCompletedCourses().stream()
                .anyMatch(rec -> rec.getCourseName().equals(course.getCourseName())); // Matches by name as stored in record
        if (completed) {
            return ResponseEntity.badRequest().body("You have already completed this course.");
        }

        // D. Check: Is this course part of the Student's Major?
        // We use the helper repository to check MajorReq
        boolean validMajor = majorReqRepository.existsByMajor_MajorIdAndCourse_CourseCode(
                student.getMajor().getMajorId(),
                courseCode
        );
        if (!validMajor) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("This course is not allowed for your major.");
        }

        // E. Check: Prerequisites
        // Get list of required courses for the target course
        List<CoursePrerequisite> prerequisites = prerequisiteRepository.findByCourse_CourseCode(courseCode); //

        for (CoursePrerequisite prereq : prerequisites) {
            String requiredCourseCode = prereq.getPrerequisite().getCourseCode();
            String requiredCourseName = prereq.getPrerequisite().getCourseName();

            // Check if student has passed this prerequisite
            // We assume a passed grade is >= 50.0 (Adjust based on university rules)
            boolean prereqMet = student.getCompletedCourses().stream()
                    .anyMatch(rec -> rec.getCourseName().equals(requiredCourseName) && rec.getGrade() >= 1.0);

            if (!prereqMet) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Missing Prerequisite: " + requiredCourseName);
            }
        }
        // --- NEW: CREDIT LIMIT CHECK ---
        int MAX_CREDITS = 18;

        // 1. Calculate current load
        // We have a list of strings (codes), we need to find their credit values
        List<Course> currentSchedule = courseRepository.findAllById(student.getCurrentCourses());
        int currentCredits = currentSchedule.stream().mapToInt(Course::getCreditHours).sum();

        // 2. Check if adding the new course exceeds the limit
        if (currentCredits + course.getCreditHours() > MAX_CREDITS) {
            return ResponseEntity.badRequest()
                    .body("Credit Limit Exceeded! You have " + currentCredits + " credits. Adding this would make it "
                            + (currentCredits + course.getCreditHours()) + ". Max is " + MAX_CREDITS + ".");
        }

        // F. Success: Enroll and Save
        student.enrollCourse(courseCode); // Adds to currentCourses list
        studentRepository.save(student);

        return ResponseEntity.ok("Successfully registered for " + course.getCourseName());
    }

    // --- 4. DROP A COURSE ---
    @PostMapping("/{studentId}/drop")
    public ResponseEntity<?> dropCourse(@PathVariable String studentId, @RequestBody Map<String, String> payload) {
        String courseCode = payload.get("courseCode");
        Optional<Student> studentOpt = studentRepository.findByStudentId(studentId);

        if (studentOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Student not found");
        }

        Student student = studentOpt.get();

        if (!student.getCurrentCourses().contains(courseCode)) {
            return ResponseEntity.badRequest().body("Course not found in current schedule.");
        }

        // Remove from list
        student.getCurrentCourses().remove(courseCode); //
        studentRepository.save(student);

        return ResponseEntity.ok("Successfully dropped course: " + courseCode);
    }
}
//
//    Here is the breakdown of the endpoints you need for a complete Student Controller.
//
//    1. Functionality Checklist
//    General Functionality:
//          getStudentProfile: Retrieves full student details (ID, Name, Major, GPA) using their email (from the token) or ID.
//          getStudentSchedule: Retrieves the list of courses the student is currently registered for (used for the "My Courses" and "Schedule" pages).
//          getAcademicHistory (Optional but recommended): Retrieves a list of completed courses and grades to calculate GPA or show a transcript.
//
//    Course Registration Functionality:
//          getAllCourses: Fetches the master list of all available courses for the semester. The frontend needs this to render the big table.
//          registerCourse: The core logic. It must receive a Student ID and Course ID, check prerequisites/major on the server side (for security), and save the enrollment.
//          dropCourse: Removes a specific course from the student's active schedule.



