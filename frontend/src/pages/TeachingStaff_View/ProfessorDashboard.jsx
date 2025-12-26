import React, { useState, useEffect } from 'react';
import './ProfessorDashboard.css';
import umsLogo from '../../assets/UMS Logo.png';
import { ProfessorAPI, Icon } from './Professor-API';

const ProfessorDashboard = () => {
    // --- STATE ---
    const [professorId] = useState(localStorage.getItem("userId") || "P-101");
    const [courses, setCourses] = useState([]);
    const [loading, setLoading] = useState(true);

    // --- NAVIGATION STATE ---
    const [selectedCourse, setSelectedCourse] = useState(null);
    const [activeTab, setActiveTab] = useState('courses'); // 'courses', 'requests', 'halls'
    const [courseSubTab, setCourseSubTab] = useState('students'); // 'students' or 'assignments'

    // --- DATA STATE ---
    const [students, setStudents] = useState([]);
    const [assignments, setAssignments] = useState([]);
    const [gradingItems, setGradingItems] = useState([]);
    const [submissions, setSubmissions] = useState([]); // NEW STATE for Submissions
    const [requests, setRequests] = useState([]);
    const [halls, setHalls] = useState([]);


    // --- LOADING STATES ---
    const [loadingStudents, setLoadingStudents] = useState(false);
    const [loadingAssignments, setLoadingAssignments] = useState(false);
    const [loadingGradingItems, setLoadingGradingItems] = useState(false);
    const [loadingSubmissions, setLoadingSubmissions] = useState(false); // NEW STATE
    const [loadingRequests, setLoadingRequests] = useState(false);
    const [loadingHalls, setLoadingHalls] = useState(false);

    // --- MODAL / FORM STATE ---
    const [showCreateAssign, setShowCreateAssign] = useState(false);
    // Added Max Grade and Editing ID
    // Added Max Grade, Editing ID, and Custom Attributes
    const [newAssignData, setNewAssignData] = useState({
        title: '',
        description: '',
        deadline: '',
        maxGrade: 100,
        file: null,
        customAttributes: [] // Array of { key: '', value: '' }
    });
    const [editingAssignmentId, setEditingAssignmentId] = useState(null);

    // Request Form State
    const [newRequestData, setNewRequestData] = useState({ type: 'Equipment', description: '', priority: 'Medium' });
    const [showCreateRequest, setShowCreateRequest] = useState(false);

    // Hall Booking Form State
    const [bookingData, setBookingData] = useState({ hallName: '', dayOfWeek: 'Monday', startTime: '09:00', endTime: '10:00' });

    // Grading Bucket Form State
    const [newGradingItem, setNewGradingItem] = useState({ categoryName: '', weight: '' });
    const [showCreateGradingItem, setShowCreateGradingItem] = useState(false);

    // Submissions Modal State
    const [viewingAssignment, setViewingAssignment] = useState(null); // The assignment we are grading
    const [showSubmissionsModal, setShowSubmissionsModal] = useState(false);

    // Payment
    const [payment, setPayment] = useState(null);
    const [loadingPayment, setLoadingPayment] = useState(false);



    // --- EFFECTS ---

    // 1. Load Courses on Mount
    useEffect(() => {
        const fetchCourses = async () => {
            try {
                const data = await ProfessorAPI.getCourses(professorId);
                // Ensure we always have an array for courses too
                setCourses(Array.isArray(data) ? data : []);
            } catch (err) {
                console.error("Failed to load courses", err);
                setCourses([]);
            } finally {
                setLoading(false);
            }
        };
        fetchCourses();
    }, [professorId]);

    // 2. Load Course Details (Students & Assignments) when course selected
    useEffect(() => {
        if (selectedCourse) {
            // Load Students
            const fetchStudents = async () => {
                setLoadingStudents(true);
                try {
                    const data = await ProfessorAPI.getStudentsInCourse(selectedCourse);
                    // We set data directly here, but the defensive check in render handles if it's not an array
                    setStudents(data);
                } catch (err) {
                    console.error("Failed to load students", err);
                    setStudents([]);
                } finally {
                    setLoadingStudents(false);
                }
            };

            // Load Assignments
            const fetchAssignments = async () => {
                setLoadingAssignments(true);
                try {
                    const data = await ProfessorAPI.getAssignments(selectedCourse);
                    setAssignments(Array.isArray(data) ? data : []);
                } catch (err) {
                    console.error("Failed to load assignments", err);
                    setAssignments([]);
                } finally {
                    setLoadingAssignments(false);
                }
            };

            // Load Grading Buckets (NEW)
            const fetchGradingItems = async () => {
                setLoadingGradingItems(true);
                try {
                    const data = await ProfessorAPI.getGradingItemsByCourse(selectedCourse);
                    setGradingItems(Array.isArray(data) ? data : []);
                } catch (err) {
                    console.error("Failed to load grading items", err);
                    setGradingItems([]);
                } finally {
                    setLoadingGradingItems(false);
                }
            };

            fetchStudents();
            fetchAssignments();
            fetchGradingItems();
            setCourseSubTab('students'); // Reset tab
            setShowCreateAssign(false);
        }
    }, [selectedCourse]);

    // 3. Load Requests when tab active
    useEffect(() => {
        if (activeTab === 'requests') {
            const fetchRequests = async () => {
                setLoadingRequests(true);
                try {
                    const data = await ProfessorAPI.getRequests(professorId);
                    setRequests(Array.isArray(data) ? data : []);
                } catch (err) {
                    setRequests([]);
                } finally {
                    setLoadingRequests(false);
                }
            };
            fetchRequests();
        }
    }, [activeTab, professorId]);

    // 4. Load Halls when tab active
    useEffect(() => {
        if (activeTab === 'halls') {
            const fetchHalls = async () => {
                setLoadingHalls(true);
                try {
                    // Requires newly added API method
                    const data = await ProfessorAPI.getAllHalls();
                    setHalls(Array.isArray(data) ? data : []);
                } catch (err) {
                    console.error("Failed to load halls", err);
                    setHalls([]);
                } finally {
                    setLoadingHalls(false);
                }
            };
            fetchHalls();
        }
    }, [activeTab]);


    // Add this useEffect (after the other useEffects)
    useEffect(() => {
        if (activeTab === 'payment') {
            const fetchPayment = async () => {
                setLoadingPayment(true);
                try {
                    const data = await ProfessorAPI.getProfessorPayment(professorId);
                    setPayment(data.payment);
                } catch (err) {
                    console.error("Failed to load payment", err);
                    setPayment(null);
                } finally {
                    setLoadingPayment(false);
                }
            };
            fetchPayment();
        }
    }, [activeTab, professorId]);


    // --- HANDLERS ---

    const handleCourseClick = (courseName) => {
        setSelectedCourse(courseName);
        setActiveTab('courses'); // Stay in course view
    };

    const handleBackToDashboard = () => {
        setSelectedCourse(null);
        setStudents([]);
        setAssignments([]);
        setActiveTab('courses');
    };

    // --- ACTION: Final Course Grade ---
    const handleAssignFinalGrade = async (studentId) => {
        const grade = prompt("Enter Final Course Grade (0.0 - 4.0):");
        if (grade) {
            try {
                await ProfessorAPI.assignFinalGrade({
                    studentId: studentId,
                    courseName: selectedCourse,
                    grade: parseFloat(grade),
                    semester: "Fall 2025"
                });
                alert("Course grade assigned & student moved to history.");
                // Optimistic update: remove student from list
                setStudents(prev => prev.filter(s => s.studentId !== studentId));
            } catch (e) {
                alert("Error assigning grade.");
            }
        }
    };

    // ... (skipped some lines)

    // --- ACTION: Create/Update Assignment ---
    const handleCreateAssignment = async (e) => {
        e.preventDefault();
        try {
            if (editingAssignmentId) {
                // UPDATE
                // Merge static fields with custom attributes
                const updatePayload = {
                    title: newAssignData.title,
                    description: newAssignData.description,
                    maxGrade: newAssignData.maxGrade,
                    deadline: newAssignData.deadline,
                    Grading_Item_Id: newAssignData.gradingItemId // Add bucket ID
                };
                // Add custom attributes to payload
                newAssignData.customAttributes.forEach(attr => {
                    if (attr.key && attr.value) {
                        updatePayload[attr.key] = attr.value;
                    }
                });

                await ProfessorAPI.updateAssignment(editingAssignmentId, updatePayload);
                alert("Assignment Updated!");
            } else {
                // CREATE
                // Merge static fields with custom attributes
                const createPayload = {
                    title: newAssignData.title,
                    description: newAssignData.description,
                    courseName: selectedCourse,
                    professorId: professorId,
                    deadline: newAssignData.deadline,
                    maxGrade: newAssignData.maxGrade,
                    deadline: newAssignData.deadline,
                    maxGrade: newAssignData.maxGrade,
                    file: newAssignData.file,
                    Grading_Item_Id: newAssignData.gradingItemId // Add bucket ID
                };
                // Add custom attributes to payload
                newAssignData.customAttributes.forEach(attr => {
                    if (attr.key && attr.value) {
                        createPayload[attr.key] = attr.value;
                    }
                });

                await ProfessorAPI.createAssignment(createPayload);
                alert("Assignment Created!");
            }

            setNewAssignData({ title: '', description: '', deadline: '', maxGrade: 100, file: null, customAttributes: [], gradingItemId: '' });
            setShowCreateAssign(false);
            setEditingAssignmentId(null);

            // Refresh list
            const data = await ProfessorAPI.getAssignments(selectedCourse);
            setAssignments(Array.isArray(data) ? data : []);
        } catch (e) {
            console.error(e);
            const errorMsg = e.response?.data ? (typeof e.response.data === 'object' ? JSON.stringify(e.response.data) : e.response.data) : e.message;
            alert("Failed to save assignment: " + errorMsg);
        }
    };

    // --- ACTION: View Submissions ---
    const handleViewSubmissions = async (assignment) => {
        setViewingAssignment(assignment);
        setShowSubmissionsModal(true);
        setLoadingSubmissions(true);
        try {
            const data = await ProfessorAPI.getSubmissions(assignment.id);
            setSubmissions(Array.isArray(data) ? data : []);
        } catch (err) {
            console.error(err);
            const msg = err.response?.data ? (typeof err.response.data === 'object' ? JSON.stringify(err.response.data) : err.response.data) : err.message;
            alert("Error fetching submissions: " + msg);
            setSubmissions([]);
        } finally {
            setLoadingSubmissions(false);
        }
    };

    const handleEditClick = (assign) => {
        // assign is the DTO { id, data: { Title, ... } }
        setEditingAssignmentId(assign.id);
        // Parse Custom Attributes
        const standardKeys = ['Title', 'Description', 'Due_Date', 'Max_Grade', 'Attachment_Url', 'Is_Visible', 'Course_Id', 'Professor_Id', 'id'];
        const customAttrs = [];
        if (assign.data) {
            Object.keys(assign.data).forEach(key => {
                if (!standardKeys.includes(key)) {
                    customAttrs.push({ key: key, value: assign.data[key] });
                }
            });
        }

        setNewAssignData({
            title: assign.data?.Title || '',
            description: assign.data?.Description || '',
            deadline: assign.data?.Due_Date ? (new Date(assign.data.Due_Date).toISOString().slice(0, 16)) : '',
            maxGrade: assign.data?.Max_Grade || 100,
            file: null,
            customAttributes: customAttrs,
            gradingItemId: assign.data?.Grading_Item_Id || '' // Load existing bucket
        });
        setShowCreateAssign(true);
    };

    // --- ACTION: Grade Specific Assignment ---
    const handleGradeAssignment = async (assignmentId, studentId) => {
        const score = prompt("Enter Score for this assignment (0-100):");
        const feedback = prompt("Enter Feedback (Optional):");

        if (score) {
            try {
                await ProfessorAPI.gradeAssignment({
                    assignmentId: assignmentId,
                    studentId: studentId,
                    score: parseFloat(score),
                    feedback: feedback || ""
                });
                alert("Assignment Graded!");
            } catch (err) {
                alert("Error grading assignment");
            }
        }
    };

    const handleGradeSubmission = async (submissionId, currentScore, currentFeedback) => {
        // Simple prompt for now, could be a real form in the modal
        const score = prompt("Enter Grade (0-100):", currentScore || "");
        if (score === null) return; // Cancelled

        const feedback = prompt("Enter Feedback:", currentFeedback || "");
        if (feedback === null) return; // Cancelled

        try {
            // We need studentId and assignmentId, but the API expects { assignmentId, studentId, score, feedback }
            // OR we can use the EAV update endpoint if we have the submission ID.
            // ProfessorAPI.gradeAssignment uses: POST /api/professor/assignment/grade
            // Payload: { assignmentId, studentId, score, feedback }

            // We can find studentId and assignmentId from the submission object in the list
            const sub = submissions.find(s => s.id === submissionId);
            if (!sub) return;

            await ProfessorAPI.gradeAssignment({
                assignmentId: viewingAssignment.id,
                studentId: sub.studentId, // Ensure DTO has this
                score: parseFloat(score),
                feedback: feedback
            });

            alert("Grade saved!");
            // Refresh
            handleViewSubmissions(viewingAssignment);

        } catch (err) {
            alert("Error saving grade: " + err.message);
        }
    };

    // --- ACTION: Delete Assignment ---
    const handleDeleteAssignment = async (id) => {
        if (window.confirm("Are you sure you want to delete this assignment?")) {
            try {
                await ProfessorAPI.deleteAssignment(id);
                // Refresh list
                const data = await ProfessorAPI.getAssignments(selectedCourse);
                setAssignments(Array.isArray(data) ? data : []);
            } catch (err) {
                alert("Failed to delete assignment");
            }
        }
    };

    // --- HELPER: Custom Attributes Form Logic ---
    const addAttribute = () => {
        setNewAssignData(prev => ({
            ...prev,
            customAttributes: [...prev.customAttributes, { key: '', value: '' }]
        }));
    };

    const updateAttribute = (index, field, value) => {
        const updated = [...newAssignData.customAttributes];
        updated[index][field] = value;
        setNewAssignData({ ...newAssignData, customAttributes: updated });
    };

    const removeAttribute = (index) => {
        const updated = newAssignData.customAttributes.filter((_, i) => i !== index);
        setNewAssignData({ ...newAssignData, customAttributes: updated });
    };

    // --- ACTION: Create Request ---
    const handleCreateRequest = async (e) => {
        e.preventDefault();
        try {
            await ProfessorAPI.createRequest({
                professorId: professorId,
                Request_Type: newRequestData.type,
                Description: newRequestData.description,
                Priority: newRequestData.priority,
                Date_Submitted: new Date().toISOString().split('T')[0] // today
            });
            alert("Request Submitted!");
            setShowCreateRequest(false);
            setNewRequestData({ type: 'Equipment', description: '', priority: 'Medium' });

            // Refresh
            const data = await ProfessorAPI.getRequests(professorId);
            setRequests(data);
        } catch (err) {
            alert("Failed to submit request");
        }
    };

    // --- ACTION: Book Hall ---
    const handleBookHall = async (e) => {
        e.preventDefault();
        try {
            await ProfessorAPI.bookHall({
                professorId: professorId,
                hallName: bookingData.hallName,
                dayOfWeek: bookingData.dayOfWeek,
                startTime: bookingData.startTime,
                endTime: bookingData.endTime
            });
            alert("Hall Booked Successfully!");
        } catch (err) {
            alert("Failed to book hall: " + (err.response?.data || err.message));
        }
    };

    // --- ACTION: Create Grading Bucket ---
    const handleCreateGradingItem = async (e) => {
        e.preventDefault();
        try {
            // Extract core Course ID
            const realCourseId = selectedCourse.split(' - ')[0];

            await ProfessorAPI.createGradingItem({
                courseId: realCourseId,
                categoryName: newGradingItem.categoryName,
                weight: parseInt(newGradingItem.weight)
            });

            alert("Grading Bucket Created!");
            setNewGradingItem({ categoryName: '', weight: '' });
            setShowCreateGradingItem(false);

            // Refresh list
            const data = await ProfessorAPI.getGradingItemsByCourse(selectedCourse);
            setGradingItems(Array.isArray(data) ? data : []);

        } catch (err) {
            alert("Failed to create bucket: " + (err.response?.data || err.message));
        }
    };

    // --- RENDERERS ---
    const renderPaymentTab = () => (
        <div className="payment-container">
            <div className="page-header">
                <h2>Payment</h2>
                <p>Your monthly salary information</p>
            </div>

            {loadingPayment ? (
                <div className="card p-4">Loading payment information...</div>
            ) : payment === null ? (
                <div className="card p-4">No payment information available.</div>
            ) : (
                <div className="card" style={{ maxWidth: '400px' }}>
                    <div className="card-title">
                        <h3>Monthly Salary</h3>
                    </div>
                    <div style={{ padding: '2rem', textAlign: 'center' }}>
                        <div style={{ fontSize: '3rem', fontWeight: 'bold', color: '#10b981' }}>
                            ${payment}
                        </div>
                        <p style={{ color: '#666', marginTop: '1rem' }}>per month</p>
                        <div style={{ marginTop: '2rem', fontSize: '0.875rem', color: '#888' }}>
                            Paid on the 1st of every month
                        </div>
                    </div>
                </div>
            )}
        </div>
    );

    const renderCourseCards = () => (
        <div className="grid">
            {courses.map((courseName, index) => (
                <div
                    key={index}
                    className="card clickable-card"
                    data-accent={index % 2 === 0 ? 'violet' : 'emerald'}
                    onClick={() => handleCourseClick(courseName)}
                >
                    <div className="card-title">
                        <div>
                            <div className="card-heading">{courseName}</div>
                            <p className="card-sub">View Enrolled Students & Assignments</p>
                        </div>
                        <div className="card-icon" aria-hidden="true" data-accent={index % 2 === 0 ? 'violet' : 'emerald'}>
                            {Icon.course}
                        </div>
                    </div>
                </div>
            ))}
        </div>
    );

    const renderStudentsTab = () => (
        <div className="table-card">
            <div className="table-header">
                <h3>Enrolled Students</h3>
                <span className="badge">
                    {/* Safe access to length */}
                    {Array.isArray(students) ? students.length : 0} Active
                </span>
            </div>

            {loadingStudents ? (
                <div className="p-4">Loading...</div>
            ) : (
                /* DEFENSIVE FIX: Check if Array.isArray(students) is false OR length is 0 */
                (!Array.isArray(students) || students.length === 0) ? (
                    <div className="p-4">No students currently enrolled in this course.</div>
                ) : (
                    <table className="prof-table">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Name</th>
                                <th>Email</th>
                                <th style={{ textAlign: 'right' }}>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {students.map((student) => (
                                <tr key={student.studentId}>
                                    <td className="id-cell">{student.studentId}</td>
                                    <td className="name-cell">{student.name}</td>
                                    <td>{student.email}</td>
                                    <td style={{ textAlign: 'right' }}>
                                        <button
                                            className="primary-btn small"
                                            onClick={() => handleAssignFinalGrade(student.studentId)}
                                        >
                                            Final Grade
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )
            )}
        </div>
    );

    const renderAssignmentsTab = () => (
        <div className="assignments-container">
            {/* Create Assignment Toggle */}
            {/* Create Assignment Toggle */}
            <div className="actions-bar">
                <button
                    className="primary-btn"
                    onClick={() => {
                        if (!showCreateAssign) {
                            // Reset form when opening creation mode
                            setNewAssignData({ title: '', description: '', deadline: '', maxGrade: 100, file: null, customAttributes: [], gradingItemId: '' });
                            setEditingAssignmentId(null);
                        }
                        setShowCreateAssign(!showCreateAssign);
                    }}
                >
                    {showCreateAssign ? 'Cancel' : '+ Create Assignment'}
                </button>
            </div>

            {/* Create/Edit Form */}
            {showCreateAssign && (
                <form className="create-form card" onSubmit={handleCreateAssignment}>
                    <h4>{editingAssignmentId ? 'Edit Assignment' : 'New Assignment'}</h4>
                    <div className="form-group">
                        <label>Title</label>
                        <input
                            type="text" required
                            value={newAssignData.title}
                            onChange={e => setNewAssignData({ ...newAssignData, title: e.target.value })}
                        />
                    </div>
                    <div className="form-group">
                        <label>Description</label>
                        <textarea
                            required
                            value={newAssignData.description}
                            onChange={e => setNewAssignData({ ...newAssignData, description: e.target.value })}
                        />
                    </div>

                    {/* Grading Bucket Selection */}
                    <div className="form-group">
                        <label>Grading Bucket (Optional)</label>
                        <select
                            value={newAssignData.gradingItemId}
                            onChange={e => setNewAssignData({ ...newAssignData, gradingItemId: e.target.value })}
                        >
                            <option value="">-- No Grading Bucket --</option>
                            {gradingItems.map(item => (
                                <option key={item.id} value={item.id}>
                                    {item.categoryName} ({item.weightPercentage}%)
                                </option>
                            ))}
                        </select>
                    </div>

                    <div className="form-row" style={{ display: 'flex', gap: '1rem' }}>
                        <div className="form-group" style={{ flex: 1 }}>
                            <label>Deadline</label>
                            <input
                                type="datetime-local" required
                                value={newAssignData.deadline}
                                onChange={e => setNewAssignData({ ...newAssignData, deadline: e.target.value })}
                            />
                        </div>
                        <div className="form-group" style={{ flex: 1 }}>
                            <label>Max Grade</label>
                            <input
                                type="number" required
                                value={newAssignData.maxGrade}
                                onChange={e => setNewAssignData({ ...newAssignData, maxGrade: e.target.value })}
                            />
                        </div>
                    </div>

                    {/* Custom Attributes Section */}
                    <div className="form-group">
                        <label>Custom Attributes (e.g. Language, Bonus)</label>
                        {newAssignData.customAttributes.map((attr, index) => (
                            <div key={index} style={{ display: 'flex', gap: '10px', marginBottom: '8px' }}>
                                <input
                                    type="text"
                                    placeholder="Name (e.g. Language)"
                                    value={attr.key}
                                    onChange={(e) => updateAttribute(index, 'key', e.target.value)}
                                    style={{ flex: 1 }}
                                />
                                <input
                                    type="text"
                                    placeholder="Value (e.g. Java)"
                                    value={attr.value}
                                    onChange={(e) => updateAttribute(index, 'value', e.target.value)}
                                    style={{ flex: 1 }}
                                />
                                <button type="button" className="ghost-btn small" onClick={() => removeAttribute(index)}>X</button>
                            </div>
                        ))}
                        <button type="button" className="secondary-btn small" onClick={addAttribute}>+ Add Attribute</button>
                    </div>

                    {!editingAssignmentId && (
                        <div className="form-group">
                            <label>Attachment (PDF)</label>
                            <input
                                type="file"
                                accept=".pdf"
                                onChange={e => setNewAssignData({ ...newAssignData, file: e.target.files[0] })}
                            />
                        </div>
                    )}
                    <button type="submit" className="primary-btn small">
                        {editingAssignmentId ? 'Update Assignment' : 'Publish Assignment'}
                    </button>
                </form>
            )}

            {/* Assignments List */}
            {/* Assignments List */}
            <div className="assignments-list">
                {loadingAssignments ? (
                    <div>Loading assignments...</div>
                ) : (!Array.isArray(assignments) || assignments.length === 0) ? (
                    <div className="empty-state">No assignments created yet.</div>
                ) : (
                    assignments.map(assign => (
                        <div key={assign.id} className="assignment-item card">
                            <div className="assign-header">
                                <div>
                                    <div className="assign-title">{assign.data?.Title || "Untitled"}</div>
                                    <div className="assign-date">
                                        Due: {assign.data?.Due_Date ? new Date(assign.data.Due_Date).toLocaleDateString() : 'No Date'}
                                        {assign.data?.Max_Grade ? ` | Max: ${assign.data.Max_Grade}` : ''}
                                        Due: {assign.data?.Due_Date ? new Date(assign.data.Due_Date).toLocaleDateString() : 'No Date'}
                                        {assign.data?.Grading_Category ? ` | ${assign.data.Grading_Category}` : ''}
                                    </div>
                                    {assign.data?.Is_Visible === false && <span className="status-badge">Hidden</span>}
                                </div>
                                <div className="assign-actions">
                                    <button
                                        className="secondary-btn small"
                                        onClick={(e) => { e.stopPropagation(); handleViewSubmissions(assign); }}
                                    >
                                        Submissions
                                    </button>
                                    <button
                                        className="ghost-btn small"
                                        onClick={(e) => { e.stopPropagation(); handleEditClick(assign); }}
                                    >
                                        Edit
                                    </button>
                                    <button
                                        className="ghost-btn small delete-btn"
                                        onClick={(e) => { e.stopPropagation(); handleDeleteAssignment(assign.id); }}
                                    >
                                        Delete
                                    </button>
                                    <span className="badge">Active</span>
                                </div>
                            </div>
                            <p className="assign-desc">{assign.data?.Description || ''}</p>
                            {assign.data?.Attachment_Url && (
                                <a
                                    href={`http://localhost:8081${assign.data.Attachment_Url}`}
                                    target="_blank"
                                    rel="noreferrer"
                                    style={{ fontSize: '12px', color: '#6366f1', textDecoration: 'none' }}
                                >
                                    View Attachment
                                </a>
                            )}

                            {/* Display Custom Attributes */}
                            <div className="custom-tags" style={{ marginTop: '8px', display: 'flex', gap: '5px', flexWrap: 'wrap' }}>
                                {Object.keys(assign.data || {}).map(key => {
                                    if (!['Title', 'Description', 'Due_Date', 'Max_Grade', 'Attachment_Url', 'Is_Visible', 'Course_Id', 'Professor_Id', 'id'].includes(key)) {
                                        return (
                                            <span key={key} style={{ background: '#e0e7ff', color: '#4338ca', padding: '2px 8px', borderRadius: '12px', fontSize: '11px' }}>
                                                {key}: {assign.data[key]}
                                            </span>
                                        )
                                    }
                                    return null;
                                })}
                            </div>

                            <div className="assign-actions">
                                <hr />
                                <h5>Grade Submissions</h5>
                                <div className="mini-student-list">
                                    {/* Safe check for students before mapping inside assignment card */}
                                    {Array.isArray(students) && students.length > 0 ? (
                                        students.map(student => (
                                            <div key={student.studentId} className="student-row">
                                                <span>{student.name} ({student.studentId})</span>
                                                <button
                                                    className="ghost-btn"
                                                    onClick={() => handleGradeAssignment(assign.id, student.studentId)}
                                                >
                                                    Grade
                                                </button>
                                            </div>
                                        ))
                                    ) : (
                                        <div style={{ padding: '10px', fontSize: '13px', color: '#888' }}>
                                            No students available to grade.
                                        </div>
                                    )}
                                </div>
                            </div>
                        </div>
                    ))
                )}
            </div>
        </div>
    );

    const renderGradingItemsTab = () => (
        <div className="grading-container">
            <div className="actions-bar">
                <button
                    className="primary-btn"
                    onClick={() => setShowCreateGradingItem(!showCreateGradingItem)}
                >
                    {showCreateGradingItem ? 'Cancel' : '+ New Grading Bucket'}
                </button>
            </div>

            {/* Create Form */}
            {showCreateGradingItem && (
                <form className="create-form card" onSubmit={handleCreateGradingItem}>
                    <h4>New Grading Bucket</h4>
                    <div className="form-group">
                        <label>Category Name (e.g. Midterm, Labs)</label>
                        <input
                            type="text" required
                            value={newGradingItem.categoryName}
                            onChange={e => setNewGradingItem({ ...newGradingItem, categoryName: e.target.value })}
                        />
                    </div>
                    <div className="form-group">
                        <label>Weight (%) (e.g. 20)</label>
                        <input
                            type="number" required
                            min="1" max="100"
                            value={newGradingItem.weight}
                            onChange={e => setNewGradingItem({ ...newGradingItem, weight: e.target.value })}
                        />
                    </div>
                    <button type="submit" className="primary-btn small">Create Bucket</button>
                </form>
            )}

            {/* List */}
            <div className="table-card" style={{ marginTop: '1rem' }}>
                <div className="table-header">
                    <h3>Grading Scheme</h3>
                    <span className="badge">
                        Total Weight: {gradingItems.reduce((sum, item) => sum + (item.weightPercentage || 0), 0)}%
                    </span>
                </div>

                {loadingGradingItems ? (
                    <div className="p-4">Loading grading items...</div>
                ) : (!Array.isArray(gradingItems) || gradingItems.length === 0) ? (
                    <div className="p-4">No grading buckets defined for this course.</div>
                ) : (
                    <table className="prof-table">
                        <thead>
                            <tr>
                                <th>Category</th>
                                <th>Weight (%)</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {gradingItems.map(item => (
                                <tr key={item.id}>
                                    <td>{item.categoryName}</td>
                                    <td>{item.weightPercentage}%</td>
                                    <td>
                                        <button className="ghost-btn small">Edit</button> {/* Placeholder */}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}
            </div>
        </div>
    );

    const renderRequestsTab = () => (
        <div className="requests-container">
            <div className="page-header">
                <h2>Staff Requests</h2>
                <button
                    className="primary-btn"
                    onClick={() => setShowCreateRequest(!showCreateRequest)}
                >
                    {showCreateRequest ? 'Cancel' : '+ New Request'}
                </button>
            </div>

            {/* Request Form */}
            {showCreateRequest && (
                <form className="create-form card" onSubmit={handleCreateRequest}>
                    <h4>New Staff Request</h4>
                    <div className="form-group">
                        <label>Request Type</label>
                        <select
                            value={newRequestData.type}
                            onChange={e => setNewRequestData({ ...newRequestData, type: e.target.value })}
                        >
                            <option value="Equipment">Equipment</option>
                            <option value="Maintenance">Maintenance</option>
                            <option value="Leave">Leave</option>
                            <option value="Course Material">Course Material</option>
                        </select>
                    </div>
                    <div className="form-group">
                        <label>Description</label>
                        <textarea
                            required
                            value={newRequestData.description}
                            onChange={e => setNewRequestData({ ...newRequestData, description: e.target.value })}
                        />
                    </div>
                    <div className="form-group">
                        <label>Priority</label>
                        <select
                            value={newRequestData.priority}
                            onChange={e => setNewRequestData({ ...newRequestData, priority: e.target.value })}
                        >
                            <option value="High">High</option>
                            <option value="Medium">Medium</option>
                            <option value="Low">Low</option>
                        </select>
                    </div>
                    <button type="submit" className="primary-btn small">Submit Request</button>
                </form>
            )}

            {/* Request List */}
            {loadingRequests ? (
                <div>Loading requests...</div>
            ) : (!Array.isArray(requests) || requests.length === 0) ? (
                <div className="empty-state">No requests submitted.</div>
            ) : (
                <table className="prof-table card">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Type</th>
                            <th>Status</th>
                            <th>Description</th>
                            <th>Date</th>
                        </tr>
                    </thead>
                    <tbody>
                        {requests.map(req => (
                            <tr key={req.id}>
                                <td>{req.id}</td>
                                <td>{req.data?.Request_Type || '-'}</td>
                                <td>
                                    <span className={`status-badge ${req.data?.Status?.toLowerCase()}`}>
                                        {req.data?.Status || 'Pending'}
                                    </span>
                                </td>
                                <td>{req.data?.Description || ''}</td>
                                <td>{req.data?.Date_Submitted || ''}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}
        </div>
    );

    const renderHallsTab = () => (
        <div className="halls-container">
            <div className="page-header">
                <h2>Hall Booking</h2>
                <p>Book a hall for lectures or events.</p>
            </div>

            <div className="card booking-card" style={{ maxWidth: '600px' }}>
                <form onSubmit={handleBookHall}>
                    <div className="form-group">
                        <label>Select Hall</label>
                        {loadingHalls ? (
                            <div>Loading Halls...</div>
                        ) : (
                            <select
                                required
                                value={bookingData.hallName}
                                onChange={e => setBookingData({ ...bookingData, hallName: e.target.value })}
                            >
                                <option value="">-- Choose a Hall --</option>
                                {halls.map(hall => {
                                    // Extract Location (Building)
                                    const location = hall.values?.find(v => v.attribute.attributeName === 'Location')?.valString || 'Unknown Building';
                                    return (
                                        <option key={hall.id} value={hall.hallName}>
                                            {hall.hallName} - {location}
                                        </option>
                                    );
                                })}
                            </select>
                        )}
                    </div>

                    <div className="form-row" style={{ display: 'flex', gap: '1rem' }}>
                        <div className="form-group" style={{ flex: 1 }}>
                            <label>Day of Week</label>
                            <select
                                value={bookingData.dayOfWeek}
                                onChange={e => setBookingData({ ...bookingData, dayOfWeek: e.target.value })}
                            >
                                <option value="Monday">Monday</option>
                                <option value="Tuesday">Tuesday</option>
                                <option value="Wednesday">Wednesday</option>
                                <option value="Thursday">Thursday</option>
                                <option value="Friday">Friday</option>
                                <option value="Saturday">Saturday</option>
                                <option value="Sunday">Sunday</option>
                            </select>
                        </div>
                    </div>



                    <div className="form-row" style={{ display: 'flex', gap: '1rem' }}>
                        <div className="form-group" style={{ flex: 1 }}>
                            <label>From</label>
                            <select
                                value={bookingData.startTime}
                                onChange={e => setBookingData({ ...bookingData, startTime: e.target.value })}
                            >
                                {/* Generate slots from 08:00 to 20:00 */}
                                {Array.from({ length: 13 }, (_, i) => i + 8).map(h => {
                                    const time = `${h.toString().padStart(2, '0')}:00`;
                                    return <option key={time} value={time}>{time}</option>;
                                })}
                            </select>
                        </div>
                        <div className="form-group" style={{ flex: 1 }}>
                            <label>To</label>
                            <select
                                value={bookingData.endTime}
                                onChange={e => setBookingData({ ...bookingData, endTime: e.target.value })}
                            >
                                {Array.from({ length: 13 }, (_, i) => i + 9).map(h => {
                                    const time = `${h.toString().padStart(2, '0')}:00`;
                                    return <option key={time} value={time}>{time}</option>;
                                })}
                            </select>
                        </div>
                    </div>

                    <button type="submit" className="primary-btn full-width">Check Availability & Book</button>
                </form>
            </div>
        </div>
    );

    return (
        <div className="prof-dashboard-theme">
            <div className="shell">
                {/* SIDEBAR */}
                <aside className="sidebar">
                    <div className="sidebar-header">
                        {/* Removed Logo as requested */}
                        <span className="logo-text">University Portal</span>
                    </div>
                    <nav className="side-nav">
                        <button
                            className={`nav-item ${activeTab === 'courses' ? 'active' : ''}`}
                            onClick={() => { setActiveTab('courses'); setSelectedCourse(null); }}
                        >
                            {Icon.home16} <span>My Courses</span>
                        </button>

                        <button
                            className={`nav-item ${activeTab === 'payment' ? 'active' : ''}`}
                            onClick={() => { setActiveTab('payment'); setSelectedCourse(null); }}
                        >
                            💰 <span>Payment</span>
                        </button>
                        <button
                            className={`nav-item ${activeTab === 'requests' ? 'active' : ''}`}
                            onClick={() => { setActiveTab('requests'); setSelectedCourse(null); }}
                        >
                            {/* Reusing existing icon for now */}
                            {Icon.menu16} <span>Requests</span>
                        </button>
                        <button
                            className={`nav-item ${activeTab === 'halls' ? 'active' : ''}`}
                            onClick={() => { setActiveTab('halls'); setSelectedCourse(null); }}
                        >
                            {/* Reusing existing icon for now */}
                            {Icon.course} <span>Halls</span>
                        </button>
                    </nav>
                    <div className="sidebar-footer">
                        <button className="nav-item">Logout</button>
                    </div>
                </aside>

                {/* MAIN CONTENT */}
                <main className="main" role="main">
                    {/* Header */}
                    <header className="topbar">
                        <div className="topbar-left">
                            {/* Breadcrumbs or Title */}
                            <h3>{activeTab.charAt(0).toUpperCase() + activeTab.slice(1)}</h3>
                        </div>
                        <div className="topbar-right">
                            <div className="sidebar-user">
                                <span className="user-name">Prof. {professorId}</span>
                            </div>
                        </div>
                    </header>

                    <div className="content-body" style={{ marginTop: '24px' }}>
                        {activeTab === 'requests' && renderRequestsTab()}

                        {activeTab === 'payment' && renderPaymentTab()}
                        {activeTab === 'halls' && renderHallsTab()}

                        {activeTab === 'courses' && (
                            selectedCourse ? (
                                <>
                                    {/* Course Header */}
                                    <div className="page-header">
                                        <div className="page-title">
                                            <button className="icon-btn" onClick={handleBackToDashboard}>{Icon.back}</button>
                                            <div>
                                                <h2>{selectedCourse}</h2>
                                                <p className="page-sub">Manage Course Content</p>
                                            </div>
                                        </div>
                                    </div>

                                    {/* Tabs */}
                                    <div className="detail-tabs">
                                        <button
                                            className={`tab-btn ${courseSubTab === 'students' ? 'active' : ''}`}
                                            onClick={() => setCourseSubTab('students')}
                                        >
                                            Students
                                        </button>
                                        <button
                                            className={`tab-btn ${courseSubTab === 'assignments' ? 'active' : ''}`}
                                            onClick={() => setCourseSubTab('assignments')}
                                        >
                                            Assignments
                                        </button>
                                        <button
                                            className={`tab-btn ${courseSubTab === 'grading' ? 'active' : ''}`}
                                            onClick={() => setCourseSubTab('grading')}
                                        >
                                            Grading Buckets
                                        </button>
                                    </div>

                                    {/* SUB-TAB CONTENT */}
                                    <div className="detail-content">
                                        {courseSubTab === 'students' && renderStudentsTab()}
                                        {courseSubTab === 'assignments' && renderAssignmentsTab()}
                                        {courseSubTab === 'grading' && renderGradingItemsTab()}
                                    </div>
                                </>
                            ) : (
                                <>
                                    <div className="page-header">
                                        <div className="page-title">
                                            <span className="page-title-ico">{Icon.home16}</span>
                                            <div>
                                                <h2>My Courses</h2>
                                                <p className="page-sub">Select a course to manage.</p>
                                            </div>
                                        </div>
                                    </div>
                                    {renderCourseCards()}
                                </>
                            )
                        )}
                        {/* SUBMISSIONS MODAL */}
                        {showSubmissionsModal && viewingAssignment && (
                            <div className="modal-overlay">
                                <div className="modal-content" style={{ maxWidth: '800px', width: '90%' }}>
                                    <div className="modal-header">
                                        <h3>Submissions: {viewingAssignment.data?.Title}</h3>
                                        <button className="close-btn" onClick={() => setShowSubmissionsModal(false)}>×</button>
                                    </div>
                                    <div className="modal-body">
                                        {loadingSubmissions ? (
                                            <div>Loading submissions...</div>
                                        ) : submissions.length === 0 ? (
                                            <div>No submissions found.</div>
                                        ) : (
                                            <table className="prof-table">
                                                <thead>
                                                    <tr>
                                                        <th>Student ID</th>
                                                        <th>Date</th>
                                                        <th>File</th>
                                                        <th>Grade</th>
                                                        {/* <th>Feedback</th> */}
                                                        <th>Action</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    {submissions.map(sub => {
                                                        const grade = sub.values?.Grade || '-';
                                                        const feedback = sub.values?.Feedback || '';
                                                        return (
                                                            <tr key={sub.id}>
                                                                <td>{sub.studentId}</td>
                                                                <td>{sub.submittedAt ? new Date(sub.submittedAt).toLocaleString() : '-'}</td>
                                                                <td>
                                                                    {sub.values?.Attachment_Url ? (
                                                                        <a
                                                                            href={`http://localhost:8081${sub.values.Attachment_Url}`}
                                                                            target="_blank"
                                                                            rel="noreferrer"
                                                                            style={{ color: 'var(--primary-color)', textDecoration: 'underline' }}
                                                                        >
                                                                            View PDF
                                                                        </a>
                                                                    ) : 'No File'}
                                                                </td>
                                                                <td>
                                                                    <span className={`status-badge ${grade !== '-' ? 'completed' : 'pending'}`}>
                                                                        {grade !== '-' ? `${grade}/100` : 'Ungraded'}
                                                                    </span>
                                                                </td>
                                                                {/* <td>{feedback || '-'}</td> */}
                                                                <td>
                                                                    <button
                                                                        className="primary-btn small"
                                                                        onClick={() => handleGradeSubmission(sub.id, grade === '-' ? '' : grade, feedback)}
                                                                    >
                                                                        Grade
                                                                    </button>
                                                                </td>
                                                            </tr>
                                                        );
                                                    })}
                                                </tbody>
                                            </table>
                                        )}
                                    </div>
                                </div>
                            </div>
                        )}
                    </div>
                </main>
            </div >
        </div >
    );
};

export default ProfessorDashboard;