import React, { useState, useEffect } from 'react';
import './Course_Reg.css';
import { Icon } from '../../Admin_View/Admin-Student-Api';
import umsLogo from '../../../assets/UMS Logo.png';

// Set your Student ID here
const CURRENT_STUDENT_ID = "22P0223";
const API_BASE_URL = "http://localhost:8081/api/student";

const Course_Reg = () => {
    // --- STATE MANAGEMENT ---
    const [student, setStudent] = useState(null);
    const [allCourses, setAllCourses] = useState([]);
    const [displayedCourses, setDisplayedCourses] = useState([]);
    const [searchTerm, setSearchTerm] = useState("");
    const [msg, setMsg] = useState("");
    const [loading, setLoading] = useState(true);

    // --- 1. FETCH DATA ON MOUNT ---
    useEffect(() => {
        fetchData();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    // --- 2. SEARCH BAR LOGIC (DEBOUNCED) ---
    useEffect(() => {
        const delayDebounceFn = setTimeout(() => {
            if (!searchTerm.trim()) {
                setDisplayedCourses(allCourses);
                return;
            }
            const lowerTerm = searchTerm.toLowerCase();
            const filtered = allCourses.filter(course =>
                course.name.toLowerCase().includes(lowerTerm) ||
                course.id.toLowerCase().includes(lowerTerm)
            );
            setDisplayedCourses(filtered);
        }, 500);

        return () => clearTimeout(delayDebounceFn);
    }, [searchTerm, allCourses]);

    // --- DATA FETCHING ---
    const fetchData = async () => {
        setLoading(true);
        try {
            const studentRes = await fetch(`${API_BASE_URL}/${CURRENT_STUDENT_ID}/profile`);
            if (!studentRes.ok) throw new Error("Failed to load student");
            const studentData = await studentRes.json();

            const coursesRes = await fetch(`${API_BASE_URL}/courses`);
            if (!coursesRes.ok) throw new Error("Failed to load courses");
            const coursesData = await coursesRes.json();

            const mappedCourses = coursesData.map(c => ({
                id: c.courseCode,
                name: c.courseName,
                credit: c.creditHours,
                majorId: c.majorId || "GenEd",
                prereq: c.prerequisite,
                enrolled: c.enrolled || 0,
                capacity: c.capacity || 60
            }));

            setStudent({
                ...studentData,
                majorId: studentData.major?.majorId || "N/A",
                registeredNow: studentData.currentCourses || [],
                completedCourses: studentData.completedCourses?.map(r => r.courseName) || []
            });
            setAllCourses(mappedCourses);

        } catch (error) {
            console.error("Error fetching data:", error);
            setMsg("Error loading data. Is the backend running?");
        } finally {
            setLoading(false);
        }
    };

    // --- LOGIC: DETERMINE ROW STATUS ---
    const getCourseStatus = (course) => {
        if (!student) return 'LOADING';
        if (student.registeredNow.includes(course.id)) return 'REGISTERED';
        if (student.completedCourses.includes(course.name)) return 'COMPLETED';
        if (course.enrolled >= course.capacity) return 'FULL';

        if (course.prereq) {
            const requiredCodes = course.prereq.split(',').map(s => s.trim());
            const missingPrereqs = requiredCodes.filter(reqCode => {
                const reqCourse = allCourses.find(c => c.id === reqCode);
                if (reqCourse) {
                    return !student.completedCourses.includes(reqCourse.name);
                }
                return true;
            });
            if (missingPrereqs.length > 0) return 'LOCKED_PREREQ';
        }
        return 'AVAILABLE';
    };

    // --- HANDLERS ---
    const handleRegister = async (courseId) => {
        setMsg("Registering...");
        try {
            const response = await fetch(`${API_BASE_URL}/${student.studentId}/register`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ courseCode: courseId })
            });
            const text = await response.text();
            if (response.ok) {
                setMsg(`Success: ${text}`);
                fetchData();
            } else {
                setMsg(`Failed: ${text}`);
            }
        } catch (error) {
            setMsg("Network Error");
        }
        setTimeout(() => setMsg(""), 3000);
    };

    const handleDrop = async (courseId) => {
        setMsg("Dropping...");
        try {
            const response = await fetch(`${API_BASE_URL}/${student.studentId}/drop`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ courseCode: courseId })
            });
            if (response.ok) {
                setMsg(`Dropped ${courseId}`);
                fetchData();
            } else {
                const text = await response.text();
                setMsg(`Error: ${text}`);
            }
        } catch (error) {
            setMsg("Network Error");
        }
        setTimeout(() => setMsg(""), 3000);
    };

    return (
        <div className="shell student-theme" style={{ height: '100vh', display: 'flex', flexDirection: 'column' }}>
            {/* --- TOP BAR (Fixed Height) --- */}
            <header className="topbar" style={{ flexShrink: 0 }}>
                <div className="topbar-left">
                    <div className="brand-mini">
                        <div className="brand-logo-shell">
                            <img src={String(umsLogo)} alt="UMS" className="mini-logo" />
                        </div>
                        <span className="brand-text">Course Registration</span>
                    </div>
                </div>
                <div className="topbar-right">
                    <div className="sidebar-user">
                        <div className="avatar">{Icon.user16}</div>
                        <div className="user-name">{student?.name} ({student?.majorId})</div>
                    </div>
                </div>
            </header>

            {/* --- MAIN CONTENT (Flex Grow + No Scroll on Body) --- */}
            <main className="main" style={{
                flex: 1,
                display: 'flex',
                flexDirection: 'column',
                overflow: 'hidden',
                paddingBottom: '0'
            }}>

                {/* Header Section (Fixed) */}
                <div className="page-header" style={{
                    marginBottom: '1rem',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    flexShrink: 0
                }}>
                    <div className="page-title">
                        <span className="page-title-ico">{Icon.requests}</span>
                        <div>
                            <h2>Available Courses</h2>
                            <p className="page-sub">Select courses for the upcoming semester</p>
                        </div>
                    </div>

                    <div style={{ position: 'relative', marginRight: '20px' }}>
                        <input
                            type="text"
                            placeholder="Search Code or Name..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            style={{
                                padding: '10px 15px',
                                borderRadius: '8px',
                                border: '1px solid #444', // Dark border
                                backgroundColor: '#222', // Dark background for input
                                color: '#fff', // White text
                                fontSize: '14px',
                                width: '250px',
                                outline: 'none'
                            }}
                        />
                    </div>
                </div>

                {/* Status Message */}
                {msg && <div className={`status-pill ${msg.includes("Failed") || msg.includes("Error") ? "status-error" : "status-success"}`} style={{marginBottom: '1rem', flexShrink: 0}}>{msg}</div>}

                {/* --- REGISTRATION TABLE CONTAINER (Scrollable) --- */}
                <div className="table-container" style={{
                    flex: 1,
                    overflowY: 'auto',
                    borderRadius: '8px 8px 0 0',
                    // REMOVED backgroundColor: '#fff'
                }}>
                    <table className="reg-table" style={{ width: '100%', borderCollapse: 'collapse' }}>
                        {/* NOTE: We need a background color for Sticky Headers so text doesn't overlap
                           when scrolling. I chose a dark color (#1a1f2c) to match your theme.
                           Adjust this Hex Code if it doesn't perfectly match your background!
                        */}
                        <thead style={{ position: 'sticky', top: 0, zIndex: 10, backgroundColor: '#1a1f2c' }}>
                        <tr>
                            <th style={{ position: 'sticky', top: 0, backgroundColor: '#1a1f2c', zIndex: 10 }}>Code</th>
                            <th style={{ position: 'sticky', top: 0, backgroundColor: '#1a1f2c', zIndex: 10 }}>Course Name</th>
                            <th style={{ position: 'sticky', top: 0, backgroundColor: '#1a1f2c', zIndex: 10 }}>Cred</th>
                            <th style={{ position: 'sticky', top: 0, backgroundColor: '#1a1f2c', zIndex: 10 }}>Major</th>
                            <th style={{ position: 'sticky', top: 0, backgroundColor: '#1a1f2c', zIndex: 10 }}>Prerequisite</th>
                            <th style={{ position: 'sticky', top: 0, backgroundColor: '#1a1f2c', zIndex: 10 }}>Status</th>
                            <th style={{ position: 'sticky', top: 0, backgroundColor: '#1a1f2c', zIndex: 10, textAlign: 'right' }}>Action</th>
                        </tr>
                        </thead>
                        <tbody>
                        {loading && <tr><td colSpan="7" style={{textAlign:'center', padding:'20px'}}>Loading courses...</td></tr>}

                        {!loading && displayedCourses.length === 0 && (
                            <tr><td colSpan="7" style={{textAlign:'center', padding:'20px'}}>No courses found matching "{searchTerm}"</td></tr>
                        )}

                        {displayedCourses.map(course => {
                            const status = getCourseStatus(course);

                            let rowClass = "";
                            if (status === 'REGISTERED') rowClass = "row-green";
                            if (status === 'COMPLETED') rowClass = "row-done";
                            if (status === 'LOCKED_PREREQ') rowClass = "row-locked";

                            return (
                                <tr key={course.id} className={rowClass}>
                                    <td className="font-bold">{course.id}</td>
                                    <td>{course.name}</td>
                                    <td>{course.credit}</td>
                                    <td>{course.majorId}</td>
                                    <td>{course.prereq || "-"}</td>

                                    <td>
                                        {status === 'REGISTERED' && <span className="tag tag-green">Selected</span>}
                                        {status === 'COMPLETED' && <span className="tag tag-blue">Done</span>}
                                        {status === 'FULL' && <span className="tag tag-red">Full</span>}
                                        {status === 'LOCKED_PREREQ' && <span className="tag tag-grey">Locked</span>}
                                        {status === 'AVAILABLE' && <span className="tag tag-blue-outline">Open</span>}
                                    </td>

                                    <td style={{textAlign: 'right'}}>
                                        {status === 'AVAILABLE' && (
                                            <button className="reg-btn btn-add" onClick={() => handleRegister(course.id)}>
                                                Add
                                            </button>
                                        )}
                                        {status === 'REGISTERED' && (
                                            <button className="reg-btn btn-drop" onClick={() => handleDrop(course.id)}>
                                                Drop
                                            </button>
                                        )}
                                        {status === 'LOCKED_PREREQ' && (
                                            <button
                                                className="reg-btn btn-disabled"
                                                disabled
                                                title={`Missing Prerequisite: ${course.prereq}`}
                                            >
                                                Locked
                                            </button>
                                        )}
                                        {(status === 'COMPLETED' || status === 'FULL') && status !== 'LOCKED_PREREQ' && (
                                            <button className="reg-btn btn-disabled" disabled>
                                                {status === 'FULL' ? 'Closed' : 'Done'}
                                            </button>
                                        )}
                                    </td>
                                </tr>
                            );
                        })}
                        </tbody>
                    </table>
                </div>
            </main>
        </div>
    );
};

export default Course_Reg;