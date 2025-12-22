package com.university.backend.repository;

import com.university.backend.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Transactional
public class MajorReqDataLoadTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository; // Assuming this exists per your request

    @Test
    @Rollback(false)
    public void assignCoursesToMajorAndSetupStudent() {

        // --- PART 1: CLEANUP OLD "CSE" RECORDS ---
        // You mentioned deleting old CSE records that were replaced by CS.
        int deletedReqs = em.createQuery("DELETE FROM MajorReq mr WHERE mr.major.majorId = 'CSE'").executeUpdate();
        int deletedMajor = em.createQuery("DELETE FROM Major m WHERE m.majorId = 'CSE'").executeUpdate();

        if (deletedMajor > 0) {
            System.out.println("CLEANUP: Removed old 'CSE' Major and " + deletedReqs + " requirements.");
        }

        // --- PART 2: ENSURE "CS" MAJOR EXISTS ---
        String majorId = "CS";
        Major major = em.find(Major.class, majorId);

        if (major == null) {
            major = new Major(majorId, "Computer Science");
            em.persist(major);
            System.out.println("Created Major: " + major.getMajorName());
        }

        // --- PART 3: LINK COURSES TO "CS" MAJOR ---
        List<String> courseCodes = Arrays.asList(
                // Year 1
                "PHM013", "PHM022", "CSE111", "CSE131", "PHM113", "EPM118", "EPM211", "ASU112",
                "CSE112", "CSE231", "CSE334", "PHM111", "PHM114", "ASU-EL1",
                // Year 2
                "CSE312", "CSE335", "CSE232", "CSE331", "PHM211", "ECE251",
                "CSE332", "CSE333", "CSE338", "CSE371", "CSE439", "CSE472",
                // Year 3
                "CSE211", "CSE233", "CSE351", "EL3-1", "EPM119", "ASU114", "ASU-EL2",
                "CSE341", "CSE354", "CSE411", "CSE432", "EL3-2", "EL3-3", "ASU111",
                // Year 4
                "CSE336", "CSE431", "CSE441", "EL4-1", "EL4-2", "CSE491",
                "CSE451", "CSE455", "EL4-3", "EL4-4", "CSE492", "ASU113"
        );

        int addedCount = 0;
        for (String code : courseCodes) {
            Course course = em.find(Course.class, code);
            if (course != null) {
                Long count = em.createQuery(
                                "SELECT COUNT(mr) FROM MajorReq mr WHERE mr.major = :m AND mr.course = :c", Long.class)
                        .setParameter("m", major)
                        .setParameter("c", course)
                        .getSingleResult();

                if (count == 0) {
                    MajorReq req = new MajorReq(major, course);
                    em.persist(req);
                    addedCount++;
                }
            }
        }
        em.flush(); // Ensure major data is saved before student logic
        System.out.println("Linked " + addedCount + " courses to Major " + majorId);


        // --- PART 4: SIMULATE STUDENT GRADES (22P0223) ---
        String targetStudentId = "22P0223";
        Student student = studentRepository.findByStudentId(targetStudentId).orElse(null);

        if (student != null) {
            // Create a copy of the list to avoid ConcurrentModificationException during loop
            List<String> currentCourses = new ArrayList<>(student.getCurrentCourses());

            if (!currentCourses.isEmpty()) {
                System.out.println("Found " + currentCourses.size() + " active courses for " + targetStudentId + ". Grading them...");

                for (String code : currentCourses) {
                    Course course = em.find(Course.class, code);
                    if (course != null) {
                        // Random Grade between 2.3 (C+) and 4.0 (A)
                        double randomGrade = ThreadLocalRandom.current().nextDouble(2.3, 4.0);
                        // Format to 2 decimal places implies standard double math, usually fine for display

                        student.addCompletedCourse(
                                course.getCourseName(),
                                randomGrade,
                                course.getCreditHours(),
                                course.getSemester()
                        );
                    }
                }
                // Clear active schedule
                student.getCurrentCourses().clear();
                studentRepository.save(student);
                System.out.println("Student " + targetStudentId + " has finished the semester.");
            }
        } else {
            System.err.println("Student " + targetStudentId + " not found. Skipping grading.");
        }


        // --- PART 5: CREATE USER LOGIN (User Repository) ---
        String userEmail = "2023023"  + "@university.edu"; // 22P0223@university.edu

        // Assuming your User entity has findByEmail or similar
        // If your User entity is different, adjust the setter names below
        if (userRepository.findByEmail(userEmail).isEmpty()) {
            User newUser = new User();
            newUser.setEmail(userEmail);
            newUser.setPassword("null"); // Default password
            newUser.setRole("STUDENT");

            // If your User entity links to Student, set it here:
            // newUser.setStudent(student);

            userRepository.save(newUser);
            System.out.println("Created User login for: " + userEmail);
        } else {
            System.out.println("User login already exists for: " + userEmail);
        }
    }
}