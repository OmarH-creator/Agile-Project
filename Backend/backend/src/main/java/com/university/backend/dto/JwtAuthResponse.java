package com.university.backend.dto;

public class JwtAuthResponse {
    private String token;
    private String role;
    private String businessId; // e.g., "P-101", "S-405", or "ADMIN"

    public JwtAuthResponse(String token, String role, String businessId) {
        this.token = token;
        this.role = role;
        this.businessId = businessId;
    }

    // Getters and Setters
    public String getToken() { return token; }
    public String getRole() { return role; }
    public String getBusinessId() { return businessId; }
}