package com.university.backend.dto;

import java.util.HashMap;
import java.util.Map;

public class AssignmentSubmissionResponseDTO {
    private Long id;
    // Map stores "Student_Id": "123", "Grade": 95.0, "Content": "http...", etc.
    private Map<String, Object> data = new HashMap<>();

    public AssignmentSubmissionResponseDTO(Long id) {
        this.id = id;
    }

    public void addField(String key, Object value) {
        this.data.put(key, value);
    }

    // Getters for JSON serialization
    public Long getId() { return id; }
    public Map<String, Object> getData() { return data; }
}