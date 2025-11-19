package com.university.backend.entity;

import university.data.UniversityRepository;

public class Admin {

    private String adminId;
    private String name;
    private String email;

    public Admin(String adminId, String name, String email) {
        this.adminId = adminId;
        this.name = name;
        this.email = email;
    }


    public void addStudent(Student student) {
        UniversityRepository.students.add(student);
    }

    public Student getStudent(String studentId) {
        return UniversityRepository.students.stream()
                .filter(s -> s.getStudentId().equals(studentId))
                .findFirst()
                .orElse(null);
    }

    // Hall scheduele
    public void addHall(Hall hall) {
        UniversityRepository.halls.add(hall);
    }

    public boolean bookHall(String hallName, LocalDateTime start, LocalDateTime end, String purpose) {
        for (Hall hall : UniversityRepository.halls) {
            if (hall.getHallName().equals(hallName)) {
                return hall.book(start, end, purpose);
            }
        }
        return false;
    }

    // Transcript
    public String generateTranscript(String studentId) {
        Student s = getStudent(studentId);

        if (s == null) return "Student not found.";

        StringBuilder sb = new StringBuilder();
        sb.append("===== Transcript for ").append(s.getName()).append(" =====\n");

        for (Student.CourseRecord cr : s.getCompletedCourses()) {
            sb.append(cr.courseName)
                    .append(" | Grade: ").append(cr.grade)
                    .append(" | Credits: ").append(cr.credits)
                    .append("\n");
        }

        sb.append("GPA: ").append(s.getGPA()).append("\n");
        sb.append("======================================");
        return sb.toString();
    }
}
