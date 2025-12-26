package com.university.backend.controllers;

import com.university.backend.dto.AssignmentResponseDTO;
import com.university.backend.dto.AssignmentSubmissionResponseDTO;
import com.university.backend.entity.Assignment.Assignment;
import com.university.backend.entity.AssignmentSubmissions.AssignmentSubmission;
import com.university.backend.entity.AssignmentSubmissions.SubmissionAttributes;
import com.university.backend.entity.AssignmentSubmissions.SubmissionValue;
import com.university.backend.entity.Course;
import com.university.backend.entity.CourseGradingItem;
import com.university.backend.entity.Student;
import com.university.backend.repository.*;
import com.university.backend.services.AssignmentService;
import com.university.backend.services.AssignmentSubmissionService;
import com.university.backend.services.GradingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/grading")
public class GradingController {

    @Autowired
    private GradingService gradingService;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseGradingItemRepository gradingItemRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private AssignmentSubmissionRepository submissionRepository;


    private final AssignmentService assignmentService;

    private final AssignmentSubmissionService submissionService;

    public GradingController(AssignmentService assignmentService, AssignmentSubmissionService submissionService) {
        this.assignmentService = assignmentService;
        this.submissionService = submissionService;
    }
    // ==================================================================================
    // ==================================================================================
    // SECTION 1: COURSE GRADING ITEMS (The "Buckets")                                 ||
    // ==================================================================================
    // ==================================================================================

    /**
     * POST: Create a new Grading Bucket (e.g., "Midterm" = 20%)
     * URL: http://localhost:8081/api/grading/item/create
     * Payload: { "courseId": "CSE111", "categoryName": "Labs", "weight": 20 }
     */
    @PostMapping("/item/create")
    public ResponseEntity<?> createGradingItem(@RequestBody Map<String, Object> payload) {
        try {
            // 1. Extract & Validate Inputs
            String courseId = (String) payload.get("courseId");
            String categoryName = (String) payload.get("categoryName");
            Integer weight = (Integer) payload.get("weight");

            if (courseId == null || categoryName == null || weight == null) {
                return ResponseEntity.badRequest().body("Error: Missing required fields (courseId, categoryName, weight).");
            }

            // 2. Check if Course Exists (Prevent Crash)
            Course course = courseRepository.findById(courseId)
                    .orElse(null);

            if (course == null) {
                return ResponseEntity.status(404).body("Error: Course with ID " + courseId + " not found.");
            }

            // 3. Create & Save Entity
            CourseGradingItem item = new CourseGradingItem(course, categoryName, weight);
            CourseGradingItem savedItem = gradingItemRepository.save(item);

            return ResponseEntity.ok(savedItem);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
        }
    }

    /**
     * GET: Fetch all Grading Buckets for a Course
     * URL: http://localhost:8080/api/grading/item/course/CSE111
     */
    @GetMapping("/item/course/{courseCode}")
    public ResponseEntity<?> getGradingItemsByCourse(@PathVariable String courseCode) {
        try {
            // 1. Check if Course Exists first (Optional, but good for specific error messages)
            if (!courseRepository.existsById(courseCode)) {
                return ResponseEntity.status(404).body("Error: Course with ID " + courseCode + " not found.");
            }

            List<CourseGradingItem> items = gradingItemRepository.findByCourse_CourseCode(courseCode);

            // CONVERT TO DTO (Data Transfer Object)
            // This strips away the "Hibernate Proxy" issues
            List<Map<String, Object>> response = items.stream().map(item -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", item.getId());
                dto.put("categoryName", item.getCategoryName());
                dto.put("weightPercentage", item.getWeightPercentage());
                dto.put("courseId", item.getCourse().getCourseCode()); // Safe string access
                return dto;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching grading items: " + e.getMessage());
        }
    }

    /**
     * PUT: Update an existing Grading Bucket
     * URL: http://localhost:8081/api/grading/item/update/{id}
     * Payload: { "categoryName": "New Name", "weight": 25 }
     */
    @PutMapping("/item/update/{id}")
    public ResponseEntity<?> updateGradingItem(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        try {
            // 1. Fetch the Item
            CourseGradingItem item = gradingItemRepository.findById(id)
                    .orElse(null);

            if (item == null) {
                return ResponseEntity.status(404).body("Error: Grading item with ID " + id + " not found.");
            }

            // 2. Update Fields (Only if they are provided in JSON)
            if (payload.containsKey("categoryName")) {
                item.setCategoryName((String) payload.get("categoryName"));
            }
            if (payload.containsKey("weight")) {
                item.setWeightPercentage((Integer) payload.get("weight"));
            }

            // 3. Save
            CourseGradingItem updatedItem = gradingItemRepository.save(item);

            // 4. PREPARE SAFE RESPONSE (DTO) - Fixes the Error
            Map<String, Object> response = new HashMap<>();
            response.put("id", updatedItem.getId());
            response.put("categoryName", updatedItem.getCategoryName());
            response.put("weightPercentage", updatedItem.getWeightPercentage());
            // Safe string access prevents the crash
            response.put("courseId", updatedItem.getCourse().getCourseCode());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating item: " + e.getMessage());
        }
    }

    /**
     * DELETE: Remove a Grading Bucket
     * URL: http://localhost:8081/api/grading/item/delete/{id}
     */
    @DeleteMapping("/item/delete/{id}")
    public ResponseEntity<?> deleteGradingItem(@PathVariable Long id) {
        try {
            // 1. Check Existence
            if (!gradingItemRepository.existsById(id)) {
                return ResponseEntity.status(404).body("Error: Grading item with ID " + id + " not found.");
            }

            // 2. Delete
            gradingItemRepository.deleteById(id);

            return ResponseEntity.ok("Grading item deleted successfully.");

        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // This happens if Assignments are already linked to this bucket
            return ResponseEntity.badRequest().body("Cannot delete this Grading Item because Assignments are linked to it. Please re-assign or delete the assignments first.");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error deleting item: " + e.getMessage());
        }
    }

    // =================================================================
    // =================================================================
    // SECTION 2: ASSIGNMENTS (Create, View)
    // =================================================================
    // =================================================================


    // GET: Fetch all assignments for a course
    // URL: http://localhost:8080/api/grading/task/course/CSE111
    @GetMapping("/task/course/{courseCode}")
    public ResponseEntity<?> getCourseAssignments(@PathVariable String courseCode) {
        try {
            // 1. VALIDATION: Check if the course actually exists first
            // This prevents returning an empty list [] for a typo URL like "CSE999"
            if (!courseRepository.existsById(courseCode)) {
                return ResponseEntity.status(404)
                        .body("Error: Course with ID " + courseCode + " not found.");
            }

            // 2. FETCH: Get the data (Safe DTOs)
            List<AssignmentResponseDTO> assignments = assignmentService.getAssignmentsByCourse(courseCode);

            // 3. SUCCESS: Return the list (Empty [] is valid for a new course)
            return ResponseEntity.ok(assignments);

        } catch (Exception e) {
            // 4. FAILURE: Database or Server crash
            return ResponseEntity.status(500)
                    .body("Internal Server Error: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------
    // GET: Student views a specific assignment
    // URL: http://localhost:8081/api/assignments/1
    // ------------------------------------------------------------
    @GetMapping("/task/{id}")
    public ResponseEntity<?> getAssignment(@PathVariable Long id) {
        try {
            // 1. Call Service
            AssignmentResponseDTO assignmentDto = assignmentService.getAssignmentById(id);

            // 2. Success
            return ResponseEntity.ok(assignmentDto);

        } catch (RuntimeException e) {
            // 3. Handle "Not Found" specifically
            // If the service throws "Assignment not found with ID: 101"
            if (e.getMessage().toLowerCase().contains("not found")) {
                return ResponseEntity.status(404).body("Error: " + e.getMessage());
            }
            // Other logical errors
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }


    }

    // 2. POST: Create Assignment (Handles Static + Dynamic Attributes)
    // URL: http://localhost:8081/api/assignments/create
    @PostMapping("/task/create")
    public ResponseEntity<?> createAssignment(@RequestBody Map<String, Object> payload) {
        try {
            // 1. Call the Service
            AssignmentResponseDTO newAssignment = assignmentService.createAssignment(payload);

            // 2. Success -> Return the created object
            return ResponseEntity.ok(newAssignment);

        } catch (RuntimeException e) {
            // 3. Catch Service Errors (e.g., "Course not found", "Missing Title")
            // If the message contains "not found", return 404. Otherwise, return 400.
            if (e.getMessage().toLowerCase().contains("not found")) {
                return ResponseEntity.status(404).body("Error: " + e.getMessage());
            } else {
                return ResponseEntity.badRequest().body("Error: " + e.getMessage());
            }

        }
    }

    // 4. UPDATE ASSIGNMENT
    @PutMapping("/task/{id}") // URL: /api/assignments/101
    public ResponseEntity<?> updateAssignment(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        try {
            AssignmentResponseDTO updated = assignmentService.updateAssignment(id, payload);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating assignment: " + e.getMessage());
        }
    }

    // 5. DELETE ASSIGNMENT
    @DeleteMapping("/task/{id}") // URL: /api/assignments/101
    public ResponseEntity<?> deleteAssignment(@PathVariable Long id) {
        try {
            assignmentService.deleteAssignment(id);
            return ResponseEntity.ok("Assignment and all related submissions deleted successfully.");
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // This catches the case where Cascade Delete is NOT configured
            return ResponseEntity.badRequest().body("Cannot delete Assignment: It has linked submissions. Please delete submissions first.");
        }
        catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Error: " + e.getMessage());
        }
    }


    // =================================================================
    // =================================================================
    // SECTION 3: SUBMISSIONS (Create, View)
    // =================================================================
    // =================================================================


    // 1. POST: Create a Submission (Uses the Service's createSubmission)
    @PostMapping("submission/create")
    public ResponseEntity<?> createSubmission(@RequestBody Map<String, Object> payload) {
        try {
            // 1. Call Service
            AssignmentSubmissionResponseDTO response = submissionService.createSubmission(payload);

            // 2. Success
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            // 3. Handle Logical Errors (Missing Fields, Student Not Found, etc.)
            if (e.getMessage().toLowerCase().contains("not found")) {
                return ResponseEntity.status(404).body("Error: " + e.getMessage());
            }
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());

        }
    }
    // 2. GET: Fetch a Submission by ID (Uses the Service's getSubmissionById)
    @GetMapping("submission/{id}")
    public ResponseEntity<?> getSubmissionById(@PathVariable Long id) {
        try {
            AssignmentSubmissionResponseDTO submission = submissionService.getSubmissionById(id);
            return ResponseEntity.ok(submission);

        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/all-submissions/{assignmentId}")
    public ResponseEntity<?> getSubmissionsByAssignment(@PathVariable Long assignmentId) {
        try {
            List<AssignmentSubmissionResponseDTO> list = submissionService.getSubmissionsByAssignment(assignmentId);
            return ResponseEntity.ok(list);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching submissions: " + e.getMessage());
        }
    }

    @GetMapping("/sub-check/{assignmentId}/student/{studentId}")
    public ResponseEntity<?> checkStudentSubmission(
            @PathVariable Long assignmentId,
            @PathVariable String studentId) {
        try {
            // 1. VALIDATION: Check if Student exists
            if (!studentRepository.existsById(studentId)) {
                return ResponseEntity.status(404)
                        .body("Error: Student with ID " + studentId + " not found.");
            }

            // 2. VALIDATION: Check if Assignment exists (Optional but Good Practice)
            if (!assignmentRepository.existsById(assignmentId)) {
                return ResponseEntity.status(404)
                        .body("Error: Assignment with ID " + assignmentId + " not found.");
            }

            // 3. LOGIC: Check for submission
            AssignmentSubmissionResponseDTO submission = submissionService.getStudentSubmission(assignmentId, studentId);

            // 4. RETURN:
            // - If Found: Returns JSON object
            // - If Not Found: Returns null (This is valid "Not Submitted" state)
            return ResponseEntity.ok(submission);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error checking submission status: " + e.getMessage());
        }
    }

    // URL: http://localhost:8081/api/grading/sub-update/500
    @PutMapping("/sub-update/{id}")
    public ResponseEntity<?> updateSubmission(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        try {
            AssignmentSubmissionResponseDTO updated = submissionService.updateSubmission(id, payload);
            return ResponseEntity.ok(updated);

        } catch (RuntimeException e) {
            // Handle "Submission not found"
            return ResponseEntity.status(404).body("Error: " + e.getMessage());
        }
    }

    // URL: http://localhost:8081/api/grading/sub-delete/500
    @DeleteMapping("/sub-delete/{id}")
    public ResponseEntity<?> deleteSubmission(@PathVariable Long id) {
        try {
            submissionService.deleteSubmission(id);
            return ResponseEntity.ok("Submission deleted successfully.");

        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Error: " + e.getMessage());
        }
    }


    // URL: http://localhost:8081/api/grading/calculate/{studentId}/{gradingItemId}
    @GetMapping("/calculate/{studentId}/{gradingItemId}")
    public ResponseEntity<?> getBucketScore(
            @PathVariable String studentId,
            @PathVariable Long gradingItemId) {
        try {
            // 1. Calculate
            double scoreReport = gradingService.calculateBucketScore(studentId, gradingItemId);

            // 2. Return Report
            // JSON Example: { "totalEarned": 80.0, "totalPossible": 100.0, "weightedScore": 16.0 }
            return ResponseEntity.ok(scoreReport);

        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
        }
    }

}
