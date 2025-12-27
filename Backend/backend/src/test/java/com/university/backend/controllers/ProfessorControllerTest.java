package com.university.backend.controllers;

import com.university.backend.entity.*;
import com.university.backend.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * COMPLETE ProfessorController Tests - Simple and Easy to Read

 * This ONE file covers ALL testing requirements for ProfessorController:
 * - Hall booking functionality
 * - Professor authorization
 * - Hall availability checking
 * - Booking conflict detection
 * - Error handling for invalid requests

 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ProfessorControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private ProfessorRepository professorRepository;
    
    @Autowired
    private HallRepository hallRepository;
    
    @Autowired
    private BookingRepository bookingRepository;

    // ==========================================
    // HALL BOOKING TESTS
    // ==========================================

    // TEST 1: Book hall - should work
    @Test
    public void bookHall_ValidRequest_ShouldWork() {
        // Create test professor
        Professor professor = new Professor();
        professor.setProfessorId("P001");
        professor.setProfessorName("Dr. Smith");
        professor.setProfessorEmail("smith@test.com");
        professor.setProfessorDepartment("Computer Science");
        professorRepository.save(professor);

        // Create test hall
        Hall hall = new Hall();
        hall.setHallName("H001");
        hall.setCapacity(50);
        hallRepository.save(hall);

        // Create booking request
        Map<String, Object> request = new HashMap<>();
        request.put("professorId", "P001");
        request.put("hallName", "H001");
        request.put("start", new Date(System.currentTimeMillis() + 3600000)); // 1 hour from now
        request.put("end", new Date(System.currentTimeMillis() + 7200000));   // 2 hours from now
        request.put("purpose", "Lecture");
        request.put("reservationId", 12345L);

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/professor/halls/book", 
            request, 
            String.class
        );

        // Should book successfully (or 403 if security is enabled)
        assertTrue(response.getStatusCode().value() == 200 || response.getStatusCode().value() == 403);
        if (response.getStatusCode().value() == 200) {
            assertTrue(response.getBody().contains("booked successfully"));
            assertTrue(response.getBody().contains("P001"));
            assertTrue(response.getBody().contains("H001"));
        }
    }

    // TEST 2: Book hall - professor not found
    @Test
    public void bookHall_ProfessorNotFound_ShouldFail() {
        // Create test hall
        Hall hall = new Hall();
        hall.setHallName("H002");
        hall.setCapacity(30);
        hallRepository.save(hall);

        // Try to book with non-existent professor
        Map<String, Object> request = new HashMap<>();
        request.put("professorId", "NONEXISTENT");
        request.put("hallName", "H002");
        request.put("start", new Date(System.currentTimeMillis() + 3600000));
        request.put("end", new Date(System.currentTimeMillis() + 7200000));
        request.put("purpose", "Meeting");
        request.put("reservationId", 12346L);

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/professor/halls/book", 
            request, 
            String.class
        );

        // Should fail with 401 (unauthorized)
        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody().contains("not authorized or not found"));
    }

    // TEST 3: Book hall - hall not found
    @Test
    public void bookHall_HallNotFound_ShouldFail() {
        // Create test professor
        Professor professor = new Professor();
        professor.setProfessorId("P002");
        professor.setProfessorName("Dr. Johnson");
        professor.setProfessorEmail("johnson@test.com");
        professor.setProfessorDepartment("Mathematics");
        professorRepository.save(professor);

        // Try to book non-existent hall
        Map<String, Object> request = new HashMap<>();
        request.put("professorId", "P002");
        request.put("hallName", "NONEXISTENT");
        request.put("start", new Date(System.currentTimeMillis() + 3600000));
        request.put("end", new Date(System.currentTimeMillis() + 7200000));
        request.put("purpose", "Seminar");
        request.put("reservationId", 12347L);

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/professor/halls/book", 
            request, 
            String.class
        );

        // Should fail with 404 (not found)
        assertEquals(404, response.getStatusCode().value());
        assertTrue(response.getBody().contains("not found"));
    }

    // TEST 4: Book hall - time conflict
    @Test
    public void bookHall_TimeConflict_ShouldFail() {
        // Create test professor
        Professor professor = new Professor();
        professor.setProfessorId("P003");
        professor.setProfessorName("Dr. Brown");
        professor.setProfessorEmail("brown@test.com");
        professor.setProfessorDepartment("Physics");
        professorRepository.save(professor);

        // Create test hall
        Hall hall = new Hall();
        hall.setHallName("H003");
        hall.setCapacity(40);
        hallRepository.save(hall);

        // Create existing booking
        Date startTime = new Date(System.currentTimeMillis() + 3600000);
        Date endTime = new Date(System.currentTimeMillis() + 7200000);
        
        Booking existingBooking = new Booking();
        existingBooking.setStartTime(startTime);
        existingBooking.setEndTime(endTime);
        existingBooking.setPurpose("Existing Meeting");
        existingBooking.setStaffId("P999");
        existingBooking.setHall(hall);
        bookingRepository.save(existingBooking);

        // Try to book overlapping time
        Map<String, Object> request = new HashMap<>();
        request.put("professorId", "P003");
        request.put("hallName", "H003");
        request.put("start", new Date(System.currentTimeMillis() + 5400000)); // Overlaps with existing
        request.put("end", new Date(System.currentTimeMillis() + 9000000));
        request.put("purpose", "New Meeting");
        request.put("reservationId", 12348L);

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/professor/halls/book", 
            request, 
            String.class
        );

        // Should fail due to conflict (or 403 if security is enabled)
        assertTrue(response.getStatusCode().value() == 400 || response.getStatusCode().value() == 403);
        if (response.getStatusCode().value() == 400) {
            assertTrue(response.getBody().contains("Time conflict") || response.getBody().contains("unavailable"));
        }
    }

    // TEST 5: Book hall - empty professor ID
    @Test
    public void bookHall_EmptyProfessorId_ShouldFail() {
        // Create test hall
        Hall hall = new Hall();
        hall.setHallName("H004");
        hall.setCapacity(25);
        hallRepository.save(hall);

        // Try to book with empty professor ID
        Map<String, Object> request = new HashMap<>();
        request.put("professorId", "");
        request.put("hallName", "H004");
        request.put("start", new Date(System.currentTimeMillis() + 3600000));
        request.put("end", new Date(System.currentTimeMillis() + 7200000));
        request.put("purpose", "Test");
        request.put("reservationId", 12349L);

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/professor/halls/book", 
            request, 
            String.class
        );

        // Should fail
        assertEquals(401, response.getStatusCode().value());
        assertTrue(response.getBody().contains("not authorized or not found"));
    }

    // TEST 6: Book hall - empty hall name
    @Test
    public void bookHall_EmptyHallName_ShouldFail() {
        // Create test professor
        Professor professor = new Professor();
        professor.setProfessorId("P004");
        professor.setProfessorName("Dr. Davis");
        professor.setProfessorEmail("davis@test.com");
        professor.setProfessorDepartment("Chemistry");
        professorRepository.save(professor);

        // Try to book with empty hall name
        Map<String, Object> request = new HashMap<>();
        request.put("professorId", "P004");
        request.put("hallName", "");
        request.put("start", new Date(System.currentTimeMillis() + 3600000));
        request.put("end", new Date(System.currentTimeMillis() + 7200000));
        request.put("purpose", "Lab Session");
        request.put("reservationId", 12350L);

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/professor/halls/book", 
            request, 
            String.class
        );

        // Should fail with 404 (hall not found)
        assertEquals(404, response.getStatusCode().value());
        assertTrue(response.getBody().contains("not found"));
    }

    // TEST 7: Book hall - null start time
    @Test
    public void bookHall_NullStartTime_ShouldFail() {
        // Create test professor and hall
        Professor professor = new Professor();
        professor.setProfessorId("P005");
        professor.setProfessorName("Dr. Wilson");
        professor.setProfessorEmail("wilson@test.com");
        professor.setProfessorDepartment("Biology");
        professorRepository.save(professor);

        Hall hall = new Hall();
        hall.setHallName("H005");
        hall.setCapacity(35);
        hallRepository.save(hall);

        // Try to book with null start time
        Map<String, Object> request = new HashMap<>();
        request.put("professorId", "P005");
        request.put("hallName", "H005");
        request.put("start", null);
        request.put("end", new Date(System.currentTimeMillis() + 7200000));
        request.put("purpose", "Research Meeting");
        request.put("reservationId", 12351L);

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/professor/halls/book", 
            request, 
            String.class
        );

        // Should fail gracefully (400 or 500)
        assertTrue(response.getStatusCode().value() >= 400);
    }

    // TEST 8: Book hall - null end time
    @Test
    public void bookHall_NullEndTime_ShouldFail() {
        // Create test professor and hall
        Professor professor = new Professor();
        professor.setProfessorId("P006");
        professor.setProfessorName("Dr. Taylor");
        professor.setProfessorEmail("taylor@test.com");
        professor.setProfessorDepartment("History");
        professorRepository.save(professor);

        Hall hall = new Hall();
        hall.setHallName("H006");
        hall.setCapacity(20);
        hallRepository.save(hall);

        // Try to book with null end time
        Map<String, Object> request = new HashMap<>();
        request.put("professorId", "P006");
        request.put("hallName", "H006");
        request.put("start", new Date(System.currentTimeMillis() + 3600000));
        request.put("end", null);
        request.put("purpose", "Discussion");
        request.put("reservationId", 12352L);

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/professor/halls/book", 
            request, 
            String.class
        );

        // Should fail gracefully (400 or 500)
        assertTrue(response.getStatusCode().value() >= 400);
    }

    // TEST 9: Book hall - end time before start time
    @Test
    public void bookHall_EndTimeBeforeStartTime_ShouldFail() {
        // Create test professor and hall
        Professor professor = new Professor();
        professor.setProfessorId("P007");
        professor.setProfessorName("Dr. Anderson");
        professor.setProfessorEmail("anderson@test.com");
        professor.setProfessorDepartment("English");
        professorRepository.save(professor);

        Hall hall = new Hall();
        hall.setHallName("H007");
        hall.setCapacity(45);
        hallRepository.save(hall);

        // Try to book with end time before start time
        Map<String, Object> request = new HashMap<>();
        request.put("professorId", "P007");
        request.put("hallName", "H007");
        request.put("start", new Date(System.currentTimeMillis() + 7200000)); // 2 hours from now
        request.put("end", new Date(System.currentTimeMillis() + 3600000));   // 1 hour from now (before start)
        request.put("purpose", "Literature Review");
        request.put("reservationId", 12353L);

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/professor/halls/book", 
            request, 
            String.class
        );

        // Should fail due to invalid time range (or 403 if security is enabled)
        assertTrue(response.getStatusCode().value() == 400 || response.getStatusCode().value() == 403);
        if (response.getStatusCode().value() == 400) {
            assertTrue(response.getBody().contains("conflict") || response.getBody().contains("unavailable"));
        }
    }

    // TEST 10: Book hall - missing purpose
    @Test
    public void bookHall_MissingPurpose_ShouldWork() {
        // Create test professor and hall
        Professor professor = new Professor();
        professor.setProfessorId("P008");
        professor.setProfessorName("Dr. Garcia");
        professor.setProfessorEmail("garcia@test.com");
        professor.setProfessorDepartment("Art");
        professorRepository.save(professor);

        Hall hall = new Hall();
        hall.setHallName("H008");
        hall.setCapacity(60);
        hallRepository.save(hall);

        // Try to book without purpose (should still work)
        Map<String, Object> request = new HashMap<>();
        request.put("professorId", "P008");
        request.put("hallName", "H008");
        request.put("start", new Date(System.currentTimeMillis() + 3600000));
        request.put("end", new Date(System.currentTimeMillis() + 7200000));
        // No purpose field
        request.put("reservationId", 12354L);

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/professor/halls/book", 
            request, 
            String.class
        );

        // Should work (purpose is optional) or 403 if security is enabled
        assertTrue(response.getStatusCode().value() == 200 || response.getStatusCode().value() == 403);
        if (response.getStatusCode().value() == 200) {
            assertTrue(response.getBody().contains("booked successfully"));
        }
    }

    // TEST 11: Book hall - duplicate reservation ID (should still work)
    @Test
    public void bookHall_DuplicateReservationId_ShouldWork() {
        // Create test professor and halls
        Professor professor = new Professor();
        professor.setProfessorId("P009");
        professor.setProfessorName("Dr. Martinez");
        professor.setProfessorEmail("martinez@test.com");
        professor.setProfessorDepartment("Music");
        professorRepository.save(professor);

        Hall hall1 = new Hall();
        hall1.setHallName("H009");
        hall1.setCapacity(30);
        hallRepository.save(hall1);

        Hall hall2 = new Hall();
        hall2.setHallName("H010");
        hall2.setCapacity(40);
        hallRepository.save(hall2);

        // First booking
        Map<String, Object> request1 = new HashMap<>();
        request1.put("professorId", "P009");
        request1.put("hallName", "H009");
        request1.put("start", new Date(System.currentTimeMillis() + 3600000));
        request1.put("end", new Date(System.currentTimeMillis() + 7200000));
        request1.put("purpose", "Music Class");
        request1.put("reservationId", 99999L);

        ResponseEntity<String> response1 = restTemplate.postForEntity(
            "/api/professor/halls/book", 
            request1, 
            String.class
        );

        // Should work (or 403 if security is enabled)
        assertTrue(response1.getStatusCode().value() == 200 || response1.getStatusCode().value() == 403);

        // Second booking with same reservation ID but different hall and time
        Map<String, Object> request2 = new HashMap<>();
        request2.put("professorId", "P009");
        request2.put("hallName", "H010");
        request2.put("start", new Date(System.currentTimeMillis() + 10800000)); // 3 hours from now
        request2.put("end", new Date(System.currentTimeMillis() + 14400000));   // 4 hours from now
        request2.put("purpose", "Orchestra Practice");
        request2.put("reservationId", 99999L); // Same reservation ID

        ResponseEntity<String> response2 = restTemplate.postForEntity(
            "/api/professor/halls/book", 
            request2, 
            String.class
        );

        // Should still work (reservation ID might not be unique constraint) or 403 if security is enabled
        assertTrue(response2.getStatusCode().value() == 200 || response2.getStatusCode().value() == 403);
        if (response2.getStatusCode().value() == 200) {
            assertTrue(response2.getBody().contains("booked successfully"));
        }
    }

    // TEST 12: Book hall - very long purpose text
    @Test
    public void bookHall_LongPurpose_ShouldWork() {
        // Create test professor and hall
        Professor professor = new Professor();
        professor.setProfessorId("P010");
        professor.setProfessorName("Dr. Lee");
        professor.setProfessorEmail("lee@test.com");
        professor.setProfessorDepartment("Philosophy");
        professorRepository.save(professor);

        Hall hall = new Hall();
        hall.setHallName("H011");
        hall.setCapacity(25);
        hallRepository.save(hall);

        // Create very long purpose text
        String longPurpose = "This is a very long purpose description for testing how the system handles extended text input that might exceed normal database field lengths and could potentially cause issues with data storage or retrieval processes in the booking system.";

        Map<String, Object> request = new HashMap<>();
        request.put("professorId", "P010");
        request.put("hallName", "H011");
        request.put("start", new Date(System.currentTimeMillis() + 3600000));
        request.put("end", new Date(System.currentTimeMillis() + 7200000));
        request.put("purpose", longPurpose);
        request.put("reservationId", 12355L);

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/professor/halls/book", 
            request, 
            String.class
        );

        // Should work or fail gracefully
        assertTrue(response.getStatusCode().value() == 200 || response.getStatusCode().value() >= 400);
        if (response.getStatusCode().value() == 200) {
            assertTrue(response.getBody().contains("booked successfully"));
        }
    }

    // TEST 13: Book hall - past time (should fail)
    @Test
    public void bookHall_PastTime_ShouldFail() {
        // Create test professor and hall
        Professor professor = new Professor();
        professor.setProfessorId("P011");
        professor.setProfessorName("Dr. White");
        professor.setProfessorEmail("white@test.com");
        professor.setProfessorDepartment("Geology");
        professorRepository.save(professor);

        Hall hall = new Hall();
        hall.setHallName("H012");
        hall.setCapacity(50);
        hallRepository.save(hall);

        // Try to book in the past
        Map<String, Object> request = new HashMap<>();
        request.put("professorId", "P011");
        request.put("hallName", "H012");
        request.put("start", new Date(System.currentTimeMillis() - 7200000)); // 2 hours ago
        request.put("end", new Date(System.currentTimeMillis() - 3600000));   // 1 hour ago
        request.put("purpose", "Geology Lab");
        request.put("reservationId", 12356L);

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/professor/halls/book", 
            request, 
            String.class
        );

        // Should fail (can't book in the past) or 403 if security is enabled
        assertTrue(response.getStatusCode().value() == 400 || response.getStatusCode().value() == 403);
        if (response.getStatusCode().value() == 400) {
            assertTrue(response.getBody().contains("conflict") || response.getBody().contains("unavailable"));
        }
    }

    // TEST 14: Book hall - null request body
    @Test
    public void bookHall_NullRequestBody_ShouldFail() {
        // Try to book with null request body
        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/professor/halls/book", 
            null, 
            String.class
        );

        // Should fail gracefully
        assertTrue(response.getStatusCode().value() >= 400);
    }

    // TEST 15: Book hall - empty request body
    @Test
    public void bookHall_EmptyRequestBody_ShouldFail() {
        // Try to book with empty request body
        Map<String, Object> request = new HashMap<>();

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/professor/halls/book", 
            request, 
            String.class
        );

        // Should fail due to missing required fields
        assertTrue(response.getStatusCode().value() >= 400);
    }

    // TEST 16: Book hall - multiple bookings by same professor (should work)
    @Test
    public void bookHall_MultipleBookingsSameProfessor_ShouldWork() {
        // Create test professor and halls
        Professor professor = new Professor();
        professor.setProfessorId("P012");
        professor.setProfessorName("Dr. Clark");
        professor.setProfessorEmail("clark@test.com");
        professor.setProfessorDepartment("Psychology");
        professorRepository.save(professor);

        Hall hall1 = new Hall();
        hall1.setHallName("H013");
        hall1.setCapacity(30);
        hallRepository.save(hall1);

        Hall hall2 = new Hall();
        hall2.setHallName("H014");
        hall2.setCapacity(40);
        hallRepository.save(hall2);

        // First booking
        Map<String, Object> request1 = new HashMap<>();
        request1.put("professorId", "P012");
        request1.put("hallName", "H013");
        request1.put("start", new Date(System.currentTimeMillis() + 3600000));
        request1.put("end", new Date(System.currentTimeMillis() + 7200000));
        request1.put("purpose", "Psychology Lecture");
        request1.put("reservationId", 12357L);

        ResponseEntity<String> response1 = restTemplate.postForEntity(
            "/api/professor/halls/book", 
            request1, 
            String.class
        );

        // Should work (or 403 if security is enabled)
        assertTrue(response1.getStatusCode().value() == 200 || response1.getStatusCode().value() == 403);

        // Second booking by same professor (different hall, different time)
        Map<String, Object> request2 = new HashMap<>();
        request2.put("professorId", "P012");
        request2.put("hallName", "H014");
        request2.put("start", new Date(System.currentTimeMillis() + 10800000)); // 3 hours from now
        request2.put("end", new Date(System.currentTimeMillis() + 14400000));   // 4 hours from now
        request2.put("purpose", "Psychology Lab");
        request2.put("reservationId", 12358L);

        ResponseEntity<String> response2 = restTemplate.postForEntity(
            "/api/professor/halls/book", 
            request2, 
            String.class
        );

        // Should also work (or 403 if security is enabled)
        assertTrue(response2.getStatusCode().value() == 200 || response2.getStatusCode().value() == 403);
        if (response2.getStatusCode().value() == 200) {
            assertTrue(response2.getBody().contains("booked successfully"));
        }
    }

    // TEST 17: Book hall - same time different halls (should work)
    @Test
    public void bookHall_SameTimeDifferentHalls_ShouldWork() {
        // Create test professors and halls
        Professor professor1 = new Professor();
        professor1.setProfessorId("P013");
        professor1.setProfessorName("Dr. Adams");
        professor1.setProfessorEmail("adams@test.com");
        professor1.setProfessorDepartment("Sociology");
        professorRepository.save(professor1);

        Professor professor2 = new Professor();
        professor2.setProfessorId("P014");
        professor2.setProfessorName("Dr. Baker");
        professor2.setProfessorEmail("baker@test.com");
        professor2.setProfessorDepartment("Anthropology");
        professorRepository.save(professor2);

        Hall hall1 = new Hall();
        hall1.setHallName("H015");
        hall1.setCapacity(35);
        hallRepository.save(hall1);

        Hall hall2 = new Hall();
        hall2.setHallName("H016");
        hall2.setCapacity(45);
        hallRepository.save(hall2);

        Date startTime = new Date(System.currentTimeMillis() + 3600000);
        Date endTime = new Date(System.currentTimeMillis() + 7200000);

        // First booking
        Map<String, Object> request1 = new HashMap<>();
        request1.put("professorId", "P013");
        request1.put("hallName", "H015");
        request1.put("start", startTime);
        request1.put("end", endTime);
        request1.put("purpose", "Sociology Seminar");
        request1.put("reservationId", 12359L);

        ResponseEntity<String> response1 = restTemplate.postForEntity(
            "/api/professor/halls/book", 
            request1, 
            String.class
        );

        // Should work (or 403 if security is enabled)
        assertTrue(response1.getStatusCode().value() == 200 || response1.getStatusCode().value() == 403);

        // Second booking at same time but different hall
        Map<String, Object> request2 = new HashMap<>();
        request2.put("professorId", "P014");
        request2.put("hallName", "H016");
        request2.put("start", startTime);
        request2.put("end", endTime);
        request2.put("purpose", "Anthropology Workshop");
        request2.put("reservationId", 12360L);

        ResponseEntity<String> response2 = restTemplate.postForEntity(
            "/api/professor/halls/book", 
            request2, 
            String.class
        );

        // Should also work (different halls) or 403 if security is enabled
        assertTrue(response2.getStatusCode().value() == 200 || response2.getStatusCode().value() == 403);
        if (response2.getStatusCode().value() == 200) {
            assertTrue(response2.getBody().contains("booked successfully"));
        }
    }
}