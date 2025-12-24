package com.university.backend.controllers;

import com.university.backend.entity.Parent;
import com.university.backend.entity.Student;
import com.university.backend.repository.StudentRepository;
import org.springframework.web.bind.annotation.*;
import com.university.backend.repository.ParentRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/parents")  // ← MUST have /api prefix
public class ParentController {

    @Autowired
    private ParentRepository parentRepository;

    @Autowired
    private StudentRepository studentRepository;

    @GetMapping("/test")
    public String test() {
        System.out.println("Parent test endpoint hit!");
        return "Parent Controller is working!";
    }

    @GetMapping("/by-email/{email}")
    public Parent getParentByEmail(@PathVariable String email) {
        System.out.println("Getting parent for email: " + email);
        return parentRepository.findByParentEmail(email)
                .orElseThrow(() -> new RuntimeException("Parent not found"));
    }

    @GetMapping("/by-email/{email}/children")
    public List<Student> getChildren(@PathVariable String email) {
        System.out.println("Getting children for parent email: " + email);

        Parent p = parentRepository.findByParentEmail(email)
                .orElseThrow(() -> new RuntimeException("Parent not found"));

        System.out.println("Found parent: " + p.getParentName());
        System.out.println("Student IDs: " + p.getStudentIds());

        return studentRepository.findByStudentIdIn(p.getStudentIds());
    }
}