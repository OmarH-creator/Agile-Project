package com.university.backend.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.ArrayList;


public class AdminTest {

    // ==========================================
    // CONSTRUCTOR TESTS
    // ==========================================

    // TEST 1: Create admin with default constructor - should work
    @Test
    public void createAdmin_DefaultConstructor_ShouldWork() {
        // Step 1: Create admin with default constructor
        Admin admin = new Admin();
        
        // Step 2: Check that object was created
        assertNotNull(admin);
        
        // Step 3: Check that fields are initially null (as expected)
        assertNull(admin.getAdminId());
        assertNull(admin.getName());
        assertNull(admin.getEmail());
        
        // Step 4: Check that assigned courses list is initialized
        assertNotNull(admin.getAssignedCourses());
        assertTrue(admin.getAssignedCourses().isEmpty());
    }

    // TEST 2: Create admin with full constructor - should work
    @Test
    public void createAdmin_FullConstructor_ShouldWork() {
        // Step 1: Create admin with full constructor
        Admin admin = new Admin("A001", "John Admin", "john.admin@university.edu");
        
        // Step 2: Check that all fields were set correctly
        assertEquals("A001", admin.getAdminId());
        assertEquals("John Admin", admin.getName());
        assertEquals("john.admin@university.edu", admin.getEmail());
        
        // Step 3: Check that assigned courses list is initialized
        assertNotNull(admin.getAssignedCourses());
        assertTrue(admin.getAssignedCourses().isEmpty());
    }

    // ==========================================
    // GETTER AND SETTER TESTS
    // ==========================================

    // TEST 3: Test admin ID getter and setter - should work
    @Test
    public void adminIdGetterSetter_ValidId_ShouldWork() {
        // Step 1: Create admin
        Admin admin = new Admin();
        
        // Step 2: Set admin ID using setter
        admin.setAdminId("A12345");
        
        // Step 3: Get admin ID using getter and verify
        assertEquals("A12345", admin.getAdminId());
    }

    // TEST 4: Test admin name getter and setter - should work
    @Test
    public void adminNameGetterSetter_ValidName_ShouldWork() {
        // Step 1: Create admin
        Admin admin = new Admin();
        
        // Step 2: Set name using setter
        admin.setName("Jane Administrator");
        
        // Step 3: Get name using getter and verify
        assertEquals("Jane Administrator", admin.getName());
    }

    // TEST 5: Test admin email getter and setter - should work
    @Test
    public void adminEmailGetterSetter_ValidEmail_ShouldWork() {
        // Step 1: Create admin
        Admin admin = new Admin();
        
        // Step 2: Set email using setter
        admin.setEmail("jane.admin@university.edu");
        
        // Step 3: Get email using getter and verify
        assertEquals("jane.admin@university.edu", admin.getEmail());
    }

    // TEST 6: Test assigned courses getter and setter - should work
    @Test
    public void assignedCoursesGetterSetter_ValidCourses_ShouldWork() {
        // Step 1: Create admin
        Admin admin = new Admin();
        
        // Step 2: Create course list
        List<String> courses = new ArrayList<>();
        courses.add("CS101");
        courses.add("MATH201");
        
        // Step 3: Set courses using setter
        admin.setAssignedCourses(courses);
        
        // Step 4: Get courses using getter and verify
        List<String> retrievedCourses = admin.getAssignedCourses();
        assertEquals(2, retrievedCourses.size());
        assertTrue(retrievedCourses.contains("CS101"));
        assertTrue(retrievedCourses.contains("MATH201"));
    }

    // ==========================================
    // COURSE ASSIGNMENT TESTS
    // ==========================================

    // TEST 7: Test adding single assigned course - should work
    @Test
    public void addAssignedCourse_SingleCourse_ShouldWork() {
        // Step 1: Create admin
        Admin admin = new Admin();
        
        // Step 2: Add an assigned course
        admin.addAssignedCourse("CS101");
        
        // Step 3: Check that course was added
        List<String> courses = admin.getAssignedCourses();
        assertEquals(1, courses.size());
        assertTrue(courses.contains("CS101"));
    }

    // TEST 8: Test adding multiple assigned courses - should work
    @Test
    public void addAssignedCourse_MultipleCourses_ShouldWork() {
        // Step 1: Create admin
        Admin admin = new Admin();
        
        // Step 2: Add multiple assigned courses
        admin.addAssignedCourse("CS101");
        admin.addAssignedCourse("CS201");
        admin.addAssignedCourse("MATH301");
        
        // Step 3: Check that all courses were added
        List<String> courses = admin.getAssignedCourses();
        assertEquals(3, courses.size());
        assertTrue(courses.contains("CS101"));
        assertTrue(courses.contains("CS201"));
        assertTrue(courses.contains("MATH301"));
    }

    // TEST 9: Test adding duplicate assigned courses - should work (allows duplicates)
    @Test
    public void addAssignedCourse_DuplicateCourses_ShouldWork() {
        // Step 1: Create admin
        Admin admin = new Admin();
        
        // Step 2: Add same course twice
        admin.addAssignedCourse("CS101");
        admin.addAssignedCourse("CS101");
        
        // Step 3: Check that both instances were added (List allows duplicates)
        List<String> courses = admin.getAssignedCourses();
        assertEquals(2, courses.size());
        assertTrue(courses.contains("CS101"));
    }

    // TEST 10: Test adding assigned course to null list - should work
    @Test
    public void addAssignedCourse_NullList_ShouldWork() {
        // Step 1: Create admin
        Admin admin = new Admin();
        
        // Step 2: Set assigned courses to null
        admin.setAssignedCourses(null);
        
        // Step 3: Add course (should initialize list)
        admin.addAssignedCourse("CS101");
        
        // Step 4: Check that list was initialized and course was added
        List<String> courses = admin.getAssignedCourses();
        assertNotNull(courses);
        assertEquals(1, courses.size());
        assertTrue(courses.contains("CS101"));
    }

    // ==========================================
    // FIELD VALIDATION TESTS
    // ==========================================

    // TEST 11: Test valid admin ID formats - should work
    @Test
    public void setAdminId_ValidFormats_ShouldWork() {
        Admin admin = new Admin();
        
        // Test numeric ID
        admin.setAdminId("12345");
        assertEquals("12345", admin.getAdminId());
        
        // Test alphanumeric ID
        admin.setAdminId("A12345");
        assertEquals("A12345", admin.getAdminId());
        
        // Test ID with dashes
        admin.setAdminId("ADMIN-2024-001");
        assertEquals("ADMIN-2024-001", admin.getAdminId());
        
        // Test department prefix
        admin.setAdminId("IT-A001");
        assertEquals("IT-A001", admin.getAdminId());
    }

    // TEST 12: Test valid email formats - should work
    @Test
    public void setAdminEmail_ValidFormats_ShouldWork() {
        Admin admin = new Admin();
        
        // Test university admin email
        admin.setEmail("admin@university.edu");
        assertEquals("admin@university.edu", admin.getEmail());
        
        // Test department admin email
        admin.setEmail("cs.admin@university.edu");
        assertEquals("cs.admin@university.edu", admin.getEmail());
        
        // Test admin email with numbers
        admin.setEmail("admin123@university.edu");
        assertEquals("admin123@university.edu", admin.getEmail());
        
        // Test system admin email
        admin.setEmail("system.admin@university.edu");
        assertEquals("system.admin@university.edu", admin.getEmail());
    }

    // TEST 13: Test different admin name formats - should work
    @Test
    public void setAdminName_ValidFormats_ShouldWork() {
        Admin admin = new Admin();
        
        // Test simple name
        admin.setName("John Smith");
        assertEquals("John Smith", admin.getName());
        
        // Test name with title
        admin.setName("Dr. Jane Doe");
        assertEquals("Dr. Jane Doe", admin.getName());
        
        // Test name with middle initial
        admin.setName("Michael J. Johnson");
        assertEquals("Michael J. Johnson", admin.getName());
        
        // Test name with suffix
        admin.setName("Robert Wilson Jr.");
        assertEquals("Robert Wilson Jr.", admin.getName());
    }

    // ==========================================
    // EDGE CASE TESTS
    // ==========================================

    // TEST 14: Test null field handling - should work (but not recommended)
    @Test
    public void setFields_NullValues_ShouldWork() {
        Admin admin = new Admin();
        
        // Set null values (entity allows it, database will reject it)
        admin.setAdminId(null);
        admin.setName(null);
        admin.setEmail(null);
        admin.setAssignedCourses(null);
        
        // Should store null values in object
        assertNull(admin.getAdminId());
        assertNull(admin.getName());
        assertNull(admin.getEmail());
        assertNull(admin.getAssignedCourses());
    }

    // TEST 15: Test empty string handling - should work
    @Test
    public void setFields_EmptyStrings_ShouldWork() {
        Admin admin = new Admin();
        
        // Set empty strings
        admin.setAdminId("");
        admin.setName("");
        admin.setEmail("");
        
        // Should store empty strings
        assertEquals("", admin.getAdminId());
        assertEquals("", admin.getName());
        assertEquals("", admin.getEmail());
    }

    // TEST 16: Test very long field values - should work (entity level)
    @Test
    public void setFields_VeryLongValues_ShouldWork() {
        Admin admin = new Admin();
        
        // Create very long values
        String longName = "VeryLongAdministratorNameThatExceedsTypicalLengthLimits";
        String longEmail = "verylongadministratoremailaddress@verylongdomainname.university.edu";
        
        admin.setName(longName);
        admin.setEmail(longEmail);
        
        assertEquals(longName, admin.getName());
        assertEquals(longEmail, admin.getEmail());
    }

    // TEST 17: Test null course assignment - should handle gracefully
    @Test
    public void addAssignedCourse_NullCourse_ShouldWork() {
        Admin admin = new Admin();
        
        // Add null course (should add null to list)
        admin.addAssignedCourse(null);
        
        // Check that null was added to course list
        List<String> courses = admin.getAssignedCourses();
        assertEquals(1, courses.size());
        assertTrue(courses.contains(null));
    }

    // TEST 18: Test empty course assignment - should work
    @Test
    public void addAssignedCourse_EmptyCourse_ShouldWork() {
        Admin admin = new Admin();
        
        // Add empty course name
        admin.addAssignedCourse("");
        
        // Check that empty string was added to course list
        List<String> courses = admin.getAssignedCourses();
        assertEquals(1, courses.size());
        assertTrue(courses.contains(""));
    }

    // ==========================================
    // BUSINESS LOGIC TESTS
    // ==========================================

    // TEST 19: Test complete admin creation workflow - should work
    @Test
    public void createCompleteAdmin_AllFields_ShouldWork() {
        // Step 1: Create admin with constructor
        Admin admin = new Admin("A001", "Alice Administrator", 
                               "alice.admin@university.edu");
        
        // Step 2: Add assigned courses
        admin.addAssignedCourse("CS101 - Introduction to Programming");
        admin.addAssignedCourse("CS201 - Data Structures");
        admin.addAssignedCourse("MATH301 - Calculus");
        
        // Step 3: Verify all fields are correct
        assertEquals("A001", admin.getAdminId());
        assertEquals("Alice Administrator", admin.getName());
        assertEquals("alice.admin@university.edu", admin.getEmail());
        
        // Step 4: Verify courses are assigned
        List<String> courses = admin.getAssignedCourses();
        assertEquals(3, courses.size());
        assertTrue(courses.contains("CS101 - Introduction to Programming"));
        assertTrue(courses.contains("CS201 - Data Structures"));
        assertTrue(courses.contains("MATH301 - Calculus"));
    }

    // TEST 20: Test admin field updates - should work
    @Test
    public void updateAdminFields_ExistingAdmin_ShouldWork() {
        // Step 1: Create initial admin
        Admin admin = new Admin("A001", "Old Name", "old@email.com");
        
        // Step 2: Update fields
        admin.setAdminId("A002");
        admin.setName("New Name");
        admin.setEmail("new@email.com");
        
        // Step 3: Verify updates worked
        assertEquals("A002", admin.getAdminId());
        assertEquals("New Name", admin.getName());
        assertEquals("new@email.com", admin.getEmail());
    }

    // TEST 21: Test course list management - should work
    @Test
    public void courseListManagement_ShouldWork() {
        // Step 1: Create admin
        Admin admin = new Admin();
        
        // Step 2: Test initial course list
        List<String> courses = admin.getAssignedCourses();
        assertNotNull(courses);
        assertTrue(courses.isEmpty());
        
        // Step 3: Add courses using addAssignedCourse method
        admin.addAssignedCourse("CS101");
        admin.addAssignedCourse("CS201");
        assertEquals(2, courses.size());
        
        // Step 4: Create new course list and set it
        List<String> newCourses = new ArrayList<>();
        newCourses.add("ADMIN101");
        newCourses.add("ADMIN201");
        newCourses.add("ADMIN301");
        
        admin.setAssignedCourses(newCourses);
        
        // Step 5: Verify new course list
        List<String> updatedCourses = admin.getAssignedCourses();
        assertEquals(3, updatedCourses.size());
        assertTrue(updatedCourses.contains("ADMIN101"));
        assertTrue(updatedCourses.contains("ADMIN201"));
        assertTrue(updatedCourses.contains("ADMIN301"));
        assertFalse(updatedCourses.contains("CS101")); // Old courses should be gone
    }

    // TEST 22: Test admin with different course types - should work
    @Test
    public void addAssignedCourse_DifferentCourseTypes_ShouldWork() {
        Admin admin = new Admin();
        
        // Add different types of courses
        admin.addAssignedCourse("CS101"); // Basic course code
        admin.addAssignedCourse("ADMIN201 - System Administration"); // Course with description
        admin.addAssignedCourse("Database Management"); // Course with full name
        admin.addAssignedCourse("WORKSHOP-2024"); // Workshop course
        admin.addAssignedCourse("TRAINING-SECURITY"); // Training course
        
        // Verify all courses were added
        List<String> courses = admin.getAssignedCourses();
        assertEquals(5, courses.size());
        assertTrue(courses.contains("CS101"));
        assertTrue(courses.contains("ADMIN201 - System Administration"));
        assertTrue(courses.contains("Database Management"));
        assertTrue(courses.contains("WORKSHOP-2024"));
        assertTrue(courses.contains("TRAINING-SECURITY"));
    }

    // TEST 23: Test admin course capacity - should work
    @Test
    public void addAssignedCourse_ManyCourses_ShouldWork() {
        Admin admin = new Admin();
        
        // Add many courses (testing list capacity)
        for (int i = 1; i <= 15; i++) {
            admin.addAssignedCourse("COURSE" + i);
        }
        
        // Verify all courses were added
        List<String> courses = admin.getAssignedCourses();
        assertEquals(15, courses.size());
        
        // Verify specific courses
        assertTrue(courses.contains("COURSE1"));
        assertTrue(courses.contains("COURSE8"));
        assertTrue(courses.contains("COURSE15"));
    }

    // TEST 24: Test admin object state consistency - should work
    @Test
    public void adminObjectState_ShouldBeConsistent() {
        // Step 1: Create admin
        Admin admin = new Admin("A001", "Test Admin", "test@university.edu");
        
        // Step 2: Verify initial state
        assertNotNull(admin.getAdminId());
        assertNotNull(admin.getName());
        assertNotNull(admin.getEmail());
        assertNotNull(admin.getAssignedCourses());
        
        // Step 3: Add course and verify state remains consistent
        admin.addAssignedCourse("TEST101");
        
        // All original fields should remain unchanged
        assertEquals("A001", admin.getAdminId());
        assertEquals("Test Admin", admin.getName());
        assertEquals("test@university.edu", admin.getEmail());
        
        // Course should be added
        assertEquals(1, admin.getAssignedCourses().size());
        assertTrue(admin.getAssignedCourses().contains("TEST101"));
    }

    // TEST 25: Test admin permissions concept - should work
    @Test
    public void adminPermissions_ConceptualTest_ShouldWork() {
        // Step 1: Create admin with different roles
        Admin systemAdmin = new Admin("SYS001", "System Administrator", "sys@university.edu");
        Admin deptAdmin = new Admin("DEPT001", "Department Administrator", "dept@university.edu");
        Admin courseAdmin = new Admin("COURSE001", "Course Administrator", "course@university.edu");
        
        // Step 2: Assign different types of courses based on admin type
        systemAdmin.addAssignedCourse("SYSTEM-MAINTENANCE");
        systemAdmin.addAssignedCourse("USER-MANAGEMENT");
        
        deptAdmin.addAssignedCourse("CS101");
        deptAdmin.addAssignedCourse("CS201");
        
        courseAdmin.addAssignedCourse("CURRICULUM-DESIGN");
        courseAdmin.addAssignedCourse("COURSE-SCHEDULING");
        
        // Step 3: Verify each admin has appropriate courses
        assertTrue(systemAdmin.getAssignedCourses().contains("SYSTEM-MAINTENANCE"));
        assertTrue(deptAdmin.getAssignedCourses().contains("CS101"));
        assertTrue(courseAdmin.getAssignedCourses().contains("CURRICULUM-DESIGN"));
        
        // Step 4: Verify admins are distinct
        assertNotEquals(systemAdmin.getAdminId(), deptAdmin.getAdminId());
        assertNotEquals(systemAdmin.getEmail(), courseAdmin.getEmail());
    }
}