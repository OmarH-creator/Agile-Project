package com.university.backend.repository;

import university.entity.Hall;
import university.entity.Student;

import java.util.ArrayList;
import java.util.List;

public class UniversityRepository {


    public static List<Student> students = new ArrayList<>();
    public static List<Hall> halls = new ArrayList<>();

    private UniversityRepository() {
    }
}
