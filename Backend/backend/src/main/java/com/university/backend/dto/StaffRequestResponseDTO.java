package com.university.backend.dto;

import java.util.HashMap;
import java.util.Map;

public class StaffRequestResponseDTO {
    private Long id;
    // Map stores "RequestType": "Leave", "Status": "Pending", "Description": "Sick Leave", etc.
    private Map<String, Object> data = new HashMap<>();

    public StaffRequestResponseDTO(Long id) {
        this.id = id;
    }

    public void addField(String key, Object value) {
        this.data.put(key, value);
    }

    // Getters for JSON serialization
    public Long getId() { return id; }
    public Map<String, Object> getData() { return data; }
}