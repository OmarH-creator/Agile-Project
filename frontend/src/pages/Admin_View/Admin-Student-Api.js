// API Configuration

const API_BASE_URL = 'http://localhost:8081/api/admin';

// Helper function to get authentication token from localStorage
const getAuthToken = () => {
    return localStorage.getItem('token');
};
///////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////// API-FUNCTIONS-FOR-ADMIN-STUDENT-RELATED-SERVICES //////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
// fetch admin name
export async function getAdmin(email) {
    const token = await getAuthToken();
    const response = await fetch(`${API_BASE_URL}/${email}`, {
        method: "GET",
        headers: {
            "Content-Type": "application/json",
            'Authorization': `Bearer ${token}`,
        }
    });
    if (!response.ok) {
        const errorMessage = await response.text();
        throw new Error(errorMessage || `Failed to create student: ${response.status}`);
    }
    return await response.text();
}
// Create a new student record
export async function createStudent(studentData) {
    const token = getAuthToken();
    // const backendData = toBackendFormat(studentData);

    const response = await fetch(`${API_BASE_URL}/students`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(studentData)
    });

    if (!response.ok) {
        const errorMessage = await response.text();
        throw new Error(errorMessage || `Failed to create student: ${response.status}`);
    }

    return await response.text();
}

// [NEW] Update an existing student record
export async function updateStudent(studentId, studentData) {
    const token = getAuthToken();

    const response = await fetch(`${API_BASE_URL}/students/${studentId}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(studentData)
    });

    if (!response.ok) {
        if (response.status === 404) {
            throw new Error(`Student with ID ${studentId} not found.`);
        }
        const errorMessage = await response.text();
        throw new Error(errorMessage || `Failed to update student: ${response.status}`);
    }

    return await response.text();
}

// Get a specific student by studentId
export async function getStudent(studentId) {
    const token = getAuthToken();

    const response = await fetch(`${API_BASE_URL}/students/${studentId}`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        }
    });

    if (!response.ok) {
        if (response.status === 404) {
            throw new Error('Student not found.');
        }
        const errorMessage = await response.text();
        throw new Error(errorMessage || `Failed to retrieve student: ${response.status}`);
    }

    // RETURN RAW JSON. No adapter needed.
    return await response.json();
}

export const getAllStudents = async (page = 0, size = 10, search = '') => {
    try {
        // use URL constructor to handle parameters safely
        const url = new URL(`${API_BASE_URL}/students`);

        url.searchParams.append("page", page);
        url.searchParams.append("size", size);

        // Only append search if it's not empty
        if (search) {
            url.searchParams.append("search", search);
        }

        const response = await fetch(url);

        if (!response.ok) {
            // Log the error to see if it's a 404 or 500
            console.error("Server Error:", response.status);
            throw new Error("Failed to fetch students");
        }

        return await response.json();
    } catch (error) {
        console.error("API Call Failed:", error);
        throw error;
    }
};

// Delete a student record
export async function deleteStudent(studentId) {
    const token = getAuthToken();

    const response = await fetch(`${API_BASE_URL}/students/${studentId}`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        }
    });

    if (!response.ok) {
        if (response.status === 404) {
            throw new Error(`Student with ID ${studentId} not found.`);
        }
        const errorMessage = await response.text();
        throw new Error(errorMessage || `Failed to delete student: ${response.status}`);
    }

    try {
        const text = await response.text();
        return text || 'Student deleted successfully';
    } catch {
        return 'Student deleted successfully';
    }
}

// Get student transcript
export async function getTranscript(studentId) {
    const token = getAuthToken();

    const response = await fetch(`${API_BASE_URL}/students/${studentId}/transcript`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        }
    });

    if (!response.ok) {
        if (response.status === 404) {
            throw new Error('Student not found.');
        }
        const errorMessage = await response.text();
        throw new Error(errorMessage || `Failed to retrieve transcript: ${response.status}`);
    }

    return await response.text();
}


// Get a specific course by code
export async function getCourse(courseCode) {
    const token = getAuthToken();
    const response = await fetch(`${API_BASE_URL}/courses/${courseCode}`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        }
    });

    if (!response.ok) {
        throw new Error(`Failed to fetch course: ${courseCode}`);
    }

    return await response.json();
}

// Get all courses a student is enrolled in
export async function getEnrolledCourses(studentId) {
    try {
        // 1. Get current student data to see list of course codes
        const student = await getStudent(studentId);

        // 2. Extract the list of course codes (e.g. ["CS101", "MATH202"])
        const courseCodes = student.currentCourses || [];

        if (courseCodes.length === 0) {
            return [];
        }

        // 3. Fetch full details for each course code
        const coursePromises = courseCodes.map(code => getCourse(code));
        const courses = await Promise.all(coursePromises);

        return courses;
    } catch (error) {
        console.error("Error fetching enrolled courses:", error);
        throw error;
    }
}

///////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////// API-FUNCTIONS-FOR-ADMIN-STUDENT-RELATED-SERVICES //////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////

//-----------------------------------------------------------------------------------------------//

/////////////////////// EMPTY-STUDENT-DATA-MODEL //////////////////////////
export const emptyStudent = {
    code: '',
    name: '',
    email: '',
    phone: '',
    militaryStatus: '',
    address: '',
    majorId: '',
    majorName: '',
    nationalId: '',
    dateOfBirth: '',
    gradYear: '',
    completedHours: 0,
    fees: 0,
    status: 'Active',
    cgpa: 0,
    academicHistory: [],
    currentRegistrations: [],
    notes: '',
    holds: []
};
/////////////////////// EMPTY-STUDENT-DATA-MODEL //////////////////////////

export const downloadBlob = (content, filename, mime) => {
    const blob = new Blob([content], { type: mime });
    const url = URL.createObjectURL(blob);
    const anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = filename;
    anchor.click();
    URL.revokeObjectURL(url);
};

export const sumCredits = (records) =>
    records.reduce((total, record) => total + (record.credits ?? 3), 0);

// Updated to read BACKEND keys (studentId, gpa, completedCourses)
export const buildStudentSnapshot = (student) => {

    // Calculate totals inline if backend doesn't provide them
    const totalHours = student.completedCourses?.reduce((acc, c) => acc + (c.credits || 0), 0) || 0;

    return {
        // Map Backend Key -> CSV Column Name
        code: student.studentId,
        name: student.name,
        email: student.email,
        phone: student.phone,

        // Handle nested major object safely
        major: student.major?.majorName || student.majorName || '',

        status: student.militaryStatus, // Or status if you have a separate field
        cgpa: student.gpa,
        completedHours: totalHours,

        feesDue: student.fees || 0,
        gradYear: student.gradYear || '',
        militaryStatus: student.militaryStatus,
        address: student.address,
        nationalId: student.nationalId,
        dateOfBirth: student.dateOfBirth,

        // Arrays
        currentRegistrations: student.currentCourses || [],
        academicHistory: student.completedCourses || []
    };
};

export const Icon = {
    student: (
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
            <path d="M3 7l9-4 9 4-9 4-9-4z" />
            <path d="M12 11v6" />
            <path d="M6 13.5c1.8 1.2 3.8 1.8 6 1.8s4.2-.6 6-1.8" />
            <path d="M18 9l3 1.5-3 1.5" />
        </svg>
    ),
    facilities: (
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
            <path d="M3 10l9-6 9 6" />
            <path d="M4 10h16" />
            <path d="M6 10v9" />
            <path d="M10 10v9" />
            <path d="M14 10v9" />
            <path d="M18 10v9" />
            <path d="M3 22h18" />
        </svg>
    ),
    analytics: (
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
            <path d="M3 3v18h18" />
            <rect x="7" y="12" width="3" height="6" rx="1" />
            <rect x="12" y="9" width="3" height="9" rx="1" />
            <rect x="17" y="6" width="3" height="12" rx="1" />
        </svg>
    ),
    requests: (
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
            <rect x="4" y="3" width="12" height="18" rx="2" />
            <path d="M8 8h4" />
            <path d="M8 12h6" />
            <path d="M14 3v4h4" />
            <path d="M16 17l2 2 4-4" />
        </svg>
    ),
    menu16: (
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M4 6h16M4 12h16M4 18h16" /></svg>
    ),
    home16: (
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M3 12l9-8 9 8" /><path d="M5 10v10h14V10" /></svg>
    ),
    help16: (
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M9.1 9a3 3 0 0 1 5.8 1c0 1.5-1 2.2-1.8 2.8-.7.5-1.1.9-1.1 1.7V15" /><circle cx="12" cy="18" r="0.5" /></svg>
    ),
    bell16: (
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M18 8a6 6 0 1 0-12 0c0 7-3 7-3 7h18s-3 0-3-7" /><path d="M13.73 21a2 2 0 0 1-3.46 0" /></svg>
    ),
    msg16: (
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5" /><path d="M22 4 12 14" /><path d="M16 4h6v6" /></svg>
    ),
    user16: (
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" /><circle cx="12" cy="7" r="4" /></svg>
    ),
    plus16: (
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M12 5v14M5 12h14" /></svg>
    ),
    edit16: (
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M17 3a2.8 2.8 0 0 1 4 4L7 21l-4 1 1-4Z" /><path d="m15 5 4 4" /></svg>
    ),
    trash16: (
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M3 6h18" /><path d="M8 6V4h8v2" /><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6" /><path d="M10 11v6M14 11v6" /></svg>
    ),
    download16: (
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M12 3v12" /><path d="m7 12 5 5 5-5" /><path d="M5 21h14" /></svg>
    ),
    check16: (
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M20 6 9 17l-5-5" /></svg>
    ),
    close16: (
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="m4 4 16 16M20 4 4 20" /></svg>
    ),
    spinner: (
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="spinner">
            <path d="M21 12a9 9 0 1 1-6.219-8.56" />
        </svg>
    ),
    search16: (
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none"
            stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
            <circle cx="11" cy="11" r="7" />
            <line x1="16.65" y1="16.65" x2="21" y2="21" />
        </svg>
    ),
    professor: (
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
            <circle cx="12" cy="7" r="4" />
            <path d="M16 3.13a4 4 0 0 1 0 7.75" />
        </svg>
    ),
    megaphone: (
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
            <path d="M3 11l18-5v12l-18-5v-2z" />
            <path d="M11.6 16.8a3 3 0 1 1-5.8-1.6" />
        </svg>
    ),

};

export const LoadingSpinner = ({ size = 'medium', message = 'Loading...' }) => (
    <div className={`loading-spinner ${size}`}>
        {Icon.spinner}
        {message && <span>{message}</span>}
    </div>
);

export const ErrorMessage = ({ error, onDismiss }) => {
    if (!error) return null;

    return (
        <div className="error-message" role="alert">
            <div className="error-content">
                <strong>Error:</strong> {error}
            </div>
            {onDismiss && (
                <button type="button" className="ghost-btn" onClick={onDismiss} aria-label="Dismiss error">
                    {Icon.close16}
                </button>
            )}
        </div>
    );
};

