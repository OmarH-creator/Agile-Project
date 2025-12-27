package com.university.backend.controllers;

import com.university.backend.entity.*;
import com.university.backend.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class StudentControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private MajorRepository majorRepository;
    
    @Autowired
    private CoursePrerequisiteRepository prerequisiteRepository;
    
    @Autowired
    private MajorReqRepository majorReqRepository;

    // ==========================================
    // STUDENT PROFILE TESTS
    // ==========================================

    // TEST 1: Get student profile - should work
    @Test
    public void getStudentProfile_ValidId_ShouldWork() {
        // Create test major and student
        Major major = new Major();
        major.setMajorId("CS");
        major.setMajorName("Computer Science");
        majorRepository.save(major);

        Student student = new Student();
        student.setStudentId("S001");
        student.setName("John Doe");
        student.setEmail("john@test.com");
        student.setPhone("1234567890");
        student.setAddress("123 Test St");
        student.setMilitaryStatus("Civilian");
        student.setMajor(major);
        studentRepository.save(student);

        // Get student profile
        ResponseEntity<Student> response = restTemplate.getForEntity(
            "/api/student/S001/profile", 
            Student.class
        );

        // Should return student profile
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("John Doe", response.getBody().getName());
        assertEquals("CS", response.getBody().getMajor().getMajorId());
    }

    // TEST 2: Get student profile - student not found
    @Test
    public void getStudentProfile_InvalidId_ShouldFail() {
        // Try to get non-existent student
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/api/student/NONEXISTENT/profile", 
            String.class
        );

        // Should return 404
        assertEquals(404, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Student not found"));
    }

    // ==========================================
    // AVAILABLE COURSES TESTS
    // ==========================================

    // TEST 3: Get all available courses - should work
    @Test
    public void getAllCourses_ShouldWork() {
        // Create test course
        Course course = new Course();
        course.setCourseCode("CS101");
        course.setCourseName("Introduction to Programming");
        course.setCreditHours(3);
        course.setSemester("Fall");
        courseRepository.save(course);

        // Get all courses
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/api/student/courses", 
            String.class
        );

        // Should return courses list
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    // ==========================================
    // COURSE REGISTRATION TESTS
    // ==========================================

    // TEST 4: Register for course - should work
    @Test
    public void registerCourse_ValidRequest_ShouldWork() {
        // Create test data
        Major major = createTestMajor("CS", "Computer Science");
        Course course = createTestCourse("CS101", "Programming", 3, "Fall");
        Student student = createTestStudent("S002", "Jane Doe", major);
        
        // Create major requirement (course is allowed for this major)
        MajorReq majorReq = new MajorReq();
        majorReq.setMajor(major);
        majorReq.setCourse(course);
        majorReqRepository.save(majorReq);

        // Register for course
        Map<String, String> request = new HashMap<>();
        request.put("courseCode", "CS101");

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/student/S002/register", 
            request, 
            String.class
        );

        // Should register successfully
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Successfully registered"));
    }

    // TEST 5: Register for course - student not found
    @Test
    public void registerCourse_StudentNotFound_ShouldFail() {
        // Try to register non-existent student
        Map<String, String> request = new HashMap<>();
        request.put("courseCode", "CS101");

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/student/NONEXISTENT/register", 
            request, 
            String.class
        );

        // Should fail
        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Invalid Student ID"));
    }

    // TEST 6: Register for course - course not found
    @Test
    public void registerCourse_CourseNotFound_ShouldFail() {
        // Create test student
        Major major = createTestMajor("CS", "Computer Science");
        Student student = createTestStudent("S003", "Test Student", major);

        // Try to register for non-existent course
        Map<String, String> request = new HashMap<>();
        request.put("courseCode", "NONEXISTENT");

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/student/S003/register", 
            request, 
            String.class
        );

        // Should fail
        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Invalid Student ID or Course Code"));
    }

    // TEST 7: Register for course - already registered
    @Test
    public void registerCourse_AlreadyRegistered_ShouldFail() {
        // Create test data
        Major major = createTestMajor("CS", "Computer Science");
        Course course = createTestCourse("CS102", "Data Structures", 3, "Fall");
        Student student = createTestStudent("S004", "Already Registered", major);
        
        // Add course to student's current courses
        student.enrollCourse("CS102");
        studentRepository.save(student);
        
        // Create major requirement
        MajorReq majorReq = new MajorReq();
        majorReq.setMajor(major);
        majorReq.setCourse(course);
        majorReqRepository.save(majorReq);

        // Try to register again
        Map<String, String> request = new HashMap<>();
        request.put("courseCode", "CS102");

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/student/S004/register", 
            request, 
            String.class
        );

        // Should fail
        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().contains("already registered"));
    }

    // TEST 8: Register for course - not allowed for major
    @Test
    public void registerCourse_NotAllowedForMajor_ShouldFail() {
        // Create test data
        Major major = createTestMajor("MATH", "Mathematics");
        Course course = createTestCourse("CS103", "Advanced Programming", 3, "Fall");
        Student student = createTestStudent("S005", "Math Student", major);
        
        // Note: No MajorReq created, so course is not allowed for this major

        // Try to register
        Map<String, String> request = new HashMap<>();
        request.put("courseCode", "CS103");

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/student/S005/register", 
            request, 
            String.class
        );

        // Should fail
        assertEquals(403, response.getStatusCode().value());
        assertTrue(response.getBody().contains("not allowed for your major"));
    }

    // TEST 9: Register for course - missing prerequisite
    @Test
    public void registerCourse_MissingPrerequisite_ShouldFail() {
        // Create test data
        Major major = createTestMajor("CS", "Computer Science");
        Course prereqCourse = createTestCourse("CS101", "Programming Basics", 3, "Fall");
        Course advancedCourse = createTestCourse("CS201", "Advanced Programming", 3, "Spring");
        Student student = createTestStudent("S006", "No Prereq", major);
        
        // Create prerequisite relationship
        CoursePrerequisite prerequisite = new CoursePrerequisite();
        prerequisite.setCourse(advancedCourse);
        prerequisite.setPrerequisite(prereqCourse);
        prerequisiteRepository.save(prerequisite);
        
        // Create major requirement for advanced course
        MajorReq majorReq = new MajorReq();
        majorReq.setMajor(major);
        majorReq.setCourse(advancedCourse);
        majorReqRepository.save(majorReq);

        // Try to register for advanced course without prerequisite
        Map<String, String> request = new HashMap<>();
        request.put("courseCode", "CS201");

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/student/S006/register", 
            request, 
            String.class
        );

        // Should fail
        assertEquals(403, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Missing Prerequisite"));
    }

    // TEST 10: Register for course - credit limit exceeded
    @Test
    public void registerCourse_CreditLimitExceeded_ShouldFail() {
        // Create test data
        Major major = createTestMajor("CS", "Computer Science");
        Student student = createTestStudent("S007", "Full Load", major);
        
        // Add courses to reach near credit limit (18 credits max)
        Course course1 = createTestCourse("CS301", "Course 1", 6, "Fall");
        Course course2 = createTestCourse("CS302", "Course 2", 6, "Fall");
        Course course3 = createTestCourse("CS303", "Course 3", 6, "Fall");
        Course newCourse = createTestCourse("CS304", "New Course", 3, "Fall");
        
        // Enroll in courses totaling 18 credits
        student.enrollCourse("CS301");
        student.enrollCourse("CS302");
        student.enrollCourse("CS303");
        studentRepository.save(student);
        
        // Create major requirement for new course
        MajorReq majorReq = new MajorReq();
        majorReq.setMajor(major);
        majorReq.setCourse(newCourse);
        majorReqRepository.save(majorReq);

        // Try to register for one more course (would exceed 18 credit limit)
        Map<String, String> request = new HashMap<>();
        request.put("courseCode", "CS304");

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/student/S007/register", 
            request, 
            String.class
        );

        // Should fail
        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Credit Limit Exceeded"));
    }

    // ==========================================
    // COURSE DROPPING TESTS
    // ==========================================

    // TEST 11: Drop course - should work
    @Test
    public void dropCourse_ValidRequest_ShouldWork() {
        // Create test data
        Major major = createTestMajor("CS", "Computer Science");
        Student student = createTestStudent("S008", "Drop Test", major);
        
        // Enroll student in a course
        student.enrollCourse("CS105");
        studentRepository.save(student);

        // Drop the course
        Map<String, String> request = new HashMap<>();
        request.put("courseCode", "CS105");

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/student/S008/drop", 
            request, 
            String.class
        );

        // Should drop successfully
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Successfully dropped"));
    }

    // TEST 12: Drop course - student not found
    @Test
    public void dropCourse_StudentNotFound_ShouldFail() {
        // Try to drop course for non-existent student
        Map<String, String> request = new HashMap<>();
        request.put("courseCode", "CS105");

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/student/NONEXISTENT/drop", 
            request, 
            String.class
        );

        // Should fail
        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Student not found"));
    }

    // TEST 13: Drop course - course not in schedule
    @Test
    public void dropCourse_CourseNotInSchedule_ShouldFail() {
        // Create test student
        Major major = createTestMajor("CS", "Computer Science");
        Student student = createTestStudent("S009", "No Course", major);

        // Try to drop course not in schedule
        Map<String, String> request = new HashMap<>();
        request.put("courseCode", "CS999");

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/student/S009/drop", 
            request, 
            String.class
        );

        // Should fail
        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Course not found in current schedule"));
    }

    // ==========================================
    // COMPLEX SCENARIO TESTS
    // ==========================================

    // TEST 14: Register with prerequisite met - should work
    @Test
    public void registerCourse_PrerequisiteMet_ShouldWork() {
        // Create test data
        Major major = createTestMajor("CS", "Computer Science");
        Course prereqCourse = createTestCourse("CS110", "Intro Programming", 3, "Fall");
        Course advancedCourse = createTestCourse("CS210", "Advanced Topics", 3, "Spring");
        Student student = createTestStudent("S010", "Good Student", major);
        
        // Add completed prerequisite course to student's record
        Course_record completedCourse = new Course_record();
        completedCourse.setStudent(student);
        completedCourse.setCourseName("Intro Programming");
        completedCourse.setGrade(3.5); // Good grade (>= 1.0)
        completedCourse.setCredits(3);
        completedCourse.setSemester("Fall 2023");
        student.getCompletedCourses().add(completedCourse);
        studentRepository.save(student);
        
        // Create prerequisite relationship
        CoursePrerequisite prerequisite = new CoursePrerequisite();
        prerequisite.setCourse(advancedCourse);
        prerequisite.setPrerequisite(prereqCourse);
        prerequisiteRepository.save(prerequisite);
        
        // Create major requirement
        MajorReq majorReq = new MajorReq();
        majorReq.setMajor(major);
        majorReq.setCourse(advancedCourse);
        majorReqRepository.save(majorReq);

        // Register for advanced course
        Map<String, String> request = new HashMap<>();
        request.put("courseCode", "CS210");

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/student/S010/register", 
            request, 
            String.class
        );

        // Should work because prerequisite is met
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Successfully registered"));
    }

    // TEST 15: Register for already completed course - should fail
    @Test
    public void registerCourse_AlreadyCompleted_ShouldFail() {
        // Create test data
        Major major = createTestMajor("CS", "Computer Science");
        Course course = createTestCourse("CS111", "Basic Programming", 3, "Fall");
        Student student = createTestStudent("S011", "Repeat Student", major);
        
        // Add completed course to student's record
        Course_record completedCourse = new Course_record();
        completedCourse.setStudent(student);
        completedCourse.setCourseName("Basic Programming");
        completedCourse.setGrade(2.5);
        completedCourse.setCredits(3);
        completedCourse.setSemester("Fall 2023");
        student.getCompletedCourses().add(completedCourse);
        studentRepository.save(student);
        
        // Create major requirement
        MajorReq majorReq = new MajorReq();
        majorReq.setMajor(major);
        majorReq.setCourse(course);
        majorReqRepository.save(majorReq);

        // Try to register for already completed course
        Map<String, String> request = new HashMap<>();
        request.put("courseCode", "CS111");

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/student/S011/register", 
            request, 
            String.class
        );

        // Should fail
        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().contains("already completed"));
    }

    // ==========================================
    // EDGE CASE TESTS
    // ==========================================

    // TEST 16: Register with empty course code - should fail
    @Test
    public void registerCourse_EmptyCourseCode_ShouldFail() {
        // Create test student
        Major major = createTestMajor("CS", "Computer Science");
        Student student = createTestStudent("S012", "Empty Code Test", major);

        // Try to register with empty course code
        Map<String, String> request = new HashMap<>();
        request.put("courseCode", "");

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/student/S012/register", 
            request, 
            String.class
        );

        // Should fail
        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Invalid Student ID or Course Code"));
    }

    // TEST 17: Drop with empty course code - should fail
    @Test
    public void dropCourse_EmptyCourseCode_ShouldFail() {
        // Create test student
        Major major = createTestMajor("CS", "Computer Science");
        Student student = createTestStudent("S013", "Empty Drop Test", major);

        // Try to drop with empty course code
        Map<String, String> request = new HashMap<>();
        request.put("courseCode", "");

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/student/S013/drop", 
            request, 
            String.class
        );

        // Should fail
        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Course not found in current schedule"));
    }

    // TEST 18: Get profile with special characters in ID - should handle gracefully
    @Test
    public void getStudentProfile_SpecialCharacters_ShouldHandleGracefully() {
        // Try to get student with special characters in ID
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/api/student/S@#$/profile", 
            String.class
        );

        // Should return 403 (forbidden due to URL encoding) or 404 (not found) rather than crash
        assertTrue(response.getStatusCode().value() == 404 || response.getStatusCode().value() == 403);
        // Response body might be null for 403 errors, so we don't check it
    }

    // TEST 19: Register course - null request body
    @Test
    public void registerCourse_NullRequestBody_ShouldFail() {
        // Create test student
        Major major = createTestMajor("CS", "Computer Science");
        Student student = createTestStudent("S014", "Null Test", major);

        // Try to register with null request
        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/student/S014/register", 
            null, 
            String.class
        );

        // Should fail gracefully
        assertTrue(response.getStatusCode().value() >= 400);
    }

    // TEST 20: Multiple course registration and dropping - integration test
    @Test
    public void multipleOperations_IntegrationTest_ShouldWork() {
        // Create test data
        Major major = createTestMajor("CS", "Computer Science");
        Student student = createTestStudent("S015", "Integration Test", major);
        Course course1 = createTestCourse("CS401", "Course 1", 3, "Fall");
        Course course2 = createTestCourse("CS402", "Course 2", 3, "Fall");
        
        // Create major requirements
        MajorReq majorReq1 = new MajorReq();
        majorReq1.setMajor(major);
        majorReq1.setCourse(course1);
        majorReqRepository.save(majorReq1);
        
        MajorReq majorReq2 = new MajorReq();
        majorReq2.setMajor(major);
        majorReq2.setCourse(course2);
        majorReqRepository.save(majorReq2);

        // Step 1: Register for first course
        Map<String, String> request1 = new HashMap<>();
        request1.put("courseCode", "CS401");
        ResponseEntity<String> response1 = restTemplate.postForEntity(
            "/api/student/S015/register", request1, String.class);
        assertEquals(200, response1.getStatusCode().value());

        // Step 2: Register for second course
        Map<String, String> request2 = new HashMap<>();
        request2.put("courseCode", "CS402");
        ResponseEntity<String> response2 = restTemplate.postForEntity(
            "/api/student/S015/register", request2, String.class);
        assertEquals(200, response2.getStatusCode().value());

        // Step 3: Drop first course
        Map<String, String> dropRequest = new HashMap<>();
        dropRequest.put("courseCode", "CS401");
        ResponseEntity<String> dropResponse = restTemplate.postForEntity(
            "/api/student/S015/drop", dropRequest, String.class);
        assertEquals(200, dropResponse.getStatusCode().value());

        // Step 4: Verify profile still works
        ResponseEntity<Student> profileResponse = restTemplate.getForEntity(
            "/api/student/S015/profile", Student.class);
        assertEquals(200, profileResponse.getStatusCode().value());
        assertNotNull(profileResponse.getBody());
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================

    private Major createTestMajor(String majorId, String majorName) {
        Major major = new Major();
        major.setMajorId(majorId);
        major.setMajorName(majorName);
        return majorRepository.save(major);
    }

    private Course createTestCourse(String courseCode, String courseName, int creditHours, String semester) {
        Course course = new Course();
        course.setCourseCode(courseCode);
        course.setCourseName(courseName);
        course.setCreditHours(creditHours);
        course.setSemester(semester);
        return courseRepository.save(course);
    }

    private Student createTestStudent(String studentId, String name, Major major) {
        Student student = new Student();
        student.setStudentId(studentId);
        student.setName(name);
        student.setEmail(studentId.toLowerCase() + "@test.com");
        student.setPhone("123456" + studentId.substring(1)); // Unique phone based on student ID
        student.setAddress("123 Test St");
        student.setMilitaryStatus("Civilian");
        student.setMajor(major);
        return studentRepository.save(student);
    }
}