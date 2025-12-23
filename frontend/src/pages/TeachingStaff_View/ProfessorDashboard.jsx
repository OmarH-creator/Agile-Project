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
    const [activeTab, setActiveTab] = useState('students'); // 'students' or 'assignments'

    // --- DATA STATE ---
    const [students, setStudents] = useState([]);
    const [assignments, setAssignments] = useState([]);

    // --- LOADING STATES ---
    const [loadingStudents, setLoadingStudents] = useState(false);
    const [loadingAssignments, setLoadingAssignments] = useState(false);

    // --- MODAL / FORM STATE ---
    const [showCreateAssign, setShowCreateAssign] = useState(false);
    const [newAssignData, setNewAssignData] = useState({ title: '', description: '', deadline: '' });

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

            fetchStudents();
            fetchAssignments();
            setActiveTab('students'); // Reset tab
            setShowCreateAssign(false);
        }
    }, [selectedCourse]);

    // --- HANDLERS ---

    const handleCourseClick = (courseName) => {
        setSelectedCourse(courseName);
    };

    const handleBackToDashboard = () => {
        setSelectedCourse(null);
        setStudents([]);
        setAssignments([]);
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

    // --- ACTION: Create Assignment ---
    const handleCreateAssignment = async (e) => {
        e.preventDefault();
        try {
            await ProfessorAPI.createAssignment({
                title: newAssignData.title,
                description: newAssignData.description,
                courseName: selectedCourse,
                professorId: professorId,
                deadline: newAssignData.deadline // Format: YYYY-MM-DDTHH:mm:ss
            });
            alert("Assignment Created!");
            setShowCreateAssign(false);
            setNewAssignData({ title: '', description: '', deadline: '' });

            // Refresh list
            const data = await ProfessorAPI.getAssignments(selectedCourse);
            setAssignments(Array.isArray(data) ? data : []);
        } catch (err) {
            alert("Failed to create assignment");
        }
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

    // --- RENDERERS ---

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
                            <th style={{textAlign: 'right'}}>Actions</th>
                        </tr>
                        </thead>
                        <tbody>
                        {students.map((student) => (
                            <tr key={student.studentId}>
                                <td className="id-cell">{student.studentId}</td>
                                <td className="name-cell">{student.name}</td>
                                <td>{student.email}</td>
                                <td style={{textAlign: 'right'}}>
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
            <div className="actions-bar">
                <button
                    className="primary-btn"
                    onClick={() => setShowCreateAssign(!showCreateAssign)}
                >
                    {showCreateAssign ? 'Cancel' : '+ Create Assignment'}
                </button>
            </div>

            {/* Create Form */}
            {showCreateAssign && (
                <form className="create-form card" onSubmit={handleCreateAssignment}>
                    <h4>New Assignment</h4>
                    <div className="form-group">
                        <label>Title</label>
                        <input
                            type="text" required
                            value={newAssignData.title}
                            onChange={e => setNewAssignData({...newAssignData, title: e.target.value})}
                        />
                    </div>
                    <div className="form-group">
                        <label>Description</label>
                        <textarea
                            required
                            value={newAssignData.description}
                            onChange={e => setNewAssignData({...newAssignData, description: e.target.value})}
                        />
                    </div>
                    <div className="form-group">
                        <label>Deadline</label>
                        <input
                            type="datetime-local" required
                            value={newAssignData.deadline}
                            onChange={e => setNewAssignData({...newAssignData, deadline: e.target.value})}
                        />
                    </div>
                    <button type="submit" className="primary-btn small">Publish Assignment</button>
                </form>
            )}

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
                                    <div className="assign-title">{assign.title}</div>
                                    <div className="assign-date">Due: {new Date(assign.deadline).toLocaleDateString()}</div>
                                </div>
                                <span className="badge">Active</span>
                            </div>
                            <p className="assign-desc">{assign.description}</p>

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
                                        <div style={{padding: '10px', fontSize: '13px', color: '#888'}}>
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

    return (
        <div className="shell">
            {/* SIDEBAR */}
            <aside className="sidebar">
                <nav className="side-nav">
                    <button
                        className={`nav-item ${!selectedCourse ? 'active' : ''}`}
                        onClick={handleBackToDashboard}
                        style={{width: '100%', textAlign: 'left'}}
                    >
                        {Icon.home16} <span>My Courses</span>
                    </button>
                    <button className="nav-item" style={{width: '100%', textAlign: 'left'}}>
                        {Icon.user16} <span>Profile</span>
                    </button>
                </nav>
            </aside>

            {/* MAIN CONTENT */}
            <main className="main" role="main">
                {/* Header */}
                <header className="topbar">
                    <div className="topbar-left">
                        <button className="icon-btn">{Icon.menu16}</button>
                        <div className="brand-mini">
                            <div className="brand-logo-shell">
                                <img src={String(umsLogo)} alt="UMS" className="mini-logo" />
                            </div>
                            <span className="brand-text">Faculty Portal</span>
                        </div>
                    </div>
                    <div className="topbar-right">
                        <div className="sidebar-user">
                            <span className="user-name">Prof. {professorId}</span>
                        </div>
                    </div>
                </header>

                <div className="content-body" style={{marginTop: '24px'}}>
                    {selectedCourse ? (
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
                            <div className="tabs">
                                <button
                                    className={`tab-btn ${activeTab === 'students' ? 'active' : ''}`}
                                    onClick={() => setActiveTab('students')}
                                >
                                    Students & Final Grades
                                </button>
                                <button
                                    className={`tab-btn ${activeTab === 'assignments' ? 'active' : ''}`}
                                    onClick={() => setActiveTab('assignments')}
                                >
                                    Assignments
                                </button>
                            </div>

                            {/* Tab Content */}
                            {activeTab === 'students' ? renderStudentsTab() : renderAssignmentsTab()}
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
                    )}
                </div>
            </main>
        </div>
    );
};

export default ProfessorDashboard;