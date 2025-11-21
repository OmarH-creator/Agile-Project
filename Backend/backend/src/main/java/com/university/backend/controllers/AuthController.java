package com.university.backend.controllers;

import com.university.backend.dto.LoginRequest;
import com.university.backend.entity.User;
import com.university.backend.repository.UserRepo;
import com.university.backend.utils.JwtUtil;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepo userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail());
        if (user != null && user.getPassword().equals(req.getPassword())) {
            Map<String, Object> claims = new HashMap<>();
                claims.put("userId", user.getId());
                claims.put("role", user.getRole()); // add role or other claims as needed
                String token = JwtUtil.generateToken(user.getEmail(), claims);

               // Include role in response as well
                return ResponseEntity.ok().body(
                    Map.of(
                        "token", token,
                        "role", claims.get("role")
                    )
                );
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials!");
    }
}
