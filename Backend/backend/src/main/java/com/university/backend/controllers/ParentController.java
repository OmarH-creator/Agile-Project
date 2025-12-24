package com.university.backend.controllers;

import com.university.backend.entity.Course_record;
import com.university.backend.entity.Parent;
import com.university.backend.entity.Student;
import com.university.backend.repository.StudentRepository;
import org.springframework.web.bind.annotation.*;
import com.university.backend.repository.ParentRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;
import com.university.backend.entity.Course_record;
import com.university.backend.repository.CourseRecordRepository;

import javax.sql.DataSource;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/parents")  // ← MUST have /api prefix
public class ParentController {

    @Autowired
    private CourseRecordRepository courseRecordRepository; // Add this
    @Autowired
    private ParentRepository parentRepository;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private StudentRepository studentRepository;
    @GetMapping("/debug-test")
    public String debugTest() {
        System.out.println("DEBUG: ParentController is working!");
        return "ParentController is working!";
    }
    @GetMapping("/debug/student-course-records/{studentId}")
    public Map<String, Object> debugStudentRecords(@PathVariable String studentId) {
        Map<String, Object> response = new HashMap<>();

        // 1. Check if student exists
        Optional<Student> studentOpt = studentRepository.findByStudentId(studentId);
        if (studentOpt.isEmpty()) {
            response.put("error", "Student not found");
            return response;
        }

        Student student = studentOpt.get();
        response.put("student", student);

        // 2. Try JPA repository method
        List<Course_record> jpaRecords = courseRecordRepository.findByStudentStudentId(studentId);
        response.put("jpaRecordsCount", jpaRecords.size());
        response.put("jpaRecords", jpaRecords);

        // 3. Try custom query (if you have it)
        try {
            // If you added the @Query method in CourseRecordRepository
            List<Course_record> queryRecords = courseRecordRepository.findCoursesByStudentId(studentId);
            response.put("queryRecordsCount", queryRecords.size());
        } catch (Exception e) {
            response.put("queryError", e.getMessage());
        }

        // 4. Check student's completedCourses (via relationship)
        response.put("completedCoursesCount", student.getCompletedCourses().size());
        response.put("completedCourses", student.getCompletedCourses());

        System.out.println("=== DEBUG FOR STUDENT " + studentId + " ===");
        System.out.println("JPA found: " + jpaRecords.size() + " records");
        System.out.println("Student.completedCourses: " + student.getCompletedCourses().size() + " records");

        return response;
    }

    // Direct SQL test
    @GetMapping("/debug/sql-course-records/{studentId}")
    public List<Map<String, Object>> debugSqlCourseRecords(@PathVariable String studentId) {
        List<Map<String, Object>> results = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM course_records WHERE student_id = ?")) {

            stmt.setString(1, studentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> record = new HashMap<>();
                record.put("id", rs.getLong("id"));
                record.put("course_name", rs.getString("course_name"));
                record.put("grade", rs.getDouble("grade"));
                record.put("credits", rs.getInt("credits"));
                record.put("semester", rs.getString("semester"));
                record.put("student_id", rs.getString("student_id"));
                results.add(record);
            }

            System.out.println("DEBUG SQL: Found " + results.size() + " course records for student " + studentId);

        } catch (Exception e) {
            System.out.println("DEBUG SQL Error: " + e.getMessage());
            e.printStackTrace();
        }

        return results;
    }

    // Test all course records
    @GetMapping("/debug/all-course-records")
    public List<Course_record> debugAllCourseRecords() {
        List<Course_record> allRecords = courseRecordRepository.findAll();
        System.out.println("Total course records in DB: " + allRecords.size());

        for (Course_record cr : allRecords) {
            System.out.println("Record: ID=" + cr.getId() +
                    ", Course=" + cr.getCourseName() +
                    ", Student=" + (cr.getStudent() != null ? cr.getStudent().getStudentId() : "null") +
                    ", Grade=" + cr.getGrade());
        }

        return allRecords;
    }


    @GetMapping("/test")
    public String test() {
        System.out.println("Parent test endpoint hit!");
        return "Parent Controller is working!";
    }

    @GetMapping("/by-email/{email}")
    public Parent getParentByEmail(@PathVariable String email) {
        System.out.println("Getting parent for email: " + email);
        return parentRepository.findByParentEmail(email)
                .orElseThrow(() -> new RuntimeException("Parent not found"));
    }

    @GetMapping("/by-email/{email}/children")
    public List<Student> getChildren(@PathVariable String email) {
        System.out.println("Getting children for parent email: " + email);

        Parent p = parentRepository.findByParentEmail(email)
                .orElseThrow(() -> new RuntimeException("Parent not found"));

        System.out.println("Found parent: " + p.getParentName());
        System.out.println("Student IDs: " + p.getStudentIds());

        return studentRepository.findByStudentIdIn(p.getStudentIds());
    }

    // NEW: Get course records for all children of a parent
    @GetMapping("/by-email/{email}/children/course-records")
    public Map<String, Object> getChildrenCourseRecords(@PathVariable String email) {
        Parent p = parentRepository.findByParentEmail(email)
                .orElseThrow(() -> new RuntimeException("Parent not found"));

        List<String> studentIds = p.getStudentIds();
        List<Student> students = studentRepository.findByStudentIdIn(studentIds);
        List<Course_record> courseRecords = courseRecordRepository.findByStudentStudentIdIn(studentIds);

        // Group course records by student
        Map<String, List<Course_record>> recordsByStudent = courseRecords.stream()
                .collect(Collectors.groupingBy(record -> record.getStudent().getStudentId()));

        Map<String, Object> response = new HashMap<>();
        response.put("students", students);
        response.put("courseRecords", recordsByStudent);
        response.put("totalStudents", students.size());
        response.put("totalCourses", courseRecords.size());

        return response;
    }

    // NEW: Get course records for a specific child
    @GetMapping("/by-email/{email}/children/{studentId}/course-records")
    public List<Course_record> getChildCourseRecords(
            @PathVariable String email,
            @PathVariable String studentId) {

        // Verify the parent owns this student
        Parent p = parentRepository.findByParentEmail(email)
                .orElseThrow(() -> new RuntimeException("Parent not found"));

        if (!p.getStudentIds().contains(studentId)) {
            throw new RuntimeException("Student not found under this parent");
        }

        return courseRecordRepository.findByStudentStudentId(studentId);
    }
}