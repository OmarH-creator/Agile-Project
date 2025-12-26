import React, { useEffect, useState } from "react";
import { jwtDecode } from "jwt-decode";
import umsLogo from "../../assets/UMS Logo.png";
import {getChildrenByEmail, getChildrenCourseRecords, getProfessorsForCourses} from "./ParentApi";
import "./ParentDashboard.css";

export default function ParentDashboard() {
    const [children, setChildren] = useState([]);
    const [courseRecords, setCourseRecords] = useState({});
    const [loading, setLoading] = useState(true);
    const [activeTab, setActiveTab] = useState("children"); // "children" or "grades"
    const [selectedStudent, setSelectedStudent] = useState(null);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const token = localStorage.getItem("token");
                const decoded = jwtDecode(token);
                const email = decoded.sub || decoded.email;

                // Fetch children data
                const childrenData = await getChildrenByEmail(email);
                setChildren(childrenData);

                // Fetch course records
                const recordsData = await getChildrenCourseRecords(email);
                setCourseRecords(recordsData.courseRecords || {});

            } catch (err) {
                console.error("Error:", err);
            } finally {
                setLoading(false);
            }
        };
        fetchData();
    }, []);

    useEffect(() => {
        const fetchProfessorInfo = async () => {
            if (Object.keys(courseRecords).length > 0) {
                // Collect all unique course names
                const allCourses = new Set();
                Object.values(courseRecords).forEach(records => {
                    records.forEach(record => {
                        if (record.courseName) {
                            allCourses.add(record.courseName);
                        }
                    });
                });

                if (allCourses.size > 0) {
                    try {
                        // Bulk fetch all professors at once
                        const professorMap = await getProfessorsForCourses(Array.from(allCourses));

                        // Update course records with professor info
                        const updatedRecords = { ...courseRecords };
                        Object.keys(updatedRecords).forEach(studentId => {
                            updatedRecords[studentId] = updatedRecords[studentId].map(record => ({
                                ...record,
                                professorName: professorMap[record.courseName]?.professorName || "Not Assigned",
                                professorEmail: professorMap[record.courseName]?.professorEmail || ""
                            }));
                        });

                        setCourseRecords(updatedRecords);
                    } catch (error) {
                        console.error("Error fetching professor info:", error);
                    }
                }
            }
        };

        fetchProfessorInfo();
    }, [courseRecords]); // Run when courseRecords change

    const calculateGPA = (records) => {
        if (!records || records.length === 0) return 0;
        const totalPoints = records.reduce((sum, record) => sum + (record.grade * record.credits), 0);
        const totalCredits = records.reduce((sum, record) => sum + record.credits, 0);
        return totalCredits > 0 ? (totalPoints / totalCredits).toFixed(2) : "0.00";
    };

    if (loading) {
        return (
            <div className="shell">
                <header className="topbar">
                    <img src={umsLogo} width={90} alt="UMS Logo"/>
                    <h2>Parent Dashboard</h2>
                </header>
                <main>
                    <div className="loading">Loading data...</div>
                </main>
            </div>
        );
    }

    return (
        <div className="shell">
            <header className="topbar">
                <img src={umsLogo} width={90} alt="UMS Logo"/>
                <h2>Parent Dashboard</h2>
            </header>

            <main>
                <div className="tabs">
                    <button
                        className={activeTab === "children" ? "active" : ""}
                        onClick={() => setActiveTab("children")}
                    >
                        Children List ({children.length})
                    </button>
                    <button
                        className={activeTab === "grades" ? "active" : ""}
                        onClick={() => setActiveTab("grades")}
                    >
                        Grade Reports
                    </button>
                </div>

                {activeTab === "children" && (
                    <>
                        <h3>Your Children</h3>
                        {children.length === 0 ? (
                            <div className="no-data">No children registered</div>
                        ) : (
                            <table className="children-table">
                                <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Name</th>
                                    <th>Department</th>
                                    {/*<th>GPA</th>*/}
                                    <th>Email</th>
                                    <th>Courses Taken</th>
                                    <th>Actions</th>
                                </tr>
                                </thead>
                                <tbody>
                                {children.map((child) => {
                                    const records = courseRecords[child.studentId] || [];
                                    return (
                                        <tr key={child.studentId}>
                                            <td>{child.studentId}</td>
                                            <td>{child.name}</td>
                                            <td>{child.major?.majorName || "Undeclared"}</td>
                                            {/*<td>{calculateGPA(records)}</td>*/}
                                            <td>{child.email}</td>
                                            <td>{records.length}</td>
                                            <td>
                                                <button
                                                    className="view-grades-btn"
                                                    onClick={() => {
                                                        setSelectedStudent(child);
                                                        setActiveTab("grades");
                                                    }}
                                                >
                                                    View Grades
                                                </button>
                                            </td>
                                        </tr>
                                    );
                                })}
                                </tbody>
                            </table>
                        )}
                    </>
                )}

                {activeTab === "grades" && (
                    <>
                        <div className="grades-header">
                            <h3>
                                {selectedStudent
                                    ? `Grade Report for ${selectedStudent.name} (${selectedStudent.studentId})`
                                    : "All Children Grade Reports"
                                }
                            </h3>
                            {selectedStudent && (
                                <button
                                    className="back-btn"
                                    onClick={() => setSelectedStudent(null)}
                                >
                                    ← Back to All
                                </button>
                            )}
                        </div>

                        {selectedStudent ? (
                            // Single student grade report
                            <div className="grade-report">
                                <div className="student-summary">
                                    <p><strong>Student:</strong> {selectedStudent.name}</p>
                                    <p><strong>ID:</strong> {selectedStudent.studentId}</p>
                                    <p><strong>Department:</strong> {selectedStudent.major?.majorName || "Undeclared"}</p>

                                </div>

                                {courseRecords[selectedStudent.studentId]?.length > 0 ? (
                                    // Update the grades table to include professor columns
                                    <table className="grades-table">
                                        <thead>
                                        <tr>
                                            <th>Course Name</th>
                                            <th>Grade</th>
                                            <th>Credits</th>
                                            <th>Semester</th>
                                            <th>Professor</th>
                                            <th>Contact</th>
                                           {/* <th>Grade Points</th>*/}
                                        </tr>
                                        </thead>
                                        <tbody>
                                        {courseRecords[selectedStudent.studentId].map((record, index) => (
                                            <tr key={index}>
                                                <td>{record.courseName}</td>
                                                <td className={`grade-${record.grade >= 60 ? 'pass' : 'fail'}`}>
                                                    {record.grade}%
                                                </td>
                                                <td>{record.credits}</td>
                                                <td>{record.semester}</td>
                                                <td>{record.professorName}</td>
                                                <td>
                                                    {record.professorEmail && (
                                                        <a
                                                            href={`mailto:${record.professorEmail}`}
                                                            className="contact-link"
                                                        >
                                                            📧 Email
                                                        </a>
                                                    )}
                                                </td>
                                                <td></td>
                                            </tr>
                                        ))}
                                        </tbody>
                                    </table>
                                ) : (
                                    <div className="no-data">No course records found</div>
                                )}
                            </div>
                        ) : (
                            // All students grade reports
                            <div className="all-grades">
                                {children.map((child) => {
                                    const records = courseRecords[child.studentId] || [];
                                    return (
                                        <div key={child.studentId} className="student-grade-card">
                                            <div className="card-header">
                                                <h4>{child.name} ({child.studentId})</h4>
                                                {/*<span className="gpa-badge">GPA: {calculateGPA(records)}</span>*/}
                                            </div>
                                            <p><strong>Department:</strong> {child.major?.majorName || "Undeclared"}</p>
                                            <p><strong>Courses Completed:</strong> {records.length}</p>

                                            {records.length > 0 && (
                                                <div className="recent-courses">
                                                    <strong>Recent Courses:</strong>
                                                    <ul>
                                                        {records.slice(0, 3).map((record, idx) => (
                                                            <li key={idx}>
                                                                {record.courseName}: {record.grade}% ({record.semester})
                                                            </li>
                                                        ))}
                                                        {records.length > 3 && (
                                                            <li>... and {records.length - 3} more</li>
                                                        )}
                                                    </ul>
                                                </div>
                                            )}

                                            <button
                                                className="view-details-btn"
                                                onClick={() => setSelectedStudent(child)}
                                            >
                                                View Full Report
                                            </button>
                                        </div>
                                    );
                                })}
                            </div>
                        )}
                    </>
                )}
            </main>
        </div>
    );
}