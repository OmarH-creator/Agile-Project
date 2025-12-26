import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import '../StudentDashboard.css';
import './StudentAnnouncements.css';
import umsLogo from '../../../assets/UMS Logo.png';
import { getStudent, Icon } from '../../Admin_View/Admin-Student-Api';
import { jwtDecode } from "jwt-decode";

const API_BASE_URL = 'http://localhost:8081/api/admin';

const StudentAnnouncements = () => {
    const [studentName, setStudentName] = useState("");
    const [announcements, setAnnouncements] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        const token = localStorage.getItem("token");
        if (token) {
            try {
                const decoded = jwtDecode(token);
                const email = decoded.sub;

                async function fetchData() {
                    try {
                        const response = await getStudent(email);
                        const name = response.name;
                        setStudentName(name);
                    } catch (error) {
                        console.error("Failed to fetch student info", error);
                    }
                }
                fetchData();
            } catch (error) {
                console.error("Invalid token", error);
            }
        }

        fetchAnnouncements();
    }, []);

    const fetchAnnouncements = async () => {
        try {
            const token = localStorage.getItem("token");
            const response = await fetch(`${API_BASE_URL}/announcements`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                }
            });

            if (!response.ok) {
                throw new Error('Failed to fetch announcements');
            }

            const data = await response.json();
            setAnnouncements(data);
            setLoading(false);
        } catch (err) {
            console.error('Error fetching announcements:', err);
            setError('Failed to load announcements');
            setLoading(false);
        }
    };

    const formatTimestamp = (timestamp) => {
        const date = new Date(timestamp);
        return date.toLocaleString('en-US', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    return (
        <div className="shell student-theme">
            {/* --- TOP BAR --- */}
            <header className="topbar" role="banner">
                <div className="topbar-left">
                    <div className="brand-mini">
                        <div className="brand-logo-shell">
                            <img src={String(umsLogo)} alt="UMS logo" className="mini-logo" />
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
                        <span className="page-title-ico">{Icon.bell16}</span>
                        <div>
                            <h2>Announcements & Events</h2>
                            <p className="page-sub">
                                Stay updated with university announcements
                            </p>
                        </div>
                    </div>
                </div>

                {error && (
                    <div className="error-banner">
                        <strong>Error:</strong> {error}
                    </div>
                )}

                <div className="announcements-list">
                    {loading ? (
                        <div className="loading-state">
                            <p>Loading announcements...</p>
                        </div>
                    ) : announcements.length === 0 ? (
                        <div className="empty-state">
                            <p>No announcements at this time.</p>
                        </div>
                    ) : (
                        announcements.map((announcement) => (
                            <div key={announcement.id} className="announcement-card">
                                <div className="announcement-header">
                                    <h3>{announcement.title}</h3>
                                    <span className="announcement-date">
                                        {formatTimestamp(announcement.timestamp)}
                                    </span>
                                </div>
                                <div className="announcement-content">
                                    {announcement.content}
                                </div>
                            </div>
                        ))
                    )}
                </div>
            </main>
        </div>
    );
};

export default StudentAnnouncements;
