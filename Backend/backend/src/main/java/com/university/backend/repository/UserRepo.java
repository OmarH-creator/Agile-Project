package com.university.backend.repository;

import com.university.backend.entity.User;
import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class UserRepo {
    private final List<User> users = new ArrayList<>();

    public UserRepo() {
        // Add some sample users for testing
        users.add(new User(1L, "user@example.com", "password123","student"));
        users.add(new User(2L, "admin@example.com", "adminpass","admin"));
    }

    public User findByEmail(String email) {
        return users.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);
    }
}
