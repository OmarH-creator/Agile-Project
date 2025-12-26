import axios from 'axios';

// Base URL configuration - adjust port if needed (default Spring Boot is 8080)
const API_BASE_URL = 'http://localhost:8081/api/professor';

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// 2. ADD THIS INTERCEPTOR
// This code runs before every request to grab the token from storage
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

export const ProfessorAPI = {
    // 1. Get courses for a specific professor
    getCourses: async (professorId) => {
        try {
            const response = await api.get(`/${professorId}/courses`);
            return response.data; // Returns Array<String> (Course Names)
        } catch (error) {
            console.error("Error fetching courses:", error);
            throw error;
        }
    },

    // 2. Get students enrolled in a specific course
    getStudentsInCourse: async (courseName) => {
        try {
            const response = await api.get(`/course/${courseName}/students`);
            return response.data; // Returns Array<Student>
        } catch (error) {
            console.error("Error fetching students:", error);
            throw error;
        }
    },

    // 3. Assign a final grade (Moves course to history)
    assignFinalGrade: async (gradeData) => {
        // gradeData = { studentId, courseName, grade, semester }
        try {
            const response = await api.post('/course/grade', gradeData);
            return response.data;
        } catch (error) {
            console.error("Error assigning grade:", error);
            throw error;
        }
    },

    // 4. Create an Assignment (Modified for File Upload)
    createAssignment: async (assignmentData) => {
        // assignmentData = { title, description, courseName, professorId, deadline, file }
        try {
            const formData = new FormData();

            // Extract Course Code if needed, or pass full string if that's what backend expects (Backend expects Course_Id which is Code)
            const courseCode = assignmentData.courseName.split(' - ')[0];

            const payload = {
                Title: assignmentData.title,
                Course_Id: courseCode,
                Professor_Id: assignmentData.professorId,
                Description: assignmentData.description,
                Due_Date: assignmentData.deadline,
                Max_Grade: assignmentData.maxGrade || 100,
                Is_Visible: true
            };

            // Add Custom Attributes (Dynamic Fields)
            Object.keys(assignmentData).forEach(key => {
                // exclude known keys that we mapped manually above
                const knownKeys = ['title', 'coursename', 'professorid', 'description', 'deadline', 'maxgrade', 'file'];
                if (!knownKeys.includes(key)) {
                    payload[key] = assignmentData[key];
                }
            });

            formData.append('payload', JSON.stringify(payload));

            if (assignmentData.file) {
                formData.append('file', assignmentData.file);
            }

            // Note: Content-Type 'multipart/form-data' is set automatically by browser when using FormData
            // We do NOT need to set it manually. In fact, setting it manually breaks the boundary.
            // We must nullify the Content-Type header to allow the browser to set it with the boundary
            const response = await api.post('/create', formData, {
                headers: {
                    'Content-Type': undefined
                }
            });
            return response.data;
        } catch (error) {
            console.error("Error creating assignment:", error);
            throw error;
        }
    },

    // 4.1 Update Assignment
    updateAssignment: async (id, assignmentData) => {
        try {
            const payload = {
                Title: assignmentData.title,
                Description: assignmentData.description,
                Max_Grade: assignmentData.maxGrade,
                Due_Date: assignmentData.deadline
            };

            // Add Custom Attributes (Dynamic Fields)
            Object.keys(assignmentData).forEach(key => {
                const knownKeys = ['title', 'description', 'maxgrade', 'deadline'];
                if (!knownKeys.includes(key)) {
                    payload[key] = assignmentData[key];
                }
            });
            const response = await api.post(`/update/${id}`, payload);
            return response.data;
        } catch (error) {
            console.error("Error updating assignment:", error);
            throw error;
        }
    },

    // 5. Get Assignments for a course
    getAssignments: async (courseName) => {
        try {
            // Extract Course Code from "CSE112 - Computer Org" -> "CSE112"
            const courseCode = courseName.split(' - ')[0];
            const response = await api.get(`/assignment/${courseCode}`);
            return response.data;
        } catch (error) {
            console.error("Error fetching assignments:", error);
            throw error;
        }
    },

    // 6. Get Submissions for an Assignment (NEW)
    getSubmissions: async (assignmentId) => {
        try {
            const response = await api.get(`/assignment/${assignmentId}/submissions`);
            return response.data;
        } catch (error) {
            console.error("Error fetching submissions:", error);
            throw error;
        }
    },

    // 7. Grade an Assignment
    gradeAssignment: async (gradingData) => {
        // gradingData = { assignmentId, studentId, score, feedback }
        try {
            const response = await api.post('/assignment/grade', gradingData);
            return response.data;
        } catch (error) {
            console.error("Error grading assignment:", error);
            throw error;
        }
    },

    // 8. Get Professor Requests
    getRequests: async (professorId) => {
        try {
            const response = await api.get(`/${professorId}/requests`);
            return response.data;
        } catch (error) {
            console.error("Error fetching requests:", error);
            throw error;
        }
    },

    // 9. Create Professor Request
    createRequest: async (requestData) => {
        try {
            const response = await api.post('/request/create', requestData);
            return response.data;
        } catch (error) {
            console.error("Error creating request:", error);
            throw error;
        }
    },

    // 10. Book a Hall
    bookHall: async (bookingData) => {
        try {
            const response = await api.post('/halls/book', bookingData);
            return response.data;
        } catch (error) {
            console.error("Error booking hall:", error);
            throw error;
        }
    },

    // 11. Get All Halls
    getAllHalls: async () => {
        try {
            const response = await api.get('/halls');
            return response.data;
        } catch (error) {
            console.error("Error fetching halls:", error);
            throw error;
        }
    },

    // 12. Create Grading Item (Bucket)
    createGradingItem: async (data) => {
        // data = { courseId, categoryName, weight }
        try {
            // Override baseURL to point to /api/grading
            const response = await api.post('http://localhost:8081/api/grading/item/create', data);
            return response.data;
        } catch (error) {
            console.error("Error creating grading item:", error);
            throw error;
        }
    },

    // 13. Get Grading Items for Course
    getGradingItemsByCourse: async (courseName) => {
        try {
            const courseCode = courseName.split(' - ')[0];
            // Override baseURL to point to /api/grading
            const response = await api.get(`http://localhost:8081/api/grading/item/course/${courseCode}`);
            return response.data;
        } catch (error) {
            console.error("Error fetching grading items:", error);
            throw error;
        }
    }
};

// Simple Icon export to match your Admin setup
export const Icon = {
    course: <svg width="24" height="24" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" /></svg>,
    user16: <svg width="16" height="16" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" /></svg>,
    menu16: <svg width="16" height="16" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" /></svg>,
    home16: <svg width="20" height="20" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" /></svg>,
    back: <svg width="20" height="20" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" /></svg>
};