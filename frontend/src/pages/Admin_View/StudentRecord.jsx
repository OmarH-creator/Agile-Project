import React from 'react';
import umsLogo from "../../assets/UMS Logo.png";
import { Icon } from './Admin-Student-Api';
import './StudentRecord.css'
// Helper for currency
const formatCurrency = (value) =>
    new Intl.NumberFormat('en-US', { style: 'currency', currency: 'EGP', minimumFractionDigits: 0 }).format(value ?? 0);

const StudentRecord = ({ student, onDelete, onEdit, onDropCourse }) => {

    // Safety check
    if (!student) return null;

    // Calculate total hours from the array since backend doesn't provide it
    const totalCredits = student.completedCourses?.reduce((acc, c) => acc + (c.credits || 0), 0) || 0;
    // Inside StudentRecord.jsx
    console.log("FULL STUDENT OBJECT:", student);





    return (
        <div className="detail-panel-content">
            {/* --- HEADER --- */}
            <article className="detail-card">
                <header className="detail-card-header">
                    <div className="detail-ident">
                        <div className="detail-logo-shell">
                            <img src={umsLogo} alt="Logo" />
                        </div>
                        <div>
                            <h2>{student.name}</h2>
                            {/* Matching Backend Keys: major.majorName & studentId */}
                            <p>{student.major?.majorName} {student.studentId}</p>
                            <p>{student.email}</p>
                        </div>
                    </div>
                    <div className="detail-actions">
                        <button type="button" className="ghost-btn" onClick={() => onEdit(student)}>
                            {Icon.edit16} <span>Edit</span>
                        </button>
                        <button type="button" className="ghost-btn danger" onClick={() => onDelete(student.studentId)}>
                            {Icon.trash16} <span>Delete</span>
                        </button>
                    </div>
                </header>

                <div className="kpi-grid">
                    <div className="kpi-card">
                        <span className="kpi-label">CGPA</span>
                        <strong>{student.gpa ? Number(student.gpa).toFixed(1) : '0.00'}</strong>
                    </div>
                    <div className="kpi-card">
                        <span className="kpi-label">Completed Hours</span>
                        <strong>{totalCredits}</strong>
                    </div>
                    <div className="kpi-card">
                        <span className="kpi-label">Status</span>
                        <strong>{student.militaryStatus || 'Active'}</strong>
                    </div>
                </div>

                <div className="meta-grid">
                    <div><span className="meta-label">Phone</span><span className="meta-value">{student.phone}</span></div>
                    <div><span className="meta-label">Address</span><span className="meta-value">{student.address}</span></div>
                    <div><span className="meta-label">Birthdate</span><span className="meta-value">{student.dateOfBirth}</span></div>
                </div>
            </article>

            {/* --- ACADEMIC HISTORY --- */}
            <article className="history-card">
                <header>
                    <h3>Academic History</h3>
                    <span>{student.completedCourses?.length || 0} courses</span>
                </header>
                <div className="history-table-wrapper">
                    <table className="history-table">
                        <thead>
                        <tr>
                            <th>Course</th>
                            <th>Credits</th>
                            <th>Grade</th>
                            <th>Semester</th>
                        </tr>
                        </thead>
                        <tbody>
                        {(student.completedCourses || []).map((record, idx) => (
                            <tr key={idx}>
                                <td>{record.courseName}</td>
                                <td>{record.credits}</td>
                                <td>{record.grade}</td>
                                <td>{record.semester}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            </article>

            {/* --- CURRENT REGISTRATIONS --- */}
            <article className="registration-card">
                <header><h3>Current Courses</h3></header>
                <div className="current-registrations">
                    <div className="registration-list">
                        {(student.currentCourses || []).map((courseString, idx) => (
                            <div className="registration-item" key={idx}>
                                <div><strong>{courseString}</strong></div>
                                <button
                                    type="button"
                                    className="ghost-btn danger"
                                    onClick={() => onDropCourse(student.studentId, courseString)}
                                >
                                    {Icon.trash16} <span>Drop</span>
                                </button>
                            </div>
                        ))}
                        {(!student.currentCourses || student.currentCourses.length === 0) && (
                            <div className="empty-state small">No active courses.</div>
                        )}
                    </div>
                </div>
            </article>
        </div>
    );
};

export default StudentRecord;