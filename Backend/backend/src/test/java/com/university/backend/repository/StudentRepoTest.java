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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE) // Use the real DB connection
@Rollback(false) // <--- IMPORTANT: Commits changes to DB so you can see them later
public class StudentRepoTest {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private MajorRepository majorRepository;

    private final Random random = new Random();

    // --- DATA POOLS FOR GENERATION ---
    private final String[] FIRST_NAMES = {
            "James", "Mary", "Robert", "Patricia", "John", "Jennifer", "Michael", "Linda",
            "David", "Elizabeth", "William", "Barbara", "Richard", "Susan", "Joseph", "Jessica",
            "Thomas", "Sarah", "Charles", "Karen", "Christopher", "Nancy", "Daniel", "Lisa",
            "Matthew", "Betty", "Anthony", "Margaret", "Mark", "Sandra"
    };

    private final String[] LAST_NAMES = {
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
            "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson",
            "Thomas", "Taylor", "Moore", "Jackson", "Martin", "Lee", "Perez", "Thompson",
            "White", "Harris", "Sanchez", "Clark", "Ramirez", "Lewis", "Robinson"
    };

    private final String[] STREETS = {
            "Maple Ave", "Oak St", "Washington Blvd", "Lakeview Dr", "Cedar Ln", "Park Place",
            "Sunset Blvd", "Broadway", "Main St", "Highland Ave"
    };

    // Realistic Course Catalog with Codes
    private final String[][] COURSE_CATALOG = {
            {"CS101", "Intro to Computer Science"}, {"CS102", "Data Structures"}, {"CS201", "Algorithms"},
            {"CS305", "Database Systems"}, {"CS310", "Operating Systems"}, {"CS400", "AI Fundamentals"},
            {"MATH101", "Calculus I"}, {"MATH102", "Calculus II"}, {"MATH201", "Linear Algebra"},
            {"MATH300", "Discrete Math"}, {"PHY101", "General Physics I"}, {"PHY102", "General Physics II"},
            {"ENG101", "Academic Writing"}, {"HIST100", "World History"}, {"PSY101", "Intro to Psychology"},
            {"ECO101", "Microeconomics"}, {"ECO102", "Macroeconomics"}, {"ART100", "Art Appreciation"},
            {"CHEM101", "General Chemistry"}, {"BIO101", "Intro to Biology"}, {"STAT200", "Statistics"},
            {"PHIL101", "Intro to Philosophy"}, {"SOC101", "Intro to Sociology"}, {"MGT101", "Business Mgmt"}
    };

    private final String[] SEMESTERS = {"Fall 2021", "Spring 2022", "Fall 2022", "Spring 2023", "Fall 2023", "Spring 2024"};
    private final double[] GRADES = {4.0, 3.7, 3.3, 3.0, 2.7, 2.3, 2.0, 1.7}; // A to C-
    private final String[] MILITARY_STATUS = {"Exempt", "Completed", "Postponed", "Not Applicable"};

    @Test
    void populateMassiveStudentData() {
        System.out.println("--- STARTING DATA POPULATION ---");

        // 1. Create Majors
        List<Major> majors = new ArrayList<>();
        majors.add(new Major("CS", "Computer Science"));
        majors.add(new Major("ENG", "Engineering"));
        majors.add(new Major("BUS", "Business Admin"));
        majors.add(new Major("ART", "Fine Arts"));
        majors.add(new Major("SCI", "Data Science"));

        majorRepository.saveAll(majors);
        System.out.println("--> Saved 5 Majors");

        List<Student> studentsToSave = new ArrayList<>();

        // 2. Generate 25 Students
        for (int i = 1; i <= 25; i++) {
            // Pick Random Major
            Major randomMajor = majors.get(random.nextInt(majors.size()));

            // Generate Name
            String fName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
            String lName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
            String fullName = fName + " " + lName;

            // Generate ID (e.g., 2023001, 2023002...)
            String studentId = String.format("2023%03d", i);
            String email = fName.toLowerCase() + "." + lName.toLowerCase() + i + "@uni.edu";
            String phone = "555-" + String.format("%04d", random.nextInt(10000));
            String address = (random.nextInt(900) + 100) + " " + STREETS[random.nextInt(STREETS.length)];
            String milStatus = MILITARY_STATUS[random.nextInt(MILITARY_STATUS.length)];

            Student s = new Student(studentId, fullName, email, randomMajor, phone, address, new Date(), milStatus);

            // 3. Add 15 to 20 Completed Courses for each student
            int coursesCount = random.nextInt(6) + 15; // Generates 15 to 20

            // Use a Set to avoid adding the same course twice for one student
            Set<Integer> usedCourseIndices = new HashSet<>();

            for (int j = 0; j < coursesCount; j++) {
                int courseIdx;
                // Find a unique course they haven't taken yet
                do {
                    courseIdx = random.nextInt(COURSE_CATALOG.length);
                } while (usedCourseIndices.contains(courseIdx));

                usedCourseIndices.add(courseIdx);

                String[] courseData = COURSE_CATALOG[courseIdx];
                String courseCode = courseData[0];
                // We append the title to the code or handle it however your entity expects.
                // Assuming addCompletedCourse takes (code, grade, credits, term)
                // If your entity stores title inside the record, adjust accordingly.

                double gpa = GRADES[random.nextInt(GRADES.length)];
                int credits = (courseCode.startsWith("CS") || courseCode.startsWith("MATH")) ? 4 : 3;
                String semester = SEMESTERS[random.nextInt(SEMESTERS.length)];

                // Add to student entity
                // NOTE: I am adding the Course Title into the 'code' string for visibility
                // if your backend requires title separately, update your addCompletedCourse method signature.
                // Here I assume standard signature: addCompletedCourse(code, grade, credits, semester)
                // If your method allows title, pass courseData[1] as well.
                s.addCompletedCourse(courseCode + " - " + courseData[1], gpa, credits, semester);
            }

            // 4. Enroll in 2 current courses
            s.enrollCourse("CS499 - Senior Project");
            s.enrollCourse("ETHICS101 - Professional Ethics");

            studentsToSave.add(s);
        }

        studentRepository.saveAll(studentsToSave);

        System.out.println("--- FINISHED: Added " + studentsToSave.size() + " students with full academic history. ---");
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