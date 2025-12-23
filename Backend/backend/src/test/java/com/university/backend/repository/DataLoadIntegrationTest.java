package com.university.backend.repository;

import com.university.backend.entity.Course;
import com.university.backend.entity.CoursePrerequisite;
import com.university.backend.entity.Hall.Hall;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE) // use real DB configured in application.properties
@Transactional
public class DataLoadIntegrationTest {

    @PersistenceContext
    private EntityManager em;

    /*
     * This test persists the course list and hall list you provided.
     * It commits (no rollback) so the rows remain in the configured database.
     *
     * Make sure:
     *  - src/main/resources/application.properties points to your SQL Server
     *  - spring.jpa.hibernate.ddl-auto is update or create (or create the tables manually)
     */
    @Test
    @Rollback(false)
    public void loadCoursesAndHalls_and_assertSaved() {
        // --- Courses data (codes, titles, semester) ---
        // Default creditHours is set to 3 where not specified
        List<Course> courses = Arrays.asList(
                // --- Semester 1: Fall 2022 ---
                new Course("PHM013", "Mechanics", 3, "Fall 2022"),
                new Course("PHM022", "Physics II", 3, "Fall 2022"),
                new Course("CSE111", "Logic Design", 3, "Fall 2022"),
                new Course("CSE131", "Computer Programming", 3, "Fall 2022"),
                new Course("PHM113", "Differential & Partial Differential Equations", 3, "Fall 2022"),
                new Course("EPM118", "Electrical & Electronic Circuits", 3, "Fall 2022"),
                new Course("EPM211", "Properties of Electrical Materials", 3, "Fall 2022"),
                new Course("ASU112", "Report Writing & Communication Skills", 3, "Fall 2022"),

                // --- Semester 2: Spring 2023 ---
                new Course("CSE112", "Computer Organization & Architecture", 3, "Spring 2023"),
                new Course("CSE231", "Advanced Computer Programming", 3, "Spring 2023"),
                new Course("CSE334", "Software Engineering", 3, "Spring 2023"),
                new Course("PHM111", "Probability & Statistics", 3, "Spring 2023"),
                new Course("PHM114", "Numerical Analysis", 3, "Spring 2023"),
                new Course("ASU-EL1", "ASU Elective (1)", 3, "Spring 2023"),

                // --- Semester 3: Fall 2023 ---
                new Course("CSE312", "Electronic Design Automation", 3, "Fall 2023"),
                new Course("CSE335", "Operating Systems", 3, "Fall 2023"),
                new Course("CSE232", "Advanced Software Engineering", 3, "Fall 2023"),
                new Course("CSE331", "Data Structures & Algorithms", 3, "Fall 2023"),
                new Course("PHM211", "Discrete Mathematics", 3, "Fall 2023"),
                new Course("ECE251", "Signals & Systems Fundamentals", 3, "Fall 2023"),

                // --- Semester 4: Spring 2024 ---
                new Course("CSE332", "Design & Analysis of Algorithms", 3, "Spring 2024"),
                new Course("CSE333", "Database Systems", 3, "Spring 2024"),
                new Course("CSE338", "Software Testing, Validation & Verification", 3, "Spring 2024"),
                new Course("CSE371", "Control Engineering", 3, "Spring 2024"),
                new Course("CSE439", "Design of Compilers", 3, "Spring 2024"),
                new Course("CSE472", "Artificial Intelligence", 3, "Spring 2024"),

                // --- Semester 5: Fall 2024 ---
                new Course("CSE211", "Introduction to Embedded Systems", 3, "Fall 2024"),
                new Course("CSE233", "Agile Software Engineering", 3, "Fall 2024"),
                new Course("CSE351", "Computer Networks", 3, "Fall 2024"),
                new Course("EL3-1", "Level-3 Technical Elective (1)", 3, "Fall 2024"),
                new Course("EPM119", "Engineering Economy & Investments", 3, "Fall 2024"),
                new Course("ASU114", "Selected Topics in Contemporary Issues", 3, "Fall 2024"),
                new Course("ASU-EL2", "ASU Elective (2)", 3, "Fall 2024"),

                // --- Semester 6: Spring 2025 ---
                new Course("CSE341", "Internet Programming", 3, "Spring 2025"),
                new Course("CSE354", "Distributed Computing", 3, "Spring 2025"),
                new Course("CSE411", "Real-Time & Embedded Systems Design", 3, "Spring 2025"),
                new Course("CSE432", "Automata & Computability", 3, "Spring 2025"),
                new Course("EL3-2", "Level-3 Technical Elective (2)", 3, "Spring 2025"),
                new Course("EL3-3", "Level-3 Technical Elective (3)", 3, "Spring 2025"),
                new Course("ASU111", "Human Rights", 3, "Spring 2025"),

                // --- Semester 7: Fall 2025 ---
                new Course("CSE336", "Software Design Patterns", 3, "Fall 2025"),
                new Course("CSE431", "Mobile Programming", 3, "Fall 2025"),
                new Course("CSE441", "Software Project Management", 3, "Fall 2025"),
                new Course("EL4-1", "Level-4 Technical Elective (1)", 3, "Fall 2025"),
                new Course("EL4-2", "Level-4 Technical Elective (2)", 3, "Fall 2025"),
                new Course("CSE491", "Graduation Project (1)", 3, "Fall 2025"),

                // --- Semester 8: Spring 2026 ---
                new Course("CSE451", "Computer & Network Security", 3, "Spring 2026"),
                new Course("CSE455", "High-Performance Computing", 3, "Spring 2026"),
                new Course("EL4-3", "Level-4 Technical Elective (3)", 3, "Spring 2026"),
                new Course("EL4-4", "Level-4 Technical Elective (4)", 3, "Spring 2026"),
                new Course("CSE492", "Graduation Project (2)", 3, "Spring 2026"),
                new Course("ASU113", "Professional Ethics & Legislations", 3, "Spring 2026")
        );

        // Persist courses
        for (Course c : courses) {
            // courseCode is PK in Course entity
            em.merge(c);
        }

        // Create prerequisites relationships where applicable.
        // For simplicity we add CoursePrerequisite rows when referenced course exists.
        // Map of courseCode -> Course entity (managed)
        Map<String, Course> courseMap = new HashMap<>();
        for (Course c : courses) {
            courseMap.put(c.getCourseCode(), em.find(Course.class, c.getCourseCode()));
        }

        // Define prerequisites pairs from your JSON (course -> prerequisite)
        String[][] prereqPairs = {
                {"PHM113", "PHM013"},
                {"EPM118", "PHM022"},
                {"EPM211", "PHM022"},
                {"CSE112", "CSE111"},
                {"CSE112", "CSE131"},
                {"CSE231", "CSE131"},
                {"CSE334", "CSE131"},
                {"PHM114", "PHM113"},
                {"CSE312", "CSE112"},
                {"CSE335", "CSE112"},
                {"CSE232", "CSE334"},
                {"CSE331", "CSE231"},
                {"PHM211", "PHM111"},
                {"PHM211", "PHM113"},
                {"ECE251", "PHM111"},
                {"ECE251", "PHM113"},
                {"CSE332", "CSE331"},
                {"CSE333", "CSE331"},
                {"CSE338", "CSE232"},
                {"CSE371", "ECE251"},
                {"CSE439", "CSE131"},
                {"CSE472", "CSE131"},
                {"CSE472", "PHM211"},
                {"CSE211", "CSE131"},
                {"CSE233", "CSE232"},
                {"CSE351", "CSE335"},
                {"CSE341", "CSE231"},
                {"CSE354", "CSE231"},
                {"CSE354", "CSE351"},
                {"CSE411", "CSE211"},
                {"CSE432", "CSE332"},
                {"CSE336", "CSE232"},
                {"CSE431", "CSE341"},
                {"CSE441", "CSE334"}, // note: standing rule - no specific prereq insertion here
                {"CSE451", "CSE351"},
                {"CSE455", "CSE112"},
                {"CSE492", "CSE491"}
        };

        for (String[] pair : prereqPairs) {
            String courseCode = pair[0];
            String prereqCode = pair[1];
            Course courseEntity = courseMap.get(courseCode);
            Course prereqEntity = courseMap.get(prereqCode);
            if (courseEntity != null && prereqEntity != null) {
                CoursePrerequisite cp = new CoursePrerequisite();
                cp.setCourse(courseEntity);
                cp.setPrerequisite(prereqEntity);
                em.persist(cp);
            }
        }

        // --- Halls data ---
        // You provided grouped hall names and capacities. Insert a representative list.
        List<Hall> halls = new ArrayList<>();
        // Main building small rooms
        halls.add(new Hall("219", 80));
        halls.add(new Hall("338", 80));
        halls.add(new Hall("346", 80));
        halls.add(new Hall("347", 80));
        halls.add(new Hall("348", 80));
        halls.add(new Hall("350", 80));

        // Credit building rooms (capacity 60)
        String[] creditRooms = {"911","911A","912","913","914","914A","921","921A","922","923","924","924A","931","931A","932","933","941","941A","942","943","944","944A"};
        for (String r : creditRooms) {
            halls.add(new Hall(r, 60));
        }


        // Halls with capacity 180
        halls.add(new Hall("Hall 1", 180));
        halls.add(new Hall("Hall 2", 180));
        halls.add(new Hall("Hall 3", 180));
        halls.add(new Hall("Hall 4", 180));
        // Another set
        halls.add(new Hall("Hall A", 180));
        halls.add(new Hall("Hall B", 180));
        halls.add(new Hall("Hall C", 180));
        halls.add(new Hall("Hall D", 180));

        // Architecture building rooms capacity 250
        halls.add(new Hall("500", 250));
        halls.add(new Hall("501", 250));
        halls.add(new Hall("502", 250));
        halls.add(new Hall("504", 250));
        halls.add(new Hall("505", 250));


        for (Hall h : halls) {
            em.persist(h);
        }

        // flush to DB
        em.flush();

        // Assertions: basic counts and lookups
        Long courseCount = em.createQuery("select count(c) from Course c", Long.class).getSingleResult();
        assertEquals(courses.size(), courseCount.intValue(), "course count should match");

        Long hallCount = em.createQuery("select count(h) from Hall h", Long.class).getSingleResult();
        assertTrue(hallCount >= halls.size(), "at least the inserted halls should exist");

        // sample lookup
        Course found = em.find(Course.class, "CSE111");
        assertNotNull(found, "CSE111 should be persisted and retrievable");

        Hall foundHall = em.createQuery("select h from Hall h where h.hallName = :n", Hall.class)
                .setParameter("n", "Hall 1")
                .getResultStream().findFirst().orElse(null);
        assertNotNull(foundHall, "Hall 1 should be persisted");
    }
}