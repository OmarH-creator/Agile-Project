package com.university.backend.repository;

import com.university.backend.entity.Major;
import com.university.backend.entity.Student;
import com.university.backend.entity.Course_record;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE) // Use the real DB connection
@Rollback(false) // <--- IMPORTANT: Commits changes to DB so you can see them later
public class StudentRepoTest {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private MajorRepository majorRepository;

    @Test
    void populateRichStudentData() {
        // --- 1. Setup Majors ---
        // We need majors first because of the Foreign Key constraint
        Major cs = new Major("CS", "Computer Science");
        Major ee = new Major("EE", "Electrical Engineering");
        Major ba = new Major("BA", "Business Administration");
        Major arch = new Major("ARCH", "Architecture");

        majorRepository.saveAll(Arrays.asList(cs, ee, ba, arch));

        // --- 2. Create Multiple Students ---

        // Student 1: CS Student, Senior, High GPA
        Student s1 = new Student(
                "S-101", "Alice Smith", "alice@uni.edu", cs,
                "555-0001", "123 Tech Ave", new Date(), "Exempt"
        );
        s1.addCompletedCourse("CS101", 4.0, 3,"Fall 2024");
        s1.addCompletedCourse("MATH201", 3.8, 3,"Fall 2025");
        s1.enrollCourse("CS300 - Algorithms");

        // Student 2: EE Student, Freshman
        Student s2 = new Student(
                "S-102", "Bob Jones", "bob@uni.edu", ee,
                "555-0002", "456 Circuit Rd", new Date(), "Completed"
        );
        s2.addCompletedCourse("PHY101", 3.5, 4,"Fall 2024");
        s2.enrollCourse("EE110 - Intro to Circuits");

        // Student 3: Business Student, Probation
        Student s3 = new Student(
                "S-103", "Charlie Brown", "charlie@uni.edu", ba,
                "555-0003", "789 Market St", new Date(), "Postponed"
        );
        s3.addCompletedCourse("MGT101", 2.0, 3,"Spring 2024"); // Low grade
        s3.enrollCourse("ACC200 - Accounting");

        // Student 4: Architecture, International
        Student s4 = new Student(
                "S-104", "Diana Prince", "diana@uni.edu", arch,
                "555-0004", "321 Design Blvd", new Date(), "Not Applicable"
        );
        s4.addCompletedCourse("ART101", 4.0, 2,"Fall 2023");
        s4.addCompletedCourse("ARCH100", 3.9, 4,"Fall 2023");

        // Student 5: CS Student, Transfer
        Student s5 = new Student(
                "S-105", "Evan Wright", "evan@uni.edu", cs,
                "555-0005", "654 Code Ln", new Date(), "Exempt"
        );
        // No completed courses yet at this uni

        // --- 3. Save All to Database ---
        List<Student> students = Arrays.asList(s1, s2, s3, s4, s5);
        studentRepository.saveAll(students);

        System.out.println("--- Data Population Complete: Added " + students.size() + " students. ---");

        // --- 4. Assertions (Just to keep it a valid test) ---
        assertEquals(5, studentRepository.count());
        assertTrue(studentRepository.existsByStudentId("S-103"));
    }

    @Test
    void testFindStudentByBusinessId_Success() {
        // Create specific data for this test to remain independent
        Major math = new Major("MATH", "Mathematics");
        majorRepository.save(math);

        Student temp = new Student("S-TEST", "Test User", "test@uni.edu", math, "000-000", "N/A", new Date(), "N/A");
        studentRepository.save(temp);

        Optional<Student> found = studentRepository.findByStudentId("S-TEST");

        assertTrue(found.isPresent());
        assertEquals("Test User", found.get().getName());
    }

    @Test
    void testDeleteByStudentId_Success() {
        Major hist = new Major("HIST", "History");
        majorRepository.save(hist);

        Student temp = new Student("S-DEL", "Delete Me", "del@uni.edu", hist, "111-111", "N/A", new Date(), "N/A");
        studentRepository.save(temp);

        assertTrue(studentRepository.existsByStudentId("S-DEL"));
        studentRepository.deleteByStudentId("S-DEL");

        // We must flush to force the delete SQL to run immediately
        studentRepository.flush();

        assertFalse(studentRepository.existsByStudentId("S-DEL"));
    }
}