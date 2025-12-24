import axios from "axios";

// Fetch children for a parent by email
export async function getChildrenByEmail(email) {
    try {
        const encodedEmail = encodeURIComponent(email);
        // Use /api/parents prefix and port 8081
        const res = await axios.get(`/api/parents/by-email/${encodedEmail}/children`);
        console.log("API Response:", res.data);
        return res.data;
    } catch (err) {
        console.error("API Error Details:", {
            status: err.response?.status,
            data: err.response?.data,
            message: err.message
        });
        throw err;
    }
}

export async function getChildrenCourseRecords(email) {
    try {
        const encodedEmail = encodeURIComponent(email);
        const res = await axios.get(`/api/parents/by-email/${encodedEmail}/children/course-records`);
        return res.data;
    } catch (err) {
        console.error("API Error:", err);
        throw err;
    }
}

// NEW: Fetch course records for a specific child
export async function getChildCourseRecords(email, studentId) {
    try {
        const encodedEmail = encodeURIComponent(email);
        const encodedStudentId = encodeURIComponent(studentId);
        const res = await axios.get(`/api/parents/by-email/${encodedEmail}/children/${encodedStudentId}/course-records`);
        return res.data;
    } catch (err) {
        console.error("API Error:", err);
        throw err;
    }
}