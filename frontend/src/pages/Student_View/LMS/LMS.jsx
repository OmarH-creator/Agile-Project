import React, { useState, useEffect } from "react";
import axios from "axios";
import { jwtDecode } from "jwt-decode";
import "./LMS.css"; // Importing the CSS file

// CONFIG
const BASE_URL = "http://localhost:8081/api";

const LMS = () => {
    // State
    const [view, setView] = useState("courses"); // 'courses' | 'assignments' | 'detail'
    const [courses, setCourses] = useState([]);
    const [studentId, setStudentId] = useState(null);
    const [selectedCourse, setSelectedCourse] = useState(null);
    const [assignments, setAssignments] = useState([]);
    const [selectedAssignment, setSelectedAssignment] = useState(null);
    const [submission, setSubmission] = useState(null);
    const [submissionFields, setSubmissionFields] = useState([{ key: "Content", value: "" }]);
    const [isEditing, setIsEditing] = useState(false);
    const [loading, setLoading] = useState(false);

    // --- 1. Fetch Courses (Mock or Real) ---
    // --- 1. Fetch Courses (Real) ---
    useEffect(() => {
        const fetchStudentData = async () => {
            try {
                const token = localStorage.getItem("token");
                if (!token) return;

                const decoded = jwtDecode(token);
                // User requested to take email from token
                const email = decoded.sub;
                console.log("Logged in as:", email);
                const id = decoded.businessId || localStorage.getItem("userId");
                setStudentId(id);

                if (!id) {
                    console.error("Student ID not found in token or local storage.");
                    return;
                }

                // 1. Fetch Student Profile to get enrolled course codes
                const profileRes = await axios.get(`${BASE_URL}/student/${id}/profile`);
                // The backend returns a detailed object; currentCourses is a list of strings (codes)
                const enrolledCodes = profileRes.data.currentCourses || [];

                // 2. Fetch All Courses to get names and details
                // (Optimally this would be a specific endpoint given a list of codes, but we reuse existing)
                const allCoursesRes = await axios.get(`${BASE_URL}/student/courses`);
                const allCourses = allCoursesRes.data;

                // 3. Match enrolled codes with course details
                const myCourses = allCourses
                    .filter((c) => enrolledCodes.includes(c.courseCode))
                    .map((c) => ({
                        id: c.courseCode,
                        name: c.courseName,
                        code: c.courseCode
                    }));

                setCourses(myCourses);
            } catch (error) {
                console.error("Error fetching student courses:", error);
            }
        };

        fetchStudentData();
    }, []);

    // --- 2. Handle Course Selection ---
    const handleCourseClick = async (course) => {
        setLoading(true);
        setSelectedCourse(course);
        try {
            // Fetch Assignments for this Course
            // API: GET /api/grading/task/course/{courseCode}
            const res = await axios.get(`${BASE_URL}/grading/task/course/${course.code}`);
            setAssignments(res.data);
            console.log(res.data);
            setView("assignments");
        } catch (error) {
            console.error("Failed to fetch assignments", error);
            // Fallback for demo if API is empty
            setAssignments([
                { id: 101, title: "Lab 1: Hello World", due_Date: "2025-10-15", description: "Print Hello World in Java." }
            ]);
            setView("assignments");
        } finally {
            setLoading(false);
        }
    };

    // --- 3. Handle Assignment Selection ---
    const handleAssignmentClick = async (assignment) => {
        setLoading(true);
        setSelectedAssignment(assignment);
        setSubmission(null);
        setSubmissionFields([{ key: "Content", value: "" }]);
        setIsEditing(false); // Reset edit mode

        try {
            // [NEW] 1. Fetch Full Assignment Details
            const detailRes = await axios.get(`${BASE_URL}/grading/task/${assignment.id}`);
            const detailedAssignment = detailRes.data;
            setSelectedAssignment(detailedAssignment);

            // 2. Check if student already submitted
            // API: GET /api/grading/sub-check/{assignmentId}/student/{studentId}
            const res = await axios.get(
                `${BASE_URL}/grading/sub-check/${assignment.id}/student/${studentId}`
            );

            if (res.data) {
                setSubmission(res.data);
                // Parse existing fields from response
                if (res.data.data) {
                    const fields = Object.entries(res.data.data)
                        .filter(([k]) => !['Grade', 'Feedback', 'Assignment_Id', 'Student_Id', 'Assignment_Title'].some(ex => k.toLowerCase() === ex.toLowerCase())) // Filter out prof & internal fields
                        .map(([k, v]) => ({ key: k, value: v }));

                    if (fields.length > 0) {
                        setSubmissionFields(fields);
                    }
                }
            }
        } catch (error) {
            console.log("No submission found (404 is expected here for new tasks).", error);
        } finally {
            setView("detail");
            setLoading(false);
        }
    };

    // --- 4. CRUD Operations ---
    const handleSubmit = async () => {
        // Construct payload from fields
        // Construct payload from fields
        const dynamicContent = {};
        for (const field of submissionFields) {
            // Skip Submission_Date so we can set it freshly to "now"
            // Also skip redundant internal fields if they somehow got in
            if (/submission[-_ ]?date/i.test(field.key)) continue;

            if (field.key.trim()) {
                dynamicContent[field.key.trim()] = field.value;
            }
        }

        if (Object.keys(dynamicContent).length === 0) return alert("Please add at least one field.");

        // Use local date to avoid "minus a day" issues with UTC conversion
        // e.g., 2025-12-25T23:00 (Local) -> 2025-12-26T04:00 (UTC) -> split('T')[0] gives 2025-12-26
        // or 2025-12-26T01:00 (Local) -> 2025-12-25T20:00 (UTC) -> split('T')[0] gives 2025-12-25 (Wrong!)
        const now = new Date();
        const localISODate = now.toLocaleDateString('en-CA'); // YYYY-MM-DD in local time

        const payload = {
            Assignment_Id: selectedAssignment.id,
            Student_Id: studentId,
            Submission_Date: localISODate,
            ...dynamicContent // Spread dynamic fields into the root payload (DTO handles mapping)
        };

        try {
            if (submission) {
                // EDIT Existing
                await axios.put(`${BASE_URL}/grading/sub-update/${submission.id}`, payload);
                alert("Submission Updated Successfully!");
                setIsEditing(false); // Exit edit mode
            } else {
                // CREATE New
                const res = await axios.post(`${BASE_URL}/grading/submission/create`, payload);
                setSubmission(res.data);
            }
        } catch (error) {
            alert("Error: " + error.message);
        }
    };

    const handleDelete = async () => {
        if (!window.confirm("Are you sure? This cannot be undone.")) return;

        try {
            await axios.delete(`${BASE_URL}/grading/sub-delete/${submission.id}`);
            setSubmission(null);
            setSubmissionFields([{ key: "Content", value: "" }]);
            setIsEditing(false);
            alert("Submission Deleted.");
        } catch (error) {
            alert("Error deleting: " + error.message);
        }
    };

    // --- RENDER FUNCTIONS ---

    // View A: Courses
    const renderCourses = () => (
        <div className="course-grid">
            {courses.map((course) => (
                <div key={course.id} className="course-card" onClick={() => handleCourseClick(course)}>
                    <div className="course-icon">📚</div>
                    <h3 className="course-name">{course.name}</h3>
                    <span className="course-code">{course.code}</span>
                </div>
            ))}
        </div>
    );

    // View B: Assignments List
    const renderAssignments = () => (
        <div>
            <button className="back-button" onClick={() => setView("courses")}>
                ← Back to Courses
            </button>
            <h2 className="detail-title">Assignments for {selectedCourse?.name}</h2>
            <div className="assignment-list">
                {assignments.length === 0 ? <p style={{ color: '#94a3b8' }}>No assignments found.</p> : null}
                {assignments.map((asn) => (
                    <div key={asn.id} className="assignment-item" onClick={() => handleAssignmentClick(asn)}>
                        <div className="assignment-info">
                            <h4>{asn.data.title || asn.data.Title}</h4>
                        </div>
                        <button className="view-btn">View</button>
                    </div>
                ))}
            </div>
        </div>
    );

    // View C: Assignment Details & Submission
    const renderDetail = () => (
        <div className="detail-view">
            <button className="back-btn" onClick={() => setView("assignments")}>
                ← Back to Assignment List
            </button>

            <div className="detail-header">
                <h2 className="detail-title">
                    {selectedAssignment?.data?.Title || selectedAssignment?.data?.title || "Assignment Details"}
                </h2>
                <div className="instruction-box">
                    {selectedAssignment?.data ? (
                        <>
                            {/* Status Badge Calculation */}
                            {(() => {
                                const dueDateEntry = Object.entries(selectedAssignment.data).find(([k]) => /due[-_ ]?date|deadline/i.test(k));
                                if (dueDateEntry) {
                                    const dueDate = new Date(dueDateEntry[1]);
                                    const now = new Date();

                                    // 1. Check if Submitted
                                    if (submission && submission.data) {
                                        const subDateEntry = Object.entries(submission.data).find(([k]) => /submission[-_ ]?date/i.test(k));
                                        if (subDateEntry) {
                                            const subDate = new Date(subDateEntry[1]);
                                            if (subDate > dueDate) {
                                                // Case 1: Submitted Late
                                                return (
                                                    <div style={{ marginBottom: '1.5rem', padding: '0.5rem', borderRadius: '4px', background: 'rgba(239, 68, 68, 0.2)', border: '1px solid #ef4444', display: 'inline-block' }}>
                                                        <strong style={{ color: '#fca5a5' }}>⚠️ Late Assignment</strong>
                                                    </div>
                                                );
                                            } else {
                                                // Case 4: Submitted On Time -> Show Nothing
                                                return null;
                                            }
                                        }
                                    } else {
                                        // Not Submitted Yet
                                        if (now > dueDate) {
                                            // Case 2: Overdue (Late)
                                            return (
                                                <div style={{ marginBottom: '1.5rem', padding: '0.5rem', borderRadius: '4px', background: 'rgba(239, 68, 68, 0.2)', border: '1px solid #ef4444', display: 'inline-block' }}>
                                                    <strong style={{ color: '#fca5a5' }}>⚠️ Late Assignment</strong>
                                                </div>
                                            );
                                        } else {
                                            // Case 3: Upcoming
                                            return (
                                                <div style={{ marginBottom: '1.5rem', padding: '0.5rem', borderRadius: '4px', background: 'rgba(34, 197, 94, 0.2)', border: '1px solid #22c55e', display: 'inline-block' }}>
                                                    <strong style={{ color: '#86efac' }}>📅 Upcoming Assignment</strong>
                                                </div>
                                            );
                                        }
                                    }
                                }
                                return null;
                            })()}

                            {Object.entries(selectedAssignment.data).map(([key, value]) => {
                                // Filter out redundant fields
                                if (/professor|course|title/i.test(key)) return null;

                                // Helper to render values based on type
                                let displayValue = String(value);

                                // 1. Boolean Styling
                                if (typeof value === 'boolean' || value === 'true' || value === 'false') {
                                    const isTrue = String(value) === 'true';
                                    displayValue = (
                                        <span style={{
                                            color: isTrue ? '#4ade80' : '#f87171',
                                            fontWeight: 'bold',
                                            padding: '2px 6px',
                                            borderRadius: '4px',
                                            background: isTrue ? 'rgba(74, 222, 128, 0.1)' : 'rgba(248, 113, 113, 0.1)'
                                        }}>
                                            {isTrue ? "Yes" : "No"}
                                        </span>
                                    );
                                }
                                // 2. Date Formatting
                                else if ((/date|deadline|time/i.test(key)) && !isNaN(Date.parse(value))) {
                                    displayValue = new Date(value).toLocaleString(undefined, {
                                        year: 'numeric', month: 'long', day: 'numeric',
                                        hour: '2-digit', minute: '2-digit'
                                    });
                                }

                                return (
                                    <div key={key} style={{ marginBottom: '1rem', borderBottom: '1px solid #334155', paddingBottom: '0.5rem' }}>
                                        <strong style={{ color: '#94a3b8', textTransform: 'capitalize', display: 'block', marginBottom: '0.25rem', fontSize: '0.9rem' }}>
                                            {key.replace(/_/g, ' ')}
                                        </strong>
                                        <div style={{ paddingLeft: '0.5rem', color: '#e2e8f0' }}>
                                            {displayValue}
                                        </div>
                                    </div>
                                );
                            })}
                        </>
                    ) : (
                        <p>No details available.</p>
                    )}
                </div>
            </div>

            <div className="submission-section">
                <h3 style={{ marginBottom: '1rem', color: 'white' }}>Your Work</h3>

                {submission ? (
                    <span className="status-badge status-submitted">✓ Submitted</span>
                ) : (
                    <span className="status-badge status-pending">⚠ Not Submitted</span>
                )}

                {/* MODE TOGGLE: SUMMARY vs EDIT */}
                {!submission || isEditing ? (
                    // --- FORM VIEW (Create or Edit) ---
                    <div className="dynamic-form" style={{ marginTop: '1rem' }}>
                        {submissionFields.map((field, index) => {
                            // Check if field is read-only (dates)
                            const isReadOnly = /date|time/i.test(field.key);
                            return (
                                <div key={index} style={{ display: 'flex', gap: '10px', marginBottom: '10px' }}>
                                    <input
                                        className="submission-input"
                                        style={{ flex: 1, minHeight: '40px', background: isReadOnly ? '#334155' : 'transparent', color: isReadOnly ? '#94a3b8' : 'white' }}
                                        placeholder="Field Name"
                                        value={field.key}
                                        readOnly={isReadOnly} // Key is always read-only for dates to prevent renaming crucial fields
                                        onChange={(e) => {
                                            const newFields = [...submissionFields];
                                            newFields[index].key = e.target.value;
                                            setSubmissionFields(newFields);
                                        }}
                                    />
                                    <textarea
                                        className="submission-input"
                                        style={{ flex: 2, minHeight: '40px', background: isReadOnly ? '#334155' : 'transparent', color: isReadOnly ? '#94a3b8' : 'white' }}
                                        placeholder="Value"
                                        value={field.value}
                                        readOnly={isReadOnly}
                                        onChange={(e) => {
                                            const newFields = [...submissionFields];
                                            newFields[index].value = e.target.value;
                                            setSubmissionFields(newFields);
                                        }}
                                    />
                                    {!isReadOnly && submissionFields.length > 1 && (
                                        <button
                                            onClick={() => {
                                                const newFields = submissionFields.filter((_, i) => i !== index);
                                                setSubmissionFields(newFields);
                                            }}
                                            style={{ background: '#ef4444', border: 'none', color: 'white', borderRadius: '4px', cursor: 'pointer', padding: '0 10px' }}
                                        >
                                            ✕
                                        </button>
                                    )}
                                </div>
                            );
                        })}

                        <div style={{ display: 'flex', gap: '10px', marginBottom: '1rem' }}>
                            <button
                                onClick={() => setSubmissionFields([...submissionFields, { key: "", value: "" }])}
                                style={{ background: '#3b82f6', border: 'none', color: 'white', padding: '5px 10px', borderRadius: '4px', cursor: 'pointer' }}
                            >
                                + Add Field
                            </button>
                        </div>

                        <div className="action-buttons">
                            <button className="btn btn-primary" onClick={handleSubmit}>
                                {submission ? "Save Changes" : "Submit Assignment"}
                            </button>
                            {isEditing && (
                                <button className="btn" style={{ background: '#64748b', marginLeft: '10px' }} onClick={() => setIsEditing(false)}>
                                    Cancel
                                </button>
                            )}
                        </div>
                    </div>
                ) : (
                    // --- SUMMARY VIEW (Read-Only) ---
                    <div className="submission-summary" style={{ background: 'rgba(51, 65, 85, 0.3)', padding: '1rem', borderRadius: '8px', marginTop: '1rem' }}>
                        {submission.data && Object.keys(submission.data).length > 0 ? (
                            Object.entries(submission.data)
                                .filter(([k]) => !['Grade', 'Feedback', 'Assignment_Id', 'Student_Id', 'Assignment_Title', 'Title'].some(ex => k.toLowerCase() === ex.toLowerCase()))
                                .map(([key, value]) => {
                                    let displayValue = String(value);
                                    if (/date|time/i.test(key) && !isNaN(Date.parse(value))) {
                                        displayValue = new Date(value).toLocaleString(undefined, { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' });
                                    }
                                    return (
                                        <div key={key} style={{ marginBottom: '0.8rem', borderBottom: '1px solid #334155', paddingBottom: '0.5rem' }}>
                                            <strong style={{ color: '#94a3b8', display: 'block', fontSize: '0.8rem', textTransform: 'capitalize' }}>{key.replace(/_/g, ' ')}</strong>
                                            <div style={{ color: 'white', marginTop: '0.25rem', whiteSpace: 'pre-wrap' }}>{displayValue}</div>
                                        </div>
                                    );
                                })
                        ) : (
                            <p style={{ color: '#94a3b8' }}>No content provided.</p>
                        )}

                        <div className="action-buttons" style={{ marginTop: '1.5rem' }}>
                            <button className="btn btn-edit" onClick={() => setIsEditing(true)}>
                                Edit Submission
                            </button>
                            <button className="btn btn-delete" onClick={handleDelete}>
                                Delete Submission
                            </button>
                        </div>
                    </div>
                )}


                {/* Professor Feedback Section */}
                {submission && submission.data && (submission.data.Grade || submission.data.Feedback) && (
                    <div className="grade-box" style={{ marginTop: '2rem', borderTop: '1px solid #334155', paddingTop: '1rem' }}>
                        <h4 style={{ color: '#fbbf24', marginBottom: '1rem' }}>🎓 Professor Feedback</h4>

                        {submission.data.Grade && (
                            <div style={{ marginBottom: '0.5rem' }}>
                                <span style={{ color: '#94a3b8', textTransform: 'uppercase', fontSize: '0.8rem', marginRight: '1rem' }}>Grade:</span>
                                <span className="grade-score">{submission.data.Grade} / 100</span>
                            </div>
                        )}

                        {submission.data.Feedback && (
                            <div>
                                <span style={{ color: '#94a3b8', textTransform: 'uppercase', fontSize: '0.8rem', display: 'block', marginBottom: '0.2rem' }}>Comments:</span>
                                <p style={{ color: '#e2e8f0', background: 'rgba(51, 65, 85, 0.5)', padding: '1rem', borderRadius: '8px' }}>
                                    "{submission.data.Feedback}"
                                </p>
                            </div>
                        )}
                    </div>
                )}
            </div>
        </div>
    );

    return (
        <div className="lms-container">
            <header className="lms-header">
                <div>
                    <h1 className="lms-title">Student Portal</h1>
                    <p className="lms-subtitle">Welcome back, Student {studentId}</p>
                </div>
                <div style={{ width: '40px', height: '40px', background: '#334155', borderRadius: '50%' }}></div>
            </header>

            {loading && <p style={{ color: '#60a5fa', textAlign: 'center' }}>Loading data...</p>}

            {!loading && (
                <>
                    {view === "courses" && renderCourses()}
                    {view === "assignments" && renderAssignments()}
                    {view === "detail" && renderDetail()}
                </>
            )}
        </div>
    );
};

export default LMS;