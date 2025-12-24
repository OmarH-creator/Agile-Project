import React, {useEffect, useState} from 'react';
import { Link } from 'react-router-dom';
// We reuse the Admin CSS because the layout is identical
import './StudentDashboard.css';
import umsLogo from '../../assets/UMS Logo.png';
// You likely need to create/export 'getStudent' in your API file
import { getStudent, Icon } from '../Admin_View/Admin-Student-Api';
import { jwtDecode } from "jwt-decode";

const StudentDashboard = () => {
    const [studentName, setStudentName] = useState("");

    useEffect(() => {
        const token = localStorage.getItem("token");
        if (token) {
            try {
                const decoded = jwtDecode(token);
                const email = decoded.sub; // or decoded.email depending on your JWT structure
                console.log("Student data from token:", decoded);

                // Fetch student details to get the display name
                async function fetchData() {
                    try {
                        // Assuming you have a similar function for students
                        const response = await getStudent(email);
                        const name =  response.name;
                        setStudentName(name); // response should be the name string
                    } catch (error) {
                        console.error("Failed to fetch student info", error);
                    }
                }
                fetchData();
            } catch (error) {
                console.error("Invalid token", error);
            }
        }
    }, []);

    // --- STUDENT TILES CONFIGURATION ---
    const tiles = [
        {
            title: 'My Courses',
            sub: 'View current enrollments and grades',
            icon: Icon.analytics, // You can swap this for a book icon if available
            accent: 'violet',
            path: '/student/courses'
        },
        {
            title: 'Course Registration',
            sub: 'Browse catalog and enroll in classes',
            icon: Icon.requests, // Represents "Actions/Requests"
            accent: 'sunrise',
            path: '/student/registration'
        },
        {
            title: 'LMS',
            sub: 'Access assignments and learning materials',
            icon: Icon.facilities, // Placeholder for "System/Platform"
            accent: 'aqua',
            path: '/student/lms' // or external link
        },
        {
            title: 'Schedule',
            sub: 'View your weekly timetable',
            icon: Icon.facilities, // Calendar icon would be best here
            accent: 'emerald',
            path: '/student/schedule'
        },
        {
            title: 'Profile',
            sub: 'Update personal info and settings',
            icon: Icon.user16, // Using the user icon
            accent: 'citrus',
            path: '/student/profile'
        }
    ];

    return (
        <div className="shell student-theme">
            {/* --- TOP BAR --- */}
            <header className="topbar" role="banner">
                <div className="topbar-left">
                    <div className="brand-mini">
                        <div className="brand-logo-shell">
                            <img src={String(umsLogo)} alt="UMS logo" className="mini-logo"/>
                        </div>
                        <span className="brand-text brand-title">
                            Student Portal
                        </span>
                    </div>
                </div>
                <div className="topbar-right">
                    <div className="sidebar-user">
                        <div className="avatar" aria-hidden="true">
                            <span className="avatar-ico">{Icon.user16}</span>
                        </div>
                        <div className="user-meta">
                            <div className="user-name">
                                {studentName ? `Hello, ${studentName}` : "Welcome Student"}
                            </div>
                        </div>
                    </div>
                </div>
            </header>

            {/* --- MAIN CONTENT --- */}
            <main className="main" role="main">
                <div className="page-header">
                    <div className="page-title">
                        <span className="page-title-ico">{Icon.home16}</span>
                        <div>
                            <h2>Student Dashboard</h2>
                            <p className="page-sub">
                                Access your academic tools and information
                            </p>
                        </div>
                    </div>
                </div>

                <div className="grid">
                    {tiles.map((tile) => (
                        <Link
                            to={tile.path}
                            key={tile.title}
                            className="card"
                            data-accent={tile.accent}
                        >
                            <div className="card-title">
                                <div>
                                    <div className="card-heading">{tile.title}</div>
                                    <p className="card-sub">{tile.sub}</p>
                                </div>
                                <div className="card-icon" aria-hidden="true" data-accent={tile.accent}>
                                    {tile.icon}
                                </div>
                            </div>
                        </Link>
                    ))}
                </div>
            </main>
        </div>
    );
};

export default StudentDashboard;