package com.university.backend.dto;

public class LoginRequest {
    private String email;
    private String password;

    public LoginRequest() {
        // default constructor
    }

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Getter and setter for email
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    // Getter and setter for password
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}
