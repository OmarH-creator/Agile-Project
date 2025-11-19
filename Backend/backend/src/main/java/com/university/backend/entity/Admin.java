package com.university.backend.entity;

import university.data.UniversityRepository;
import university.entity.Professor;

public class Admin {

    private String adminId;
    private String name;
    private String email;

    public Admin(String adminId, String name, String email) {
        this.adminId = adminId;
        this.name = name;
        this.email = email;
    }

    public void createStudentRecord(String id, String name, String email, String Department) {
        //check if student already exists
        if (getStudent(id) != null) {
            System.out.println("Student already exists.");
            return;
        }
        Student newStudent = new Student(id, name, email, Department);
        UniversityRepository.students.add(newStudent);
    }

    public void createProfessorRecord(String id, String name, String email, String Department) {
        //check if student already exists
        if (getProfessorID(id) != null) {
            System.out.println("Professor already exists.");
            return;
        }
        Professor newProfessor = new Professor(id, name, email, Department);
        UniversityRepository.professors.add(newProfessor);
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

    public Professor getProfessor(String profId){
        return UniversityRepository.professors.stream()
                .filter(s -> s.getProfessorId().equals(profId))
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

    public String assignCourseToProfessor(String professorId, String courseName) {
        // 1. Find the professor in the repository
        Professor targetProf = UniversityRepository.professors.stream()
                .filter(p -> p.getProfessorId().equals(professorId))
                .findFirst()
                .orElse(null);

        // 2. Validate results
        if (targetProf == null) {
            return "Error: Professor with ID " + professorId + " not found.";
        }

        // 3. Assign the course
        targetProf.assignCourse(courseName);
        return "Success: Course '" + courseName + "' assigned to " + targetProf.getProfessorName();
    }

}
