package com.university.backend.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Date;
import java.util.List;

public class StudentTest {

    // ==========================================
    // CONSTRUCTOR TESTS
    // ==========================================

    // TEST 1: Create student with default constructor - should work
    @Test
    public void createStudent_DefaultConstructor_ShouldWork() {
        // Step 1: Create student with default constructor
        Student student = new Student();
        
        // Step 2: Check that object was created
        assertNotNull(student);
        
        // Step 3: Check that fields are initially null/empty (as expected)
        assertNull(student.getStudentId());
        assertNull(student.getName());
        assertNull(student.getEmail());
        assertNull(student.getMajor());
        assertNull(student.getPhone());
        assertNull(student.getAddress());
        assertNull(student.getDateOfBirth());
        assertNull(student.getMilitaryStatus());
        
        // Step 4: Check that lists are initialized (not null)
        assertNotNull(student.getCompletedCourses());
        assertNotNull(student.getCurrentCourses());
        assertTrue(student.getCompletedCourses().isEmpty());
        assertTrue(student.getCurrentCourses().isEmpty());
    }

    // TEST 2: Create student with full constructor - should work
    @Test
    public void createStudent_FullConstructor_ShouldWork() {
        // Step 1: Create test major
        Major major = new Major("CS", "Computer Science");
        
        // Step 2: Create test date
        Date birthDate = new Date();
        
        // Step 3: Create student with full constructor
        Student student = new Student("S001", "John Doe", "john@test.com", 
                                    major, "1234567890", "123 Test St", 
                                    birthDate, "Civilian");
        
        // Step 4: Check that all fields were set correctly
        assertEquals("S001", student.getStudentId());
        assertEquals("John Doe", student.getName());
        assertEquals("john@test.com", student.getEmail());
        assertEquals(major, student.getMajor());
        assertEquals("1234567890", student.getPhone());
        assertEquals("123 Test St", student.getAddress());
        assertEquals(birthDate, student.getDateOfBirth());
        assertEquals("Civilian", student.getMilitaryStatus());
        
        // Step 5: Check that lists are initialized
        assertNotNull(student.getCompletedCourses());
        assertNotNull(student.getCurrentCourses());
    }

    // ==========================================
    // GETTER AND SETTER TESTS
    // ==========================================

    // TEST 3: Test student ID getter and setter - should work
    @Test
    public void studentIdGetterSetter_ValidId_ShouldWork() {
        // Step 1: Create student
        Student student = new Student();
        
        // Step 2: Set student ID using setter
        student.setStudentId("S12345");
        
        // Step 3: Get student ID using getter and verify
        assertEquals("S12345", student.getStudentId());
    }

    // TEST 4: Test name getter and setter - should work
    @Test
    public void nameGetterSetter_ValidName_ShouldWork() {
        // Step 1: Create student
        Student student = new Student();
        
        // Step 2: Set name using setter
        student.setName("Jane Smith");
        
        // Step 3: Get name using getter and verify
        assertEquals("Jane Smith", student.getName());
    }

    // TEST 5: Test email getter and setter - should work
    @Test
    public void emailGetterSetter_ValidEmail_ShouldWork() {
        // Step 1: Create student
        Student student = new Student();
        
        // Step 2: Set email using setter
        student.setEmail("jane@university.edu");
        
        // Step 3: Get email using getter and verify
        assertEquals("jane@university.edu", student.getEmail());
    }

    // TEST 6: Test phone getter and setter - should work
    @Test
    public void phoneGetterSetter_ValidPhone_ShouldWork() {
        // Step 1: Create student
        Student student = new Student();
        
        // Step 2: Set phone using setter
        student.setPhone("555-123-4567");
        
        // Step 3: Get phone using getter and verify
        assertEquals("555-123-4567", student.getPhone());
    }

    // TEST 7: Test address getter and setter - should work
    @Test
    public void addressGetterSetter_ValidAddress_ShouldWork() {
        // Step 1: Create student
        Student student = new Student();
        
        // Step 2: Set address using setter
        student.setAddress("456 University Ave, College Town, ST 12345");
        
        // Step 3: Get address using getter and verify
        assertEquals("456 University Ave, College Town, ST 12345", student.getAddress());
    }

    // TEST 8: Test military status getter and setter - should work
    @Test
    public void militaryStatusGetterSetter_ValidStatus_ShouldWork() {
        // Step 1: Create student
        Student student = new Student();
        
        // Step 2: Test different military statuses
        student.setMilitaryStatus("Civilian");
        assertEquals("Civilian", student.getMilitaryStatus());
        
        student.setMilitaryStatus("Veteran");
        assertEquals("Veteran", student.getMilitaryStatus());
        
        student.setMilitaryStatus("Active Duty");
        assertEquals("Active Duty", student.getMilitaryStatus());
    }

    // TEST 9: Test date of birth getter and setter - should work
    @Test
    public void dateOfBirthGetterSetter_ValidDate_ShouldWork() {
        // Step 1: Create student
        Student student = new Student();
        
        // Step 2: Create test date
        Date testDate = new Date(2000, 5, 15); // June 15, 2000
        
        // Step 3: Set date using setter
        student.setDateOfBirth(testDate);
        
        // Step 4: Get date using getter and verify
        assertEquals(testDate, student.getDateOfBirth());
    }

    // TEST 10: Test major getter and setter - should work
    @Test
    public void majorGetterSetter_ValidMajor_ShouldWork() {
        // Step 1: Create student
        Student student = new Student();
        
        // Step 2: Create test major
        Major major = new Major("MATH", "Mathematics");
        
        // Step 3: Set major using setter
        student.setMajor(major);
        
        // Step 4: Get major using getter and verify
        assertEquals(major, student.getMajor());
        assertEquals("MATH", student.getMajor().getMajorId());
        assertEquals("Mathematics", student.getMajor().getMajorName());
    }

    // ==========================================
    // COURSE ENROLLMENT TESTS
    // ==========================================

    // TEST 11: Test enrolling in current courses - should work
    @Test
    public void enrollCourse_ValidCourse_ShouldWork() {
        // Step 1: Create student
        Student student = new Student();
        
        // Step 2: Enroll in a course
        student.enrollCourse("CS101");
        
        // Step 3: Check that course was added to current courses
        List<String> currentCourses = student.getCurrentCourses();
        assertEquals(1, currentCourses.size());
        assertTrue(currentCourses.contains("CS101"));
    }

    // TEST 12: Test enrolling in multiple courses - should work
    @Test
    public void enrollCourse_MultipleCourses_ShouldWork() {
        // Step 1: Create student
        Student student = new Student();
        
        // Step 2: Enroll in multiple courses
        student.enrollCourse("CS101");
        student.enrollCourse("MATH201");
        student.enrollCourse("ENG102");
        
        // Step 3: Check that all courses were added
        List<String> currentCourses = student.getCurrentCourses();
        assertEquals(3, currentCourses.size());
        assertTrue(currentCourses.contains("CS101"));
        assertTrue(currentCourses.contains("MATH201"));
        assertTrue(currentCourses.contains("ENG102"));
    }

    // TEST 13: Test adding completed courses - should work
    @Test
    public void addCompletedCourse_ValidCourse_ShouldWork() {
        // Step 1: Create student
        Student student = new Student();
        
        // Step 2: Add a completed course
        student.addCompletedCourse("CS100", 3.5, 3, "Fall 2023");
        
        // Step 3: Check that course was added to completed courses
        List<Course_record> completedCourses = student.getCompletedCourses();
        assertEquals(1, completedCourses.size());
        
        Course_record record = completedCourses.get(0);
        assertEquals("CS100", record.getCourseName());
        assertEquals(3.5, record.getGrade());
        assertEquals(3, record.getCredits());
        assertEquals("Fall 2023", record.getSemester());
        assertEquals(student, record.getStudent());
    }

    // TEST 14: Test adding multiple completed courses - should work
    @Test
    public void addCompletedCourse_MultipleCourses_ShouldWork() {
        // Step 1: Create student
        Student student = new Student();
        
        // Step 2: Add multiple completed courses
        student.addCompletedCourse("CS100", 3.5, 3, "Fall 2023");
        student.addCompletedCourse("MATH150", 4.0, 4, "Fall 2023");
        student.addCompletedCourse("ENG101", 3.0, 3, "Spring 2024");
        
        // Step 3: Check that all courses were added
        List<Course_record> completedCourses = student.getCompletedCourses();
        assertEquals(3, completedCourses.size());
        
        // Step 4: Verify each course
        Course_record cs100 = completedCourses.get(0);
        assertEquals("CS100", cs100.getCourseName());
        assertEquals(3.5, cs100.getGrade());
        
        Course_record math150 = completedCourses.get(1);
        assertEquals("MATH150", math150.getCourseName());
        assertEquals(4.0, math150.getGrade());
        
        Course_record eng101 = completedCourses.get(2);
        assertEquals("ENG101", eng101.getCourseName());
        assertEquals(3.0, eng101.getGrade());
    }

    // ==========================================
    // GPA CALCULATION TESTS
    // ==========================================

    // TEST 15: Test GPA calculation with no courses - should return 0
    @Test
    public void getGPA_NoCourses_ShouldReturnZero() {
        // Step 1: Create student with no completed courses
        Student student = new Student();
        
        // Step 2: Calculate GPA
        double gpa = student.getGPA();
        
        // Step 3: Should return 0 when no courses completed
        assertEquals(0.0, gpa);
    }

    // TEST 16: Test GPA calculation with one course - should work
    @Test
    public void getGPA_OneCourse_ShouldCalculateCorrectly() {
        // Step 1: Create student
        Student student = new Student();
        
        // Step 2: Add one completed course (3.5 grade, 3 credits)
        student.addCompletedCourse("CS101", 3.5, 3, "Fall 2023");
        
        // Step 3: Calculate GPA
        double gpa = student.getGPA();
        
        // Step 4: GPA should be 3.5 (3.5 * 3 credits / 3 credits = 3.5)
        assertEquals(3.5, gpa, 0.01); // Allow small floating point difference
    }

    // TEST 17: Test GPA calculation with multiple courses - should work
    @Test
    public void getGPA_MultipleCourses_ShouldCalculateCorrectly() {
        // Step 1: Create student
        Student student = new Student();
        
        // Step 2: Add multiple completed courses
        student.addCompletedCourse("CS101", 4.0, 3, "Fall 2023");  // 4.0 * 3 = 12 points
        student.addCompletedCourse("MATH201", 3.0, 4, "Fall 2023"); // 3.0 * 4 = 12 points
        student.addCompletedCourse("ENG102", 3.5, 2, "Spring 2024"); // 3.5 * 2 = 7 points
        
        // Step 3: Calculate GPA
        double gpa = student.getGPA();
        
        // Step 4: GPA should be (12 + 12 + 7) / (3 + 4 + 2) = 31 / 9 = 3.44
        double expectedGPA = 31.0 / 9.0;
        assertEquals(expectedGPA, gpa, 0.01);
    }

    // TEST 18: Test GPA calculation with perfect grades - should work
    @Test
    public void getGPA_PerfectGrades_ShouldReturnFour() {
        // Step 1: Create student
        Student student = new Student();
        
        // Step 2: Add courses with perfect grades
        student.addCompletedCourse("CS101", 4.0, 3, "Fall 2023");
        student.addCompletedCourse("MATH201", 4.0, 4, "Fall 2023");
        student.addCompletedCourse("ENG102", 4.0, 2, "Spring 2024");
        
        // Step 3: Calculate GPA
        double gpa = student.getGPA();
        
        // Step 4: GPA should be 4.0
        assertEquals(4.0, gpa, 0.01);
    }

    // ==========================================
    // FIELD VALIDATION TESTS
    // ==========================================

    // TEST 19: Test valid student ID formats - should work
    @Test
    public void setStudentId_ValidFormats_ShouldWork() {
        Student student = new Student();
        
        // Test numeric ID
        student.setStudentId("12345");
        assertEquals("12345", student.getStudentId());
        
        // Test alphanumeric ID
        student.setStudentId("S12345");
        assertEquals("S12345", student.getStudentId());
        
        // Test ID with dashes
        student.setStudentId("2024-CS-001");
        assertEquals("2024-CS-001", student.getStudentId());
    }

    // TEST 20: Test valid email formats - should work
    @Test
    public void setEmail_ValidFormats_ShouldWork() {
        Student student = new Student();
        
        // Test university email
        student.setEmail("john.doe@university.edu");
        assertEquals("john.doe@university.edu", student.getEmail());
        
        // Test personal email
        student.setEmail("student123@gmail.com");
        assertEquals("student123@gmail.com", student.getEmail());
        
        // Test email with numbers
        student.setEmail("s2024001@student.university.edu");
        assertEquals("s2024001@student.university.edu", student.getEmail());
    }

    // TEST 21: Test valid phone formats - should work
    @Test
    public void setPhone_ValidFormats_ShouldWork() {
        Student student = new Student();
        
        // Test phone with dashes
        student.setPhone("555-123-4567");
        assertEquals("555-123-4567", student.getPhone());
        
        // Test phone with parentheses
        student.setPhone("(555) 123-4567");
        assertEquals("(555) 123-4567", student.getPhone());
        
        // Test phone with dots
        student.setPhone("555.123.4567");
        assertEquals("555.123.4567", student.getPhone());
        
        // Test international format
        student.setPhone("+1-555-123-4567");
        assertEquals("+1-555-123-4567", student.getPhone());
    }

    // ==========================================
    // EDGE CASE TESTS
    // ==========================================

    // TEST 22: Test null field handling - should work (but not recommended)
    @Test
    public void setFields_NullValues_ShouldWork() {
        Student student = new Student();
        
        // Set null values (entity allows it, database will reject it)
        student.setStudentId(null);
        student.setName(null);
        student.setEmail(null);
        student.setPhone(null);
        student.setAddress(null);
        student.setMilitaryStatus(null);
        student.setDateOfBirth(null);
        student.setMajor(null);
        
        // Should store null values in object
        assertNull(student.getStudentId());
        assertNull(student.getName());
        assertNull(student.getEmail());
        assertNull(student.getPhone());
        assertNull(student.getAddress());
        assertNull(student.getMilitaryStatus());
        assertNull(student.getDateOfBirth());
        assertNull(student.getMajor());
    }

    // TEST 23: Test empty string handling - should work
    @Test
    public void setFields_EmptyStrings_ShouldWork() {
        Student student = new Student();
        
        // Set empty strings
        student.setStudentId("");
        student.setName("");
        student.setEmail("");
        student.setPhone("");
        student.setAddress("");
        student.setMilitaryStatus("");
        
        // Should store empty strings
        assertEquals("", student.getStudentId());
        assertEquals("", student.getName());
        assertEquals("", student.getEmail());
        assertEquals("", student.getPhone());
        assertEquals("", student.getAddress());
        assertEquals("", student.getMilitaryStatus());
    }

    // TEST 24: Test very long field values - should work (entity level)
    @Test
    public void setFields_VeryLongValues_ShouldWork() {
        Student student = new Student();
        
        // Create very long values
        String longName = "VeryLongStudentNameThatExceedsTypicalLengthLimits";
        String longAddress = "123 Very Long Street Name That Goes On And On, Very Long City Name, Very Long State Name, 12345-6789";
        
        student.setName(longName);
        student.setAddress(longAddress);
        
        assertEquals(longName, student.getName());
        assertEquals(longAddress, student.getAddress());
    }

    // TEST 25: Test toString method - should work
    @Test
    public void toString_ValidStudent_ShouldWork() {
        // Step 1: Create student with major
        Major major = new Major("CS", "Computer Science");
        Student student = new Student();
        student.setStudentId("S001");
        student.setName("John Doe");
        student.setEmail("john@test.com");
        student.setMajor(major);
        student.setPhone("555-1234");
        student.setAddress("123 Test St");
        student.setMilitaryStatus("Civilian");
        student.setDateOfBirth(new Date());
        
        // Step 2: Get string representation
        String studentString = student.toString();
        
        // Step 3: Check that string contains key information
        assertNotNull(studentString);
        assertTrue(studentString.contains("S001"));
        assertTrue(studentString.contains("John Doe"));
        assertTrue(studentString.contains("john@test.com"));
        assertTrue(studentString.contains("Computer Science"));
        assertTrue(studentString.contains("555-1234"));
    }

    // TEST 26: Test course list management - should work
    @Test
    public void courseListManagement_ShouldWork() {
        // Step 1: Create student
        Student student = new Student();
        
        // Step 2: Test current courses list
        List<String> currentCourses = student.getCurrentCourses();
        assertNotNull(currentCourses);
        assertTrue(currentCourses.isEmpty());
        
        // Step 3: Add courses and verify
        student.enrollCourse("CS101");
        student.enrollCourse("MATH201");
        assertEquals(2, currentCourses.size());
        
        // Step 4: Test completed courses list
        List<Course_record> completedCourses = student.getCompletedCourses();
        assertNotNull(completedCourses);
        assertTrue(completedCourses.isEmpty());
        
        // Step 5: Add completed course and verify
        student.addCompletedCourse("CS100", 3.5, 3, "Fall 2023");
        assertEquals(1, completedCourses.size());
    }

    // TEST 27: Test GPA with zero credit courses - should handle correctly
    @Test
    public void getGPA_ZeroCreditCourses_ShouldHandleCorrectly() {
        // Step 1: Create student
        Student student = new Student();
        
        // Step 2: Add course with zero credits (unusual but possible)
        student.addCompletedCourse("ORIENTATION", 4.0, 0, "Fall 2023");
        
        // Step 3: Calculate GPA
        double gpa = student.getGPA();
        
        // Step 4: Should return 0 because total credits is 0
        assertEquals(0.0, gpa);
    }
}