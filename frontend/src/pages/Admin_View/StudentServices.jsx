import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import StudentRecord from './StudentRecord'; // Importing the child component
import { getStudent } from './Admin-Student-Api';
import { Icon } from './Admin-Student-Api'; // Assuming you have an Icons file
import './StudentServices.css';
import umsLogo from "../../assets/UMS Logo.png";

const StudentServices = () => {
    // --- STATE MANAGEMENT ---
    const [students, setStudents] = useState([]);
    const [searchQuery, setSearchQuery] = useState('');
    const [selectedStudent, setSelectedStudent] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    // --- SEARCH LOGIC ---
    const handleSearch = async (e) => {
        if (e.key === 'Enter') {
            setLoading(true);
            setError(null);
            try {
                // 1. Fetch from API
                const result = await getStudent(searchQuery);

                // 2. Add to Sidebar List (prevent duplicates)
                setStudents(prev => {
                    // Check using studentId because that's what backend returns
                    if (prev.find(s => s.studentId === result.studentId)) return prev;
                    return [...prev, result];
                });

                // 3. Select the student immediately
                setSelectedStudent(result);
            } catch (err) {
                console.error(err);
                setError("Student not found");
            } finally {
                setLoading(false);
            }
        }
    };

    const handleSelectStudent = (student) => {
        setSelectedStudent(student);
        // If you need to fetch extra course data, do it here
    };

    return (
        <div className="shell">
            {/* --- TOPBAR --- */}
            <header className="topbar" role="banner">
                <div className="topbar-left">
                    <button className="icon-btn" aria-label="Menu">
                        {Icon.menu16}
                    </button>
                    <div className="brand-mini">
                        <div className="brand-logo-shell">
                            <img src={umsLogo} alt="UMS logo" className="mini-logo"/>
                        </div>
                        <span className="brand-text brand-title">University Management</span>
                    </div>
                </div>
                <div className="topbar-right">
                    <div className="sidebar-user">
                        <div className="avatar">
                            <span className="avatar-ico">{Icon.user16}</span>
                        </div>
                        <div className="user-meta">
                            <div className="user-name">Admin User</div>
                        </div>
                    </div>
                </div>
            </header>

            <main className="main">
                {/* --- PAGE HEADER --- */}
                <div className="page-header">
                    <div className="page-title">
                        <Link to="/admin" className="ghost-btn">
                            {Icon.arrowLeft || '<-'} Back
                        </Link>
                        <div>
                            <h2>Student Services</h2>
                            <p className="page-sub">Manage records and enrollment</p>
                        </div>
                    </div>
                </div>

                <section className="dashboard-content">

                    {/* --- LEFT PANEL: SEARCH & LIST --- */}
                    <aside className="student-panel">
                        <header className="student-panel-header">
                            <div className="student-panel-top">
                                <h3>Students</h3>
                                <button className="primary-btn">
                                    {Icon.plus16} Add
                                </button>
                            </div>
                            <div className="student-filters">
                                <input
                                    type="search"
                                    placeholder="Search ID (Enter)"
                                    value={searchQuery}
                                    onChange={(e) => setSearchQuery(e.target.value)}
                                    onKeyDown={handleSearch}
                                />
                                <div className="filter-row">
                                    <select>
                                        <option value="all">All Majors</option>
                                        <option value="CS">CS</option>
                                        <option value="ENG">Engineering</option>
                                    </select>
                                </div>
                            </div>
                        </header>

                        <div className="student-list">
                            {students.map(student => (
                                <button
                                    key={student.studentId}
                                    className={`student-card ${selectedStudent?.studentId === student.studentId ? 'active' : ''}`}
                                    onClick={() => handleSelectStudent(student)}
                                >
                                    <div className="student-card-main">
                                        <h4>{student.name}</h4>
                                        <span className="student-code">{student.studentId}</span>
                                    </div>
                                    <div className="student-card-meta">
                                        <span className="status-chip">{student.militaryStatus || 'Active'}</span>
                                    </div>
                                </button>
                            ))}
                        </div>
                    </aside>

                    {/* --- RIGHT PANEL: RECORD VIEW --- */}
                    <section className="detail-panel">
                        {loading && <div className="loading-spinner">Loading...</div>}

                        {error && <div className="error-message">{error}</div>}

                        {!loading && !selectedStudent && (
                            <div className="empty-state large">
                                <p>Search for a student to view details.</p>
                            </div>
                        )}

                        {!loading && selectedStudent && (
                            <StudentRecord
                                student={selectedStudent} // Passing the backend data
                                onDelete={() => console.log("Delete")}
                                onEdit={() => console.log("Edit")}
                                onDropCourse={(id, course) => console.log("Drop", id, course)}
                            />
                        )}
                    </section>

                </section>
            </main>
        </div>
    );
};

export default StudentServices;