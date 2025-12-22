package com.university.backend.dto;

import java.util.HashMap;
import java.util.Map;

public class AssignmentResponseDTO {
    private Long id;
    // Map stores "Title": "Math", "Deadline": "2023..."
    private Map<String, Object> data = new HashMap<>();

    public AssignmentResponseDTO(Long id) {
        this.id = id;
    }

    public void addField(String key, Object value) {
        this.data.put(key, value);
    }

    // Getters for JSON serialization
    public Long getId() { return id; }
    public Map<String, Object> getData() { return data; }
}