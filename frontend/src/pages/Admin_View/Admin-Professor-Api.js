// Admin-Professor-Api.js

// 1. Define the missing constants
const API_BASE_URL = 'http://localhost:8081/api/admin';

const getAuthToken = () => {
    return localStorage.getItem('token');
};

// 2. Your Professor Functions
export async function updateProfessor(professorId, professorData) {
    const token = getAuthToken();
    const response = await fetch(`${API_BASE_URL}/professors/${professorId}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(professorData)
    });

    if (!response.ok) {
        const errorMessage = await response.text();
        throw new Error(errorMessage || 'Failed to update professor');
    }
    return await response.text();
}

export async function assignCourseToProfessor(professorId, courseName) {
    const token = getAuthToken();
    const url = `${API_BASE_URL}/professors/${professorId}/assign-course?courseName=${encodeURIComponent(courseName)}`;

    const response = await fetch(url, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        }
    });

    if (!response.ok) {
        const errorMessage = await response.text();
        throw new Error(errorMessage || 'Failed to assign course');
    }
    return await response.text();
}

export async function deleteProfessor(professorId) {
    const token = getAuthToken();
    const response = await fetch(`${API_BASE_URL}/professors/${professorId}`, {
        method: 'DELETE',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    });

    if (!response.ok) {
        const errorMessage = await response.text();
        throw new Error(errorMessage || 'Failed to delete professor');
    }
    return await response.text();
}

// [NEW] Get All Professors
export async function getAllProfessors() {
    const token = getAuthToken();
    const response = await fetch(`${API_BASE_URL}/professors`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    });

    if (!response.ok) {
        throw new Error('Failed to fetch professors');
    }
    return await response.json();
}

// [NEW] Create Professor
export async function createProfessor(professorData) {
    const token = getAuthToken();
    const response = await fetch(`${API_BASE_URL}/professors`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(professorData)
    });

    if (!response.ok) {
        const msg = await response.text();
        throw new Error(msg || 'Failed to create professor');
    }
    return await response.text();
}

// [NEW] Get All Courses for Dropdown
export async function getAllCourses() {
    const token = getAuthToken();
    const response = await fetch(`${API_BASE_URL}/courses`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    });

    if (!response.ok) {
        throw new Error('Failed to fetch courses');
    }
    return await response.json();
}