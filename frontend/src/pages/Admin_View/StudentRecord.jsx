import React, { useState } from 'react';
import umsLogo from "../../assets/UMS Logo.png";
import {
    downloadBlob,
    getTranscript,
    Icon,
} from './Admin-Student-Api';
import './StudentRecord.css';

const StudentRecord = ({ student }) => {
    // State management
    const [transcriptModal, setTranscriptModal] = useState({ open: false, content: '', loading: false });
    const [error, setError] = useState(null);

    const clearError = () => setError(null);

    // Safety check
    if (!student) return null;

    // Calculate total credits
    const totalCredits = student.completedCourses?.reduce((acc, c) => acc + (c.credits || 0), 0) || 0;

    // --- Transcript Logic ---
    const handleGenerateTranscript = async () => {
        // Clear error when user retries operation
        clearError();
        setTranscriptModal({ open: true, content: '', loading: true });

        try {
            // Check if we have a student code to query
            const codeToUse = student.studentId || student.code;
            if (!codeToUse) throw new Error("Student ID not found");

            const transcriptText = await getTranscript(codeToUse);
            setTranscriptModal({ open: true, content: transcriptText, loading: false });
        } catch (err) {
            const errorMessage = err.message || 'Failed to generate transcript';
            setError(errorMessage);
            setTranscriptModal({ open: false, content: '', loading: false });
        }
    };

    const closeTranscriptModal = () => {
        setTranscriptModal({ open: false, content: '', loading: false });
    };

    const downloadTranscript = () => {
        if (!student || !transcriptModal.content) return;
        const filename = student.studentId || student.code || 'transcript';
        downloadBlob(
            transcriptModal.content,
            `${filename}-transcript.txt`,
            'text/plain'
        );
    };

    return (
        <div className="detail-panel-content">

            {/* --- ACTION TOOLBAR (Added this so you can trigger the modes) --- */}
            <div className="record-toolbar" style={{ marginBottom: '1rem', display: 'flex', gap: '10px' }}>
                <button className="secondary-btn" onClick={handleGenerateTranscript}>
                    {Icon.download16 || 'Doc'} Generate Transcript
                </button>
            </div>

            {/* --- HEADER --- */}
            <article className="detail-card">
                <header className="detail-card-header">
                    <div className="detail-ident">
                        <div className="detail-logo-shell">
                            <img src={umsLogo} alt="Logo" />
                        </div>
                        <div>
                            <h2>{student.name}</h2>
                            <p> {student.studentId} {student.major?.majorName}</p>
                            <p>{student.email}</p>
                        </div>
                    </div>
                </header>

                {error && <div className="error-banner" style={{ color: 'red', padding: '10px' }}>{error}</div>}

                <div className="kpi-grid">
                    <div className="kpi-card">
                        <span className="kpi-label">CGPA</span>
                        <strong>{student.gpa ? Number(student.gpa).toFixed(2) : '0.00'}</strong>
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
                    <div><span className="meta-label">Birthdate</span><span className="meta-value">{student.dateOfBirth ? new Date(student.dateOfBirth).toLocaleDateString() : 'N/A'}</span></div>
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
                            </div>
                        ))}
                        {(!student.currentCourses || student.currentCourses.length === 0) && (
                            <div className="empty-state small">No active courses.</div>
                        )}
                    </div>
                </div>
            </article>



            {/* --- MODAL: TRANSCRIPT (Uncommented and fixed) --- */}
            {transcriptModal.open && (
                <div className="modal-backdrop" role="dialog" aria-modal="true">
                    <div className="modal">
                        <header className="modal-header">
                            <h3>Student Transcript</h3>
                            <button type="button" className="close-btn" onClick={closeTranscriptModal} aria-label="Close">
                                {Icon.close16 || 'X'}
                            </button>
                        </header>
                        <div className="modal-body">
                            {transcriptModal.loading ? (
                                <div style={{ textAlign: 'center', padding: '20px' }}>
                                    {Icon.spinner || 'Loading...'} Generating transcript...
                                </div>
                            ) : (
                                <pre className="transcript-viewer" style={{ whiteSpace: 'pre-wrap', maxHeight: '400px', overflowY: 'auto' }}>
                                    {transcriptModal.content}
                                </pre>
                            )}
                        </div>
                        <footer className="modal-footer">
                            <button
                                type="button"
                                className="primary-btn"
                                onClick={downloadTranscript}
                                disabled={transcriptModal.loading || !transcriptModal.content}
                            >
                                {Icon.download16 || 'DL'}
                                <span>Download</span>
                            </button>
                        </footer>
                    </div>
                </div>
            )}
        </div>
    );
};

export default StudentRecord;