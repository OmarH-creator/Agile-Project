import React, { useState, useEffect } from 'react'; // Added useEffect
import { Link } from 'react-router-dom';
import StudentRecord from './StudentRecord'; // Importing the child component
import {
    getStudent,
    getAllStudents,
    getTranscript,
    downloadBlob,
    emptyStudent,
    sumCredits, deleteStudent, createStudent, buildStudentSnapshot
} from './Admin-Student-Api'; // Importing functions from the Admin-Student-Api file
import { Icon } from './Admin-Student-Api'; // Assuming you have an Icons file
import './StudentServices.css';
import umsLogo from "../../assets/UMS Logo.png";

const StudentServices = () => {
    // --- STATE ---
    const [students, setStudents] = useState([]);
    const [searchQuery, setSearchQuery] = useState('');
    const [selectedStudent, setSelectedStudent] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [formMode, setFormMode] = useState(null);
    const [formData, setFormData] = useState(emptyStudent);
    const [validationError, setValidationError] = useState('');
    const [selectedCode, setSelectedCode] = useState(null);
    const [filters, setFilters] = useState({ query: '', status: 'all', major: 'all' });
    const [pendingRegistration, setPendingRegistration] = useState([]);
    const [notesDraft, setNotesDraft] = useState('');

    // Pagination
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const pageSize = 10;

    const clearError = () => setError(null);

    // --- INITIAL LOAD ---
    useEffect(() => {
        // Load ALL students initially
        triggerSearch(0, '');
    }, []);



    // --- THE SEARCH FUNCTION ---
    const triggerSearch = async (page, query) => {
        setLoading(true);
        try {
            // The API returns ONLY matches if 'query' is not empty
            const data = await getAllStudents(page, pageSize, query);

            // This REPLACES the entire list.
            // Non-matching students effectively "disappear" from the UI.
            setStudents(data.content);
            setTotalPages(data.totalPages);

        } catch (err) {
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    // --- HANDLERS ---

    // 1. Typing just updates the text box state
    const handleInputChange = (e) => {
        setSearchQuery(e.target.value);
    };

    // 2. Pressing Enter triggers the filtering
    const handleKeyDown = (e) => {
        if (e.key === 'Enter') {
            setCurrentPage(0); // Always reset to page 1 for new search
            triggerSearch(0, searchQuery);
        }
    };

    // 3. Clicking Search Icon triggers the filtering
    const handleSearchClick = () => {
        setCurrentPage(0);
        triggerSearch(0, searchQuery);
    };

    // 4. Pagination (Keeps the current search active)
    const handlePageChange = (pageNum) => {
        setCurrentPage(pageNum);
        triggerSearch(pageNum, searchQuery);
    };


    useEffect(() => {
        if (students.length === 0) {
            setSelectedStudent(null);
        }
    }, [students]);

    return (
        <div className="shell">
            <header className="topbar" role="banner">
                <div className="topbar-left">
                    <button className="icon-btn" aria-label="Menu" title="Menu">
                        {Icon.menu16}
                    </button>
                    <div className="brand-mini">
                        <div className="brand-logo-shell">
                            <img src={String(umsLogo)} alt="UMS logo" className="mini-logo"/>
                        </div>
                        <span className="brand-text brand-title">
                            University Management - Admin
                        </span>
                    </div>
                </div>
                <div className="topbar-right">
                    <div className="sidebar-user">
                        <div className="avatar" aria-hidden="true">
                            <span className="avatar-ico">{Icon.user16}</span>
                        </div>
                        <div className="user-meta">
                            <div className="user-name">Admin User</div>
                        </div>
                    </div>
                </div>
            </header>

            <main className="main">
                <div className="page-header">
                    <h2>Student Services</h2>
                </div>

                <section className="dashboard-content">
                    <aside className="student-panel">
                        <header className="student-panel-header">
                            <div className="student-panel-top">
                                <h3>Students</h3>
                                <button className="primary-btn">{Icon.plus16} Add</button>
                            </div>

                            {/* SEARCH BAR */}
                            <div className="student-filters">
                                <div className="search-group">
                                    <input
                                        type="search"
                                        placeholder="Filter by ID (e.g. 202)"
                                        value={searchQuery}
                                        onChange={handleInputChange}
                                        onKeyDown={handleKeyDown}
                                        className="search-input"
                                    />
                                    <button
                                        className="search-btn"
                                        onClick={handleSearchClick}
                                    >
                                        {Icon.search16 || '🔍'}
                                    </button>
                                </div>
                            </div>
                        </header>

                        {/* LIST */}
                        <div className="student-list">
                            {loading ? (
                                <div style={{padding: '20px'}}>Loading...</div>
                            ) : students.length > 0 ? (
                                students.map(student => (
                                    <button
                                        key={student.studentId}
                                        className={`student-card ${selectedStudent?.studentId === student.studentId ? 'active' : ''}`}
                                        onClick={() => setSelectedStudent(student)}
                                    >
                                        <div className="student-card-main">
                                            <h4>{student.name}</h4>
                                            <span className="student-code">{student.studentId}</span>
                                        </div>
                                    </button>
                                ))
                            ) : (
                                <div style={{padding: '1rem', color: '#666'}}>
                                    No students start with "{searchQuery}"
                                </div>

                            )}
                        </div>

                        {/* PAGINATION */}
                        <div className="pagination-footer">
                            <button
                                disabled={currentPage === 0}
                                onClick={() => handlePageChange(currentPage - 1)}
                                className="page-btn"
                            >
                                &lt;
                            </button>
                            <span style={{fontSize: '0.8rem'}}>
                                {currentPage + 1} / {totalPages || 1}
                            </span>
                            <button
                                disabled={currentPage >= totalPages - 1}
                                onClick={() => handlePageChange(currentPage + 1)}
                                className="page-btn"
                            >
                                &gt;
                            </button>
                        </div>
                    </aside>

                    <section className="detail-panel">
                        {selectedStudent ? (
                            <StudentRecord student={selectedStudent}/>
                        ) : (
                            <div className="empty-state large">Select a student</div>
                        )}
                    </section>
                </section>
            </main>
        </div>
    );
};


export default StudentServices;