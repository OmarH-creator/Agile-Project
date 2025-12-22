package com.university.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "submission_meta_keys")
public class SubmissionAttributes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String keyName;

    // Default Constructor
    public SubmissionAttributes() {}

    // Parameterized Constructor
    public SubmissionAttributes(String keyName) {
        this.keyName = keyName;
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }
}