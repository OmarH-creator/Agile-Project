package com.university.backend.entity.StaffRequests;

import com.university.backend.entity.User;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "staff_requests")
public class StaffRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;

    // --- STATIC COLUMNS ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private User requester;

    @Column(nullable = false)
    private String requestType; // e.g., "Leave", "Equipment", "Maintenance"

    @Column(nullable = false)
    private String status; // e.g., "Pending", "Approved", "Rejected"

    // --- DYNAMIC EAV MAPPING ---

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RequestAttributes> attributes = new ArrayList<>();

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RequestValue> values = new ArrayList<>();

    // --- CONSTRUCTORS ---

    public StaffRequest() {
        // Universal dynamic attributes for a generic request
        this.addAttribute("Description", "STRING");
        this.addAttribute("Date_Submitted", "DATE");
        this.addAttribute("Priority", "STRING"); // High, Medium, Low
    }

    public StaffRequest(User requester, String requestType) {
        this(); // Initialize attributes
        this.requester = requester;
        this.requestType = requestType;
        this.status = "Pending"; // Default status
    }

    // Helper to add attribute
    private void addAttribute(String name, String type) {
        RequestAttributes attr = new RequestAttributes(this, name, type);
        this.attributes.add(attr);
    }

    // --- GETTERS AND SETTERS ---

    public Long getId() {
        return requestId;
    }

    public void setId(Long id) {
        this.requestId = id;
    }

    public User getRequester() {
        return requester;
    }

    public void setRequester(User requester) {
        this.requester = requester;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<RequestAttributes> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<RequestAttributes> attributes) {
        this.attributes = attributes;
    }

    public List<RequestValue> getValues() {
        return values;
    }

    public void setValues(List<RequestValue> values) {
        this.values = values;
    }
}