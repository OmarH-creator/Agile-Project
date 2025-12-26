import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './StudentProfile.css';
import umsLogo from '../../../assets/UMS Logo.png';
import { getStudent, Icon } from '../../Admin_View/Admin-Student-Api';
import { jwtDecode } from "jwt-decode";

const StudentProfile = () => {
    const [student, setStudent] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        const token = localStorage.getItem("token");
        if (token) {
            try {
                const decoded = jwtDecode(token);
                const email = decoded.sub;
                console.log("Student email from token:", email);

                // Fetch student details
                async function fetchData() {
                    try {
                        const response = await getStudent(email);
                        setStudent(response);
                        setLoading(false);
                    } catch (error) {
                        console.error("Failed to fetch student info", error);
                        setError("Failed to load profile information");
                        setLoading(false);
                    }
                }
                fetchData();
            } catch (error) {
                console.error("Invalid token", error);
                setError("Authentication error");
                setLoading(false);
            }
        } else {
            setError("No authentication token found");
            setLoading(false);
        }
    }, []);


    if (loading) {
        return (
            <div className="shell student-theme">
                <header className="topbar" role="banner">
                    <div className="topbar-left">
                        <div className="brand-mini">
                            <div className="brand-logo-shell">
                                <img src={String(umsLogo)} alt="UMS logo" className="mini-logo" />
                            </div>
                            <span className="brand-text brand-title">Student Portal</span>
                        </div>
                    </div>
                </header>
                <main className="main" role="main">
                    <div className="loading-container">
                        <p>Loading profile...</p>
                    </div>
                </main>
            </div>
        );
    }

    if (error) {
        return (
            <div className="shell student-theme">
                <header className="topbar" role="banner">
                    <div className="topbar-left">
                        <div className="brand-mini">
                            <div className="brand-logo-shell">
                                <img src={String(umsLogo)} alt="UMS logo" className="mini-logo" />
                            </div>
                            <span className="brand-text brand-title">Student Portal</span>
                        </div>
                    </div>
                </header>
                <main className="main" role="main">
                    <div className="error-container">
                        <p>{error}</p>
                    </div>
                </main>
            </div>
        );
    }

    return (
        <div className="shell student-theme">
            {/* --- TOP BAR --- */}
            <header className="topbar" role="banner">
                <div className="topbar-left">
                    <div className="brand-mini">
                        <div className="brand-logo-shell">
                            <img src={String(umsLogo)} alt="UMS logo" className="mini-logo" />
                        </div>
                        <span className="brand-text brand-title">Student Portal</span>
                    </div>
                </div>
                <div className="topbar-right">
                    <div className="sidebar-user">
                        <div className="avatar" aria-hidden="true">
                            <span className="avatar-ico">{Icon.user16}</span>
                        </div>
                        <div className="user-meta">
                            <div className="user-name">
                                {student?.name || "Student"}
                            </div>
                        </div>
                    </div>
                </div>
            </header>

            {/* --- MAIN CONTENT --- */}
            <main className="main" role="main">
                <div className="page-header">
                    <div className="page-title">
                        <span className="page-title-ico">{Icon.user16}</span>
                        <div>
                            <h2>My Profile</h2>
                            <p className="page-sub">View your personal information and academic details</p>
                        </div>
                    </div>
                </div>

                <div className="profile-content">
                    {/* Personal Information Section */}
                    <div className="profile-section">
                        <h3 className="section-title">Personal Information</h3>
                        <div className="info-grid">
                            <div className="info-item">
                                <span className="info-label">Student ID</span>
                                <span className="info-value">{student?.studentId || 'N/A'}</span>
                            </div>
                            <div className="info-item">
                                <span className="info-label">Full Name</span>
                                <span className="info-value">{student?.name || 'N/A'}</span>
                            </div>
                            <div className="info-item">
                                <span className="info-label">Email</span>
                                <span className="info-value">{student?.email || 'N/A'}</span>
                            </div>
                            <div className="info-item">
                                <span className="info-label">Date of Birth</span>
                                <span className="info-value">
                                    {student?.dateOfBirth
                                        ? new Date(student.dateOfBirth).toLocaleDateString()
                                        : 'N/A'}
                                </span>
                            </div>
                            <div className="info-item">
                                <span className="info-label">Phone</span>
                                <span className="info-value">{student?.phone || 'N/A'}</span>
                            </div>
                            <div className="info-item">
                                <span className="info-label">Address</span>
                                <span className="info-value">{student?.address || 'N/A'}</span>
                            </div>
                            <div className="info-item">
                                <span className="info-label">Military Status</span>
                                <span className="info-value">{student?.militaryStatus || 'N/A'}</span>
                            </div>
                        </div>
                    </div>

                    {/* Academic Information Section */}
                    <div className="profile-section">
                        <h3 className="section-title">Academic Information</h3>
                        <div className="info-grid">
                            <div className="info-item">
                                <span className="info-label">Major</span>
                                <span className="info-value">{student?.major?.majorName || 'N/A'}</span>
                            </div>
                            <div className="info-item">
                                <span className="info-label">GPA</span>
                                <span className="info-value">{student?.gpa?.toFixed(2) || 'N/A'}</span>
                            </div>
                            <div className="info-item">
                                <span className="info-label">Completed Credits</span>
                                <span className="info-value">
                                    {student?.completedCourses?.reduce((total, course) => total + (course.credits || 0), 0) || 0}
                                </span>
                            </div>
                            <div className="info-item">
                                <span className="info-label">Current Courses</span>
                                <span className="info-value">{student?.currentCourses?.length || 0}</span>
                            </div>
                        </div>
                    </div>

                    {/* Academic History Section (Completed Courses) */}
                    {student?.completedCourses && student.completedCourses.length > 0 ? (
                        <div className="profile-section">
                            <h3 className="section-title">Academic History</h3>
                            <div className="table-responsive">
                                <table className="history-table">
                                    <thead>
                                        <tr>
                                            <th>Course Name</th>
                                            <th>Semester</th>
                                            <th>Credits</th>
                                            <th>Grade</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {student.completedCourses.map((record, index) => (
                                            <tr key={index}>
                                                <td>{record.courseName}</td>
                                                <td>{record.semester}</td>
                                                <td>{record.credits}</td>
                                                <td>{record.grade}</td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    ) : (
                        <div className="profile-section">
                            <h3 className="section-title">Academic History</h3>
                            <p className="no-records">No completed courses yet.</p>
                        </div>
                    )}
                </div>
            </main>
        </div>
    );
};

export default StudentProfile;
