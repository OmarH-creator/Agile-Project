package com.university.backend.repository;

import com.university.backend.entity.Admin;
import com.university.backend.entity.Professor;
import com.university.backend.entity.User;
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
import java.util.List;

@AutoConfigureTestDatabase(replace = Replace.NONE)
@SpringBootTest
public class UserRepoTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    // Assuming you might create an AdminRepository later,
    // but for now we can use EntityManager for Admin if the repo doesn't exist.

    @Test
    @Transactional
    @Rollback(false) // Commit data to the database so you can see it in Workbench/pgAdmin
    public void loadUsersAdminsAndProfessors() {

        // --- 1. Create Users (Authentication data) ---
        List<User> users = new ArrayList<>();
        users.add(createGenericUser("admin@university.edu", "securePass123", "ADMIN"));
        users.add(createGenericUser("dr.smith@university.edu", "pass123", "PROFESSOR"));
        users.add(createGenericUser("dr.jones@university.edu", "pass123", "PROFESSOR"));

        // Save users using Repository
        userRepository.saveAll(users);

        // --- 2. Create Admins ---
        Admin admin1 = new Admin();
        admin1.setAdminId("ADM-001");
        admin1.setEmail("admin@university.edu");
        admin1.setName("System Administrator");

        // Using EM here in case you haven't created AdminRepository yet
        em.persist(admin1);

        // --- 3. Create Professors ---

        // Professor 1
        Professor p1 = new Professor();
        // FIX: This is REQUIRED because your Entity does not have @GeneratedValue
        p1.setProfessorId("PROF-001");
        p1.setProfessorName("Dr. Alice Smith");
        p1.setProfessorEmail("dr.smith@university.edu");
        p1.setProfessorDepartment("Computer Science");

        // Add courses (Logic defined in Professor entity)
        addCourseToProfessor(p1, "CSE111 - Logic Design");
        addCourseToProfessor(p1, "CSE112 - Computer Org");

        // Professor 2
        Professor p2 = new Professor();
        // FIX: This is REQUIRED because your Entity does not have @GeneratedValue
        p2.setProfessorId("PROF-002");
        p2.setProfessorName("Dr. Bob Jones");
        p2.setProfessorEmail("dr.jones@university.edu");
        p2.setProfessorDepartment("Electrical Engineering");

        addCourseToProfessor(p2, "EPM118 - Circuits");
        addCourseToProfessor(p2, "ECE251 - Signals & Systems");

        // Save professors using Repository
        // This handles the @ElementCollection saving automatically
        professorRepository.save(p1);
        professorRepository.save(p2);

        System.out.println("Data Loading Complete: Users, Admins, and Professors created.");
    }

    // --- Helper Methods ---

    private User createGenericUser(String email, String pass, String role) {
        User u = new User();
        u.setEmail(email);
        u.setPassword(pass);
        u.setRole(role);
        return u;
    }

    private void addCourseToProfessor(Professor prof, String courseName) {
        prof.assignCourse(courseName);
    }
}