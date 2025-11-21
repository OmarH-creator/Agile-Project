package com.university.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.university.backend.entity.Student;
import com.university.backend.entity.Hall;
import com.university.backend.entity.Professor;
import com.university.backend.entity.Admin;
import com.university.backend.entity.Course;
//import org.springframework.data.jpa.repository.JpaRepository; // (Keep this if it exists)
//package com.university.backend.repository;


import java.util.ArrayList;
import java.util.List;

public class UniversityRepository {


    public static List<Student> students = new ArrayList<>();
    public static List<Hall> halls = new ArrayList<>();
    public static List<Professor> professors = new ArrayList<>();
    public static List<Course> courses = new ArrayList<>();
    private UniversityRepository() {
    }
}
