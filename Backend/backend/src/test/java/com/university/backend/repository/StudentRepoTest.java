package com.university.backend.repository;

import com.university.backend.entity.Student;
import com.university.backend.entity.Major;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;

@AutoConfigureTestDatabase(replace = Replace.NONE)
@SpringBootTest
public class StudentRepoTest {

    @Autowired
    private StudentRepository studentRepository;

    private Major sampleMajor() {
        Major major = new Major();
        major.setMajorName("Engineering");
        major.setMajorId("ENG");
        return major;
    }

    @Test
    void should_store_and_find_student() {
        // Given
        Major major = sampleMajor();
        Student student = new Student(
                "S001", // studentId
                "Ali Ahmed", // name
                "ali@university.edu", // email
                major, // major
                "01012345678", // phone
                "123 Main St", // address
                new Date(), // dateOfBirth
                "Exempt" // militaryStatus
        );

        // When
        studentRepository.save(student);
        Student found = studentRepository.findById(student.getId()).orElse(null);

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Ali Ahmed");
        assertThat(found.getMajor().getMajorName()).isEqualTo("Engineering");
    }

    @Test
    void should_return_true_if_student_exists_by_id() {
        Major major = sampleMajor();
        Student student = new Student(
                "S002", "Sara", "sara@u.edu", major, "01098765432",
                "456 Elm St", new Date(), "Required"
        );
        studentRepository.save(student);
        assertThat(studentRepository.existsById(student.getId())).isTrue();
    }

    @Test
    void should_delete_student_by_id() {
        Major major = sampleMajor();
        Student student = new Student(
                "S003", "Hossam", "hossam@u.edu", major, "01234567890",
                "789 Oak St", new Date(), "Unknown"
        );
        studentRepository.save(student);
        Long id = student.getId();
        studentRepository.deleteById(id);
        assertThat(studentRepository.existsById(id)).isFalse();
    }

}
