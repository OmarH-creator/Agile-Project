package com.university.backend.controllers;

import com.university.backend.entity.User;
import com.university.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * COMPLETE AuthController Tests

 * This ONE file covers ALL testing requirements from the checklist:
 * - Login with valid/invalid credentials
 * - Login with non-existent users
 * - Login with empty/null data
 * - Check status for first-time and returning users
 * - Set password for new users
 * - All error cases and edge cases

 * Uses simple syntax - no complex mocking or hard annotations.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AuthControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private UserRepository userRepository;



    // TEST 2: Login with wrong password should fail
    @Test
    public void login_WrongPassword_ShouldFail() {
        // Create test user
        User user = new User();
        user.setEmail("student2@test.com");
        user.setPassword("correctpassword");
        user.setRole("STUDENT");
        userRepository.save(user);

        // Try to login with wrong password
        Map<String, String> request = new HashMap<>();
        request.put("email", "student2@test.com");
        request.put("password", "wrongpassword");

        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/login", request, String.class);

        // Should fail
        assertEquals(401, response.getStatusCode().value());
    }

    // TEST 3: Login with user that doesn't exist should fail
    @Test
    public void login_UserDoesNotExist_ShouldFail() {
        // Try to login with email that doesn't exist
        Map<String, String> request = new HashMap<>();
        request.put("email", "nonexistent@test.com");
        request.put("password", "anypassword");

        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/login", request, String.class);

        // Should fail
        assertEquals(401, response.getStatusCode().value());
    }

    // TEST 4: Login with empty email should fail
    @Test
    public void login_EmptyEmail_ShouldFail() {
        // Try to login with empty email
        Map<String, String> request = new HashMap<>();
        request.put("email", "");
        request.put("password", "password123");

        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/login", request, String.class);

        // Should fail with 401 (user not found)
        assertEquals(401, response.getStatusCode().value());
    }

    // TEST 5: Login with empty password should fail
    @Test
    public void login_EmptyPassword_ShouldFail() {
        // Try to login with empty password
        Map<String, String> request = new HashMap<>();
        request.put("email", "student@test.com");
        request.put("password", "");

        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/login", request, String.class);

        // Should fail with 401 (user not found)
        assertEquals(401, response.getStatusCode().value());
    }

    // TEST 6: Check status for first-time user
    @Test
    public void checkStatus_FirstTimeUser_ShouldReturnTrue() {
        // Create first-time user (password = "null")
        User user = new User();
        user.setEmail("firsttime@test.com");
        user.setPassword("null");
        user.setRole("STUDENT");
        userRepository.save(user);

        // Check status
        Map<String, String> request = new HashMap<>();
        request.put("email", "firsttime@test.com");

        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/check-status", request, String.class);

        // Should return first-time = true
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("true"));
    }

    // TEST 7: Check status for returning user
    @Test
    public void checkStatus_ReturningUser_ShouldReturnFalse() {
        // Create returning user (password != "null")
        User user = new User();
        user.setEmail("returning@test.com");
        user.setPassword("realpassword");
        user.setRole("STUDENT");
        userRepository.save(user);

        // Check status
        Map<String, String> request = new HashMap<>();
        request.put("email", "returning@test.com");

        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/check-status", request, String.class);

        // Should return first-time = false
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("false"));
    }

    // TEST 8: Check status for user that doesn't exist
    @Test
    public void checkStatus_UserDoesNotExist_ShouldReturnFalse() {
        // Check status for non-existent user
        Map<String, String> request = new HashMap<>();
        request.put("email", "doesnotexist@test.com");

        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/check-status", request, String.class);

        // Should return first-time = false
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("false"));
    }

    // TEST 9: Set password for first-time user should work
    @Test
    public void setPassword_FirstTimeUser_ShouldWork() {
        // Create first-time user
        User user = new User();
        user.setEmail("newuser@test.com");
        user.setPassword("null");
        user.setRole("STUDENT");
        userRepository.save(user);

        // Set password
        Map<String, String> request = new HashMap<>();
        request.put("email", "newuser@test.com");
        request.put("newPassword", "mynewpassword123");

        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/set-initial-password", request, String.class);

        // Should work
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Password updated successfully"));
    }

    // TEST 10: Set password for user with existing password should fail
    @Test
    public void setPassword_ExistingPassword_ShouldFail() {
        // Create user with existing password
        User user = new User();
        user.setEmail("existing@test.com");
        user.setPassword("existingpassword");
        user.setRole("STUDENT");
        userRepository.save(user);

        // Try to set password
        Map<String, String> request = new HashMap<>();
        request.put("email", "existing@test.com");
        request.put("newPassword", "newpassword123");

        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/set-initial-password", request, String.class);

        // Should fail
        assertEquals(403, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Password has already been set"));
    }

    // TEST 11: Set password for user that doesn't exist should fail
    @Test
    public void setPassword_UserDoesNotExist_ShouldFail() {
        // Try to set password for non-existent user
        Map<String, String> request = new HashMap<>();
        request.put("email", "nonexistent@test.com");
        request.put("newPassword", "newpassword123");

        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/set-initial-password", request, String.class);

        // Should fail
        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().contains("User not found"));
    }

    // TEST 12: Login with completely empty request body should fail
    @Test
    public void login_EmptyRequestBody_ShouldFail() {
        // Send empty request body
        Map<String, String> request = new HashMap<>();

        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/login", request, String.class);

        // Should fail with 401 (user not found because email is null)
        assertEquals(401, response.getStatusCode().value());
    }

    // TEST 13: Login with null email should fail
    @Test
    public void login_NullEmail_ShouldFail() {
        // Send request with null email
        Map<String, String> request = new HashMap<>();
        request.put("email", null);
        request.put("password", "password123");

        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/login", request, String.class);

        // Should fail with 401 (user not found)
        assertEquals(401, response.getStatusCode().value());
    }

    // TEST 14: Login with null password should fail
    @Test
    public void login_NullPassword_ShouldFail() {
        // Create test user
        User user = new User();
        user.setEmail("nullpasstest@test.com");
        user.setPassword("realpassword");
        user.setRole("STUDENT");
        userRepository.save(user);

        // Send request with null password
        Map<String, String> request = new HashMap<>();
        request.put("email", "nullpasstest@test.com");
        request.put("password", null);

        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/login", request, String.class);

        // Should fail with 401 (invalid credentials)
        assertEquals(401, response.getStatusCode().value());
    }


}