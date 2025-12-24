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