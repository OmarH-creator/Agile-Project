// src/api/ProfessorApi.js

// 1. Define Constants
const API_BASE_URL = 'http://localhost:8081/api'; // Ensure this matches your Spring Boot port

const getAuthToken = () => {
    return localStorage.getItem('token');
};

// 2. Professor Functions

// Get Professor Profile & Courses
// Maps to: GET /api/professor/{professorId}
export async function getProfessorProfile(professorId) {
    const token = getAuthToken();
    const response = await fetch(`${API_BASE_URL}/professor/${professorId}`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        }
    });

    if (!response.ok) {
        const errorMessage = await response.text();
        throw new Error(errorMessage || 'Failed to fetch professor profile');
    }
    return await response.json();
}

// Get Students for a specific Course
// Maps to: GET /api/professor/courses/{courseId}/students
// (You will need to ensure this endpoint exists in your Controller)
export async function getCourseStudents(courseId) {
    const token = getAuthToken();
    const response = await fetch(`${API_BASE_URL}/professor/courses/${courseId}/students`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        }
    });

    if (!response.ok) {
        const errorMessage = await response.text();
        throw new Error(errorMessage || 'Failed to fetch student list');
    }
    return await response.json();
}

// Book a Hall
// Maps to: POST /api/professor/halls/book
export async function bookHall(bookingRequest) {
    const token = getAuthToken();
    const response = await fetch(`${API_BASE_URL}/professor/halls/book`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(bookingRequest)
    });

    if (!response.ok) {
        const errorMessage = await response.text();
        throw new Error(errorMessage || 'Failed to book hall');
    }
    return await response.text();
}