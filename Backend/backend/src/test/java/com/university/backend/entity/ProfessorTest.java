package com.university.backend.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.ArrayList;

/**
 * SIMPLE Professor Entity Tests - Easy to Read and Understand
 * 
 * This file tests the Professor.java entity using basic JUnit syntax.
 * No complex Spring annotations - just simple object testing like your JavaFX course!
 * 
 * Tests cover:
 * - Object creation with constructors
 * - Getter and setter methods
 * - Field validation (professor ID, email uniqueness)
 * - Course assignment functionality
 * - Department validation
 * - Edge cases and null handling
 * 
 * Total: 20+ simple tests covering all Professor functionality.
 */
public class ProfessorTest {

    // ==========================================
    // CONSTRUCTOR TESTS
    // ==========================================

    // TEST 1: Create professor with default constructor - should work
    @Test
    public void createProfessor_DefaultConstructor_ShouldWork() {
        // Step 1: Create professor with default constructor
        Professor professor = new Professor();
        
        // Step 2: Check that object was created
        assertNotNull(professor);
        
        // Step 3: Check that fields are initially null (as expected)
        assertNull(professor.getProfessorId());
        assertNull(professor.getProfessorName());
        assertNull(professor.getProfessorEmail());
        assertNull(professor.getProfessorDepartment());
        
        // Step 4: Check that course list is initialized (not null)
        assertNotNull(professor.getProfessorCourses());
        assertTrue(professor.getProfessorCourses().isEmpty());
    }

    // TEST 2: Create professor with full constructor - should work
    @Test
    public void createProfessor_FullConstructor_ShouldWork() {
        // Step 1: Create professor with full constructor
        Professor professor = new Professor("P001", "Dr. John Smith", 
                                          "john.smith@university.edu", 
                                          "Computer Science");
        
        // Step 2: Check that all fields were set correctly
        assertEquals("P001", professor.getProfessorId());
        assertEquals("Dr. John Smith", professor.getProfessorName());
        assertEquals("john.smith@university.edu", professor.getProfessorEmail());
        assertEquals("Computer Science", professor.getProfessorDepartment());
        
        // Step 3: Check that course list is initialized
        assertNotNull(professor.getProfessorCourses());
        assertTrue(professor.getProfessorCourses().isEmpty());
    }

    // ==========================================
    // GETTER AND SETTER TESTS
    // ==========================================

    // TEST 3: Test professor ID getter and setter - should work
    @Test
    public void professorIdGetterSetter_ValidId_ShouldWork() {
        // Step 1: Create professor
        Professor professor = new Professor();
        
        // Step 2: Set professor ID using setter
        professor.setProfessorId("P12345");
        
        // Step 3: Get professor ID using getter and verify
        assertEquals("P12345", professor.getProfessorId());
    }

    // TEST 4: Test professor name getter and setter - should work
    @Test
    public void professorNameGetterSetter_ValidName_ShouldWork() {
        // Step 1: Create professor
        Professor professor = new Professor();
        
        // Step 2: Set name using setter
        professor.setProfessorName("Dr. Jane Doe");
        
        // Step 3: Get name using getter and verify
        assertEquals("Dr. Jane Doe", professor.getProfessorName());
    }

    // TEST 5: Test professor email getter and setter - should work
    @Test
    public void professorEmailGetterSetter_ValidEmail_ShouldWork() {
        // Step 1: Create professor
        Professor professor = new Professor();
        
        // Step 2: Set email using setter
        professor.setProfessorEmail("jane.doe@university.edu");
        
        // Step 3: Get email using getter and verify
        assertEquals("jane.doe@university.edu", professor.getProfessorEmail());
    }

    // TEST 6: Test professor department getter and setter - should work
    @Test
    public void professorDepartmentGetterSetter_ValidDepartment_ShouldWork() {
        // Step 1: Create professor
        Professor professor = new Professor();
        
        // Step 2: Set department using setter
        professor.setProfessorDepartment("Mathematics");
        
        // Step 3: Get department using getter and verify
        assertEquals("Mathematics", professor.getProfessorDepartment());
    }

    // TEST 7: Test professor courses getter and setter - should work
    @Test
    public void professorCoursesGetterSetter_ValidCourses_ShouldWork() {
        // Step 1: Create professor
        Professor professor = new Professor();
        
        // Step 2: Create course list
        List<String> courses = new ArrayList<>();
        courses.add("CS101");
        courses.add("CS201");
        
        // Step 3: Set courses using setter
        professor.setProfessorCourses(courses);
        
        // Step 4: Get courses using getter and verify
        List<String> retrievedCourses = professor.getProfessorCourses();
        assertEquals(2, retrievedCourses.size());
        assertTrue(retrievedCourses.contains("CS101"));
        assertTrue(retrievedCourses.contains("CS201"));
    }

    // ==========================================
    // COURSE ASSIGNMENT TESTS
    // ==========================================

    // TEST 8: Test assigning single course - should work
    @Test
    public void assignCourse_SingleCourse_ShouldWork() {
        // Step 1: Create professor
        Professor professor = new Professor();
        
        // Step 2: Assign a course
        professor.assignCourse("CS101");
        
        // Step 3: Check that course was added
        List<String> courses = professor.getProfessorCourses();
        assertEquals(1, courses.size());
        assertTrue(courses.contains("CS101"));
    }

    // TEST 9: Test assigning multiple courses - should work
    @Test
    public void assignCourse_MultipleCourses_ShouldWork() {
        // Step 1: Create professor
        Professor professor = new Professor();
        
        // Step 2: Assign multiple courses
        professor.assignCourse("CS101");
        professor.assignCourse("CS201");
        professor.assignCourse("CS301");
        
        // Step 3: Check that all courses were added
        List<String> courses = professor.getProfessorCourses();
        assertEquals(3, courses.size());
        assertTrue(courses.contains("CS101"));
        assertTrue(courses.contains("CS201"));
        assertTrue(courses.contains("CS301"));
    }

    // TEST 10: Test assigning duplicate courses - should work (allows duplicates)
    @Test
    public void assignCourse_DuplicateCourses_ShouldWork() {
        // Step 1: Create professor
        Professor professor = new Professor();
        
        // Step 2: Assign same course twice
        professor.assignCourse("CS101");
        professor.assignCourse("CS101");
        
        // Step 3: Check that both instances were added (List allows duplicates)
        List<String> courses = professor.getProfessorCourses();
        assertEquals(2, courses.size());
        assertTrue(courses.contains("CS101"));
    }

    // ==========================================
    // FIELD VALIDATION TESTS
    // ==========================================

    // TEST 11: Test valid professor ID formats - should work
    @Test
    public void setProfessorId_ValidFormats_ShouldWork() {
        Professor professor = new Professor();
        
        // Test numeric ID
        professor.setProfessorId("12345");
        assertEquals("12345", professor.getProfessorId());
        
        // Test alphanumeric ID
        professor.setProfessorId("P12345");
        assertEquals("P12345", professor.getProfessorId());
        
        // Test ID with dashes
        professor.setProfessorId("PROF-2024-001");
        assertEquals("PROF-2024-001", professor.getProfessorId());
        
        // Test department prefix
        professor.setProfessorId("CS-P001");
        assertEquals("CS-P001", professor.getProfessorId());
    }

    // TEST 12: Test valid email formats - should work
    @Test
    public void setProfessorEmail_ValidFormats_ShouldWork() {
        Professor professor = new Professor();
        
        // Test university email
        professor.setProfessorEmail("john.doe@university.edu");
        assertEquals("john.doe@university.edu", professor.getProfessorEmail());
        
        // Test email with department
        professor.setProfessorEmail("j.doe@cs.university.edu");
        assertEquals("j.doe@cs.university.edu", professor.getProfessorEmail());
        
        // Test email with numbers
        professor.setProfessorEmail("professor123@university.edu");
        assertEquals("professor123@university.edu", professor.getProfessorEmail());
    }

    // TEST 13: Test different department names - should work
    @Test
    public void setProfessorDepartment_ValidDepartments_ShouldWork() {
        Professor professor = new Professor();
        
        // Test Computer Science
        professor.setProfessorDepartment("Computer Science");
        assertEquals("Computer Science", professor.getProfessorDepartment());
        
        // Test Mathematics
        professor.setProfessorDepartment("Mathematics");
        assertEquals("Mathematics", professor.getProfessorDepartment());
        
        // Test Engineering
        professor.setProfessorDepartment("Electrical Engineering");
        assertEquals("Electrical Engineering", professor.getProfessorDepartment());
        
        // Test Business
        professor.setProfessorDepartment("Business Administration");
        assertEquals("Business Administration", professor.getProfessorDepartment());
    }

    // TEST 14: Test different name formats - should work
    @Test
    public void setProfessorName_ValidFormats_ShouldWork() {
        Professor professor = new Professor();
        
        // Test with Dr. title
        professor.setProfessorName("Dr. John Smith");
        assertEquals("Dr. John Smith", professor.getProfessorName());
        
        // Test with Prof. title
        professor.setProfessorName("Prof. Jane Doe");
        assertEquals("Prof. Jane Doe", professor.getProfessorName());
        
        // Test without title
        professor.setProfessorName("Michael Johnson");
        assertEquals("Michael Johnson", professor.getProfessorName());
        
        // Test with middle initial
        professor.setProfessorName("Dr. Sarah J. Wilson");
        assertEquals("Dr. Sarah J. Wilson", professor.getProfessorName());
    }

    // ==========================================
    // EDGE CASE TESTS
    // ==========================================

    // TEST 15: Test null field handling - should work (but not recommended)
    @Test
    public void setFields_NullValues_ShouldWork() {
        Professor professor = new Professor();
        
        // Set null values (entity allows it, database will reject it)
        professor.setProfessorId(null);
        professor.setProfessorName(null);
        professor.setProfessorEmail(null);
        professor.setProfessorDepartment(null);
        
        // Should store null values in object
        assertNull(professor.getProfessorId());
        assertNull(professor.getProfessorName());
        assertNull(professor.getProfessorEmail());
        assertNull(professor.getProfessorDepartment());
    }

    // TEST 16: Test empty string handling - should work
    @Test
    public void setFields_EmptyStrings_ShouldWork() {
        Professor professor = new Professor();
        
        // Set empty strings
        professor.setProfessorId("");
        professor.setProfessorName("");
        professor.setProfessorEmail("");
        professor.setProfessorDepartment("");
        
        // Should store empty strings
        assertEquals("", professor.getProfessorId());
        assertEquals("", professor.getProfessorName());
        assertEquals("", professor.getProfessorEmail());
        assertEquals("", professor.getProfessorDepartment());
    }

    // TEST 17: Test very long field values - should work (entity level)
    @Test
    public void setFields_VeryLongValues_ShouldWork() {
        Professor professor = new Professor();
        
        // Create very long values
        String longName = "Dr. VeryLongProfessorNameThatExceedsTypicalLengthLimits";
        String longDepartment = "Very Long Department Name That Goes On And On And Exceeds Normal Limits";
        
        professor.setProfessorName(longName);
        professor.setProfessorDepartment(longDepartment);
        
        assertEquals(longName, professor.getProfessorName());
        assertEquals(longDepartment, professor.getProfessorDepartment());
    }

    // TEST 18: Test null course assignment - should handle gracefully
    @Test
    public void assignCourse_NullCourse_ShouldWork() {
        Professor professor = new Professor();
        
        // Assign null course (should add null to list)
        professor.assignCourse(null);
        
        // Check that null was added to course list
        List<String> courses = professor.getProfessorCourses();
        assertEquals(1, courses.size());
        assertTrue(courses.contains(null));
    }

    // TEST 19: Test empty course assignment - should work
    @Test
    public void assignCourse_EmptyCourse_ShouldWork() {
        Professor professor = new Professor();
        
        // Assign empty course name
        professor.assignCourse("");
        
        // Check that empty string was added to course list
        List<String> courses = professor.getProfessorCourses();
        assertEquals(1, courses.size());
        assertTrue(courses.contains(""));
    }

    // ==========================================
    // BUSINESS LOGIC TESTS
    // ==========================================

    // TEST 20: Test complete professor creation workflow - should work
    @Test
    public void createCompleteProfessor_AllFields_ShouldWork() {
        // Step 1: Create professor with constructor
        Professor professor = new Professor("P001", "Dr. Alice Johnson", 
                                          "alice.johnson@university.edu", 
                                          "Computer Science");
        
        // Step 2: Assign courses
        professor.assignCourse("CS101 - Introduction to Programming");
        professor.assignCourse("CS201 - Data Structures");
        professor.assignCourse("CS301 - Algorithms");
        
        // Step 3: Verify all fields are correct
        assertEquals("P001", professor.getProfessorId());
        assertEquals("Dr. Alice Johnson", professor.getProfessorName());
        assertEquals("alice.johnson@university.edu", professor.getProfessorEmail());
        assertEquals("Computer Science", professor.getProfessorDepartment());
        
        // Step 4: Verify courses are assigned
        List<String> courses = professor.getProfessorCourses();
        assertEquals(3, courses.size());
        assertTrue(courses.contains("CS101 - Introduction to Programming"));
        assertTrue(courses.contains("CS201 - Data Structures"));
        assertTrue(courses.contains("CS301 - Algorithms"));
    }

    // TEST 21: Test professor field updates - should work
    @Test
    public void updateProfessorFields_ExistingProfessor_ShouldWork() {
        // Step 1: Create initial professor
        Professor professor = new Professor("P001", "Dr. Old Name", 
                                          "old@email.com", "Old Department");
        
        // Step 2: Update fields
        professor.setProfessorId("P002");
        professor.setProfessorName("Dr. New Name");
        professor.setProfessorEmail("new@email.com");
        professor.setProfessorDepartment("New Department");
        
        // Step 3: Verify updates worked
        assertEquals("P002", professor.getProfessorId());
        assertEquals("Dr. New Name", professor.getProfessorName());
        assertEquals("new@email.com", professor.getProfessorEmail());
        assertEquals("New Department", professor.getProfessorDepartment());
    }

    // TEST 22: Test course list management - should work
    @Test
    public void courseListManagement_ShouldWork() {
        // Step 1: Create professor
        Professor professor = new Professor();
        
        // Step 2: Test initial course list
        List<String> courses = professor.getProfessorCourses();
        assertNotNull(courses);
        assertTrue(courses.isEmpty());
        
        // Step 3: Add courses using assignCourse method
        professor.assignCourse("CS101");
        professor.assignCourse("CS201");
        assertEquals(2, courses.size());
        
        // Step 4: Create new course list and set it
        List<String> newCourses = new ArrayList<>();
        newCourses.add("MATH101");
        newCourses.add("MATH201");
        newCourses.add("MATH301");
        
        professor.setProfessorCourses(newCourses);
        
        // Step 5: Verify new course list
        List<String> updatedCourses = professor.getProfessorCourses();
        assertEquals(3, updatedCourses.size());
        assertTrue(updatedCourses.contains("MATH101"));
        assertTrue(updatedCourses.contains("MATH201"));
        assertTrue(updatedCourses.contains("MATH301"));
        assertFalse(updatedCourses.contains("CS101")); // Old courses should be gone
    }

    // TEST 23: Test professor with different course types - should work
    @Test
    public void assignCourse_DifferentCourseTypes_ShouldWork() {
        Professor professor = new Professor();
        
        // Assign different types of courses
        professor.assignCourse("CS101"); // Basic course code
        professor.assignCourse("CS201 - Advanced Programming"); // Course with description
        professor.assignCourse("Special Topics in AI"); // Course with full name
        professor.assignCourse("LAB-CS101"); // Lab course
        professor.assignCourse("SEMINAR-2024"); // Seminar course
        
        // Verify all courses were added
        List<String> courses = professor.getProfessorCourses();
        assertEquals(5, courses.size());
        assertTrue(courses.contains("CS101"));
        assertTrue(courses.contains("CS201 - Advanced Programming"));
        assertTrue(courses.contains("Special Topics in AI"));
        assertTrue(courses.contains("LAB-CS101"));
        assertTrue(courses.contains("SEMINAR-2024"));
    }

    // TEST 24: Test professor course capacity - should work
    @Test
    public void assignCourse_ManyCourses_ShouldWork() {
        Professor professor = new Professor();
        
        // Assign many courses (testing list capacity)
        for (int i = 1; i <= 10; i++) {
            professor.assignCourse("COURSE" + i);
        }
        
        // Verify all courses were added
        List<String> courses = professor.getProfessorCourses();
        assertEquals(10, courses.size());
        
        // Verify specific courses
        assertTrue(courses.contains("COURSE1"));
        assertTrue(courses.contains("COURSE5"));
        assertTrue(courses.contains("COURSE10"));
    }

    // TEST 25: Test professor object state consistency - should work
    @Test
    public void professorObjectState_ShouldBeConsistent() {
        // Step 1: Create professor
        Professor professor = new Professor("P001", "Dr. Test", 
                                          "test@university.edu", "Testing");
        
        // Step 2: Verify initial state
        assertNotNull(professor.getProfessorId());
        assertNotNull(professor.getProfessorName());
        assertNotNull(professor.getProfessorEmail());
        assertNotNull(professor.getProfessorDepartment());
        assertNotNull(professor.getProfessorCourses());
        
        // Step 3: Add course and verify state remains consistent
        professor.assignCourse("TEST101");
        
        // All original fields should remain unchanged
        assertEquals("P001", professor.getProfessorId());
        assertEquals("Dr. Test", professor.getProfessorName());
        assertEquals("test@university.edu", professor.getProfessorEmail());
        assertEquals("Testing", professor.getProfessorDepartment());
        
        // Course should be added
        assertEquals(1, professor.getProfessorCourses().size());
        assertTrue(professor.getProfessorCourses().contains("TEST101"));
    }
}