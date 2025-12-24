// src/api/CoursesApi.js

const API_BASE_URL = 'http://localhost:8081/api/admin';

const getAuthToken = () => {
    return localStorage.getItem('token');
};

const getHeaders = () => ({
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${getAuthToken()}`
});

export async function getAllCourses() {
    const response = await fetch(`${API_BASE_URL}/courses`, {
        method: 'GET',
        headers: getHeaders()
    });

    if (!response.ok) {
        throw new Error('Failed to fetch courses');
    }
    return await response.json();
}

export async function updateCourse(courseCode, courseData) {
    const response = await fetch(`${API_BASE_URL}/courses/${courseCode}`, {
        method: 'PUT',
        headers: getHeaders(),
        body: JSON.stringify(courseData)
    });

    if (!response.ok) {
        throw new Error('Failed to update course');
    }
    return await response.text();
}

export async function deleteCourse(courseCode) {
    const response = await fetch(`${API_BASE_URL}/courses/${courseCode}`, {
        method: 'DELETE',
        headers: getHeaders()
    });

    if (!response.ok) {
        throw new Error('Failed to delete course');
    }
    return await response.text();
}

export async function addCourse(courseData) {
    const response = await fetch(`${API_BASE_URL}/courses`, {
        method: 'POST',
        headers: getHeaders(),
        body: JSON.stringify(courseData)
    });

    if (!response.ok) {
        throw new Error('Failed to add course');
    }
    return await response.text();
}
export async function updatePrerequisites(courseCode, prerequisites) {
    const response = await fetch(`${API_BASE_URL}/courses/${courseCode}/prerequisites`, {
        method: 'PUT',
        headers: getHeaders(),
        body: JSON.stringify(prerequisites)
    });

    if (!response.ok) {
        throw new Error('Failed to update prerequisites');
    }
    return await response.text();
}
