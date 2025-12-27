package com.university.backend.repository;

import com.university.backend.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SUPER SIMPLE UserRepository Tests
 * 
 * These tests are written in the most basic way possible.
 * No complex setup, just simple save/find/delete operations.
 */
@DataJpaTest
@ActiveProfiles("test")
public class SimpleUserRepoTest {

    @Autowired
    private UserRepository userRepository;

    // TEST 1: Save a user and find it back
    @Test
    public void testSaveUser_ThenFindIt_ShouldWork() {
        // Step 1: Create a new user
        User user = new User();
        user.setEmail("john@example.com");
        user.setPassword("password123");
        user.setRole("STUDENT");

        // Step 2: Save the user
        User savedUser = userRepository.save(user);

        // Step 3: Check that it was saved
        assertNotNull(savedUser);
        assertNotNull(savedUser.getId());
        assertEquals("john@example.com", savedUser.getEmail());
    }

    // TEST 2: Find user by email
    @Test
    public void testFindUserByEmail_ShouldWork() {
        // Step 1: Save a user first
        User user = new User();
        user.setEmail("jane@example.com");
        user.setPassword("mypassword");
        user.setRole("ADMIN");
        userRepository.save(user);

        // Step 2: Try to find the user by email
        Optional<User> foundUser = userRepository.findByEmail("jane@example.com");

        // Step 3: Check that we found the user
        assertTrue(foundUser.isPresent());
        assertEquals("jane@example.com", foundUser.get().getEmail());
        assertEquals("ADMIN", foundUser.get().getRole());
    }

    // TEST 3: Try to find user that doesn't exist
    @Test
    public void testFindNonExistentUser_ShouldReturnEmpty() {
        // Step 1: Try to find user that doesn't exist
        Optional<User> foundUser = userRepository.findByEmail("doesnotexist@example.com");

        // Step 2: Should be empty
        assertFalse(foundUser.isPresent());
    }

    // TEST 4: Delete a user
    @Test
    public void testDeleteUser_ShouldWork() {
        // Step 1: Save a user first
        User user = new User();
        user.setEmail("delete@example.com");
        user.setPassword("password");
        user.setRole("STUDENT");
        User savedUser = userRepository.save(user);

        // Step 2: Delete the user
        userRepository.delete(savedUser);

        // Step 3: Try to find the user - should not exist
        Optional<User> foundUser = userRepository.findByEmail("delete@example.com");
        assertFalse(foundUser.isPresent());
    }

    // TEST 5: Count users
    @Test
    public void testCountUsers_ShouldWork() {
        // Step 1: Check how many users we have initially
        long initialCount = userRepository.count();

        // Step 2: Add 3 users
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setPassword("pass1");
        user1.setRole("STUDENT");
        userRepository.save(user1);

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setPassword("pass2");
        user2.setRole("ADMIN");
        userRepository.save(user2);

        User user3 = new User();
        user3.setEmail("user3@example.com");
        user3.setPassword("pass3");
        user3.setRole("PROFESSOR");
        userRepository.save(user3);

        // Step 3: Count should be initial + 3
        long finalCount = userRepository.count();
        assertEquals(initialCount + 3, finalCount);
    }

    // TEST 6: Find all users
    @Test
    public void testFindAllUsers_ShouldWork() {
        // Step 1: Add 2 users
        User user1 = new User();
        user1.setEmail("all1@example.com");
        user1.setPassword("pass1");
        user1.setRole("STUDENT");
        userRepository.save(user1);

        User user2 = new User();
        user2.setEmail("all2@example.com");
        user2.setPassword("pass2");
        user2.setRole("ADMIN");
        userRepository.save(user2);

        // Step 2: Find all users
        List<User> allUsers = userRepository.findAll();

        // Step 3: Should have at least our 2 users
        assertTrue(allUsers.size() >= 2);
        
        // Check that our users are in the list
        boolean foundUser1 = allUsers.stream().anyMatch(u -> u.getEmail().equals("all1@example.com"));
        boolean foundUser2 = allUsers.stream().anyMatch(u -> u.getEmail().equals("all2@example.com"));
        assertTrue(foundUser1);
        assertTrue(foundUser2);
    }

    // TEST 7: Update a user
    @Test
    public void testUpdateUser_ShouldWork() {
        // Step 1: Save a user
        User user = new User();
        user.setEmail("update@example.com");
        user.setPassword("oldpassword");
        user.setRole("STUDENT");
        User savedUser = userRepository.save(user);

        // Step 2: Update the user
        savedUser.setPassword("newpassword");
        savedUser.setRole("ADMIN");
        User updatedUser = userRepository.save(savedUser);

        // Step 3: Check that changes were saved
        assertEquals("newpassword", updatedUser.getPassword());
        assertEquals("ADMIN", updatedUser.getRole());

        // Step 4: Find the user again to double-check
        Optional<User> foundUser = userRepository.findByEmail("update@example.com");
        assertTrue(foundUser.isPresent());
        assertEquals("newpassword", foundUser.get().getPassword());
        assertEquals("ADMIN", foundUser.get().getRole());
    }
}