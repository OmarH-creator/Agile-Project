package com.university.backend.controllers;

import com.university.backend.dto.LoginRequest;
import com.university.backend.entity.User;
import com.university.backend.entity.Professor;
import com.university.backend.entity.Student;
import com.university.backend.repository.ProfessorRepository;
import com.university.backend.repository.StudentRepository;
import com.university.backend.repository.UserRepository;
import com.university.backend.utils.JwtUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private StudentRepository studentRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        //1. find user in the user table ya 7elw
        Optional<User> userOpt = userRepository.findByEmail(req.getEmail());

        if (userOpt.isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("user not present in database!");
        }
        User user = userOpt.get();
        //2. validate password (efta7 ya semsem)
        if (user.getPassword().equals(req.getPassword())) {
            // 3. Resolve the Business ID based on Role
            String businessId = null;
            String role = user.getRole().toUpperCase(); // Ensure case consistency
            if ("PROFESSOR".equals(role)) {
                Optional<Professor> prof = professorRepository.findByProfessorEmail(user.getEmail());
                if (prof.isPresent()) {
                    businessId = prof.get().getProfessorId(); // e.g., "P-101"
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Professor profile not linked to this account.");
                }
            }
            else if ("STUDENT".equals(role)) {
                Optional<Student> student = studentRepository.findByEmail(user.getEmail());
                if (student.isPresent()) {
                    businessId = student.get().getStudentId(); // e.g., "S-450"
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Student profile not linked to this account.");
                }
            }
            else if ("ADMIN".equals(role)) {
                businessId = "ADMIN";
            }

            // 4. Generate Token
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", user.getId());
            claims.put("role", role);
            claims.put("businessId", businessId); // Optional: Add ID inside token claims too

            String token = JwtUtil.generateToken(user.getEmail(), claims);

            // 5. Return Response with Token, Role, AND Business ID
            // We use a HashMap instead of Map.of() to ensure we can handle null values if necessary
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("role", role);
            response.put("businessId", businessId);

            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials!");
    }

    // --- NEW: CHECK IF PASSWORD IS "null" ---
    @PostMapping("/check-status")
    public ResponseEntity<?> checkStatus(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            // Check if the password is strictly the string "null"
            boolean isFirstTime = "null".equals(userOpt.get().getPassword());
            return ResponseEntity.ok(Map.of("isFirstTime", isFirstTime));
        }
        // If user not found, strictly return false so we don't leak info or break UI
        return ResponseEntity.ok(Map.of("isFirstTime", false));
    }

    // --- NEW: UPDATE INITIAL PASSWORD ---
    @PostMapping("/set-initial-password")
    public ResponseEntity<?> setInitialPassword(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String newPassword = payload.get("newPassword");

        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found.");
        }

        User user = userOpt.get();

        // Security Check: Only allow this if the current password is actually "null"
        if (!"null".equals(user.getPassword())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Password has already been set. Use 'Forgot Password'.");
        }

        // Update the password
        user.setPassword(newPassword);
        userRepository.save(user);

        return ResponseEntity.ok("Password updated successfully. Please login.");
    }

}
