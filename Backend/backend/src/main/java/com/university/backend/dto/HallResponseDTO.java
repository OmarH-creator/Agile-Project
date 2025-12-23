package com.university.backend.dto;

import java.util.HashMap;
import java.util.Map;

public class HallResponseDTO {
    private Long id;
    // Map stores "Name": "Lecture A", "Capacity": 100, etc.
    private Map<String, Object> data = new HashMap<>();

    public HallResponseDTO(Long id) {
        this.id = id;
    }

    public void addField(String key, Object value) {
        this.data.put(key, value);
    }

    // Getters for JSON serialization
    public Long getId() { return id; }
    public Map<String, Object> getData() { return data; }
}