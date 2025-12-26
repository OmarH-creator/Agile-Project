import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { jwtDecode } from 'jwt-decode';
import { getEnrolledCourses, Icon, getStudent } from '../../Admin_View/Admin-Student-Api';
import './StudentCourses.css';
import umsLogo from '../../../assets/UMS Logo.png';

const StudentCourses = () => {
    const [courses, setCourses] = useState([]);
    const [student, setStudent] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        const token = localStorage.getItem("token");
        if (!token) {
            navigate("/");
            return;
        }

        const fetchStudentAndCourses = async () => {
            try {
                const decoded = jwtDecode(token);
                const email = decoded.sub;

                // Fetch student details to get studentId
                const studentData = await getStudent(email);
                setStudent(studentData);

                // Fetch enrolled courses using studentId
                const enrolledCourses = await getEnrolledCourses(studentData.studentId);
                setCourses(enrolledCourses);
            } catch (err) {
                console.error("Failed to load courses:", err);
                setError(err.message || "Failed to load courses");
            } finally {
                setLoading(false);
            }
        };

        fetchStudentAndCourses();
    }, [navigate]);

    if (loading) {
        return (
            <div className="shell student-theme">
                <div className="loading-container">
                    <div className="spinner">{Icon.spinner}</div>
                    <p>Loading your courses...</p>
                </div>
            </div>
        );
    }

    return (
        <div className="shell student-theme">
            {/* Top Bar */}
            <header className="topbar" role="banner">
                <div className="topbar-left">
                    <div className="brand-mini">
                        <div className="brand-logo-shell">
                            <img src={umsLogo} alt="UMS logo" className="mini-logo" />
                        </div>
                        <span className="brand-text brand-title">Student Portal</span>
                    </div>
                </div>
                <div className="topbar-right">
                    <div className="sidebar-user">
                        <div className="avatar">
                            <span className="avatar-ico">{Icon.user16}</span>
                        </div>
                        <div className="user-meta">
                            <div className="user-name">
                                {student ? `Hello, ${student.name}` : "Welcome Student"}
                            </div>
                        </div>
                    </div>
                </div>
            </header>

            {/* Main Content */}
            <main className="main">
                <div className="page-header">
                    <div className="page-title">
                        <span className="page-title-ico">{Icon.analytics}</span>
                        <div>
                            <h2>My Courses</h2>
                            <p className="page-sub">Courses you are currently enrolled in for this semester</p>
                        </div>
                    </div>
                </div>

                {error && (
                    <div className="error-banner">
                        <strong>Error:</strong> {error}
                    </div>
                )}

                {!loading && !error && courses.length === 0 && (
                    <div className="empty-state">
                        <div className="empty-icon">{Icon.requests}</div>
                        <h3>No Enrolled Courses</h3>
                        <p>You haven't registered for any courses yet.</p>
                        <button className="primary-btn" onClick={() => navigate("/student/registration")}>
                            Go to Registration
                        </button>
                    </div>
                )}

                {!loading && !error && courses.length > 0 && (
                    <div className="courses-grid">
                        {courses.map((course) => (
                            <div key={course.courseCode} className="course-card">
                                <div className="course-header">
                                    <span className="course-code">{course.courseCode}</span>
                                    <span className="course-credits">{course.creditHours} Credits</span>
                                </div>
                                <h3 className="course-name">{course.courseName}</h3>
                                <div className="course-instructor">
                                    <span className="prof-label">Instructor: </span>
                                    <span className="prof-email">{course.professorEmail}</span>
                                </div>
                                <div className="course-footer">
                                    <span className="course-semester">Semester: {course.semester}</span>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </main>
        </div>
    );
};

export default StudentCourses;
