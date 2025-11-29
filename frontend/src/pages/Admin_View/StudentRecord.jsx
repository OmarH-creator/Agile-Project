import React, {useState} from 'react';
import umsLogo from "../../assets/UMS Logo.png";
import {
    buildStudentSnapshot,
    createStudent,
    deleteStudent,
    downloadBlob,
    emptyStudent,
    getTranscript,
    Icon,
    sumCredits,
    ErrorMessage
} from './Admin-Student-Api';
import './StudentRecord.css'
// Helper for currency

const StudentRecord = ({student}) => {
    const [transcriptModal, setTranscriptModal] = useState({ open: false, content: '', loading: false });
    const [formMode, setFormMode] = useState(null);
    const [formData, setFormData] = useState(emptyStudent);
    const [validationError, setValidationError] = useState('');
    const [selectedCode, setSelectedCode] = useState(null);
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);


    const clearError = () => setError(null);

    // Safety check
    if (!student) return null;

    // Calculate total hours from the array since backend doesn't provide it
    const totalCredits = student.completedCourses?.reduce((acc, c) => acc + (c.credits || 0), 0) || 0;
    // Inside StudentRecord.jsx
    console.log("FULL STUDENT OBJECT:", student);

    const closeForm = () => {
        setFormMode(null);
        setFormData(emptyStudent);
        setValidationError('');
    };

    const openEditStudent = (student) => {
        setFormData({
            ...student,
            completedHours:
                student.completedHours ?? sumCredits(student.academicHistory),
            gradYear: student.gradYear ?? '',
            fees: student.fees ?? 0
        });
        setFormMode('edit');
        setValidationError('');
    };

    const handleGenerateTranscript = async () => {
        if (!student) return;

        // Clear error when user retries operation
        clearError();
        setTranscriptModal({ open: true, content: '', loading: true });

        try {
            const transcriptText = await getTranscript(student.code);
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

        downloadBlob(
            transcriptModal.content,
            `${student.code}-transcript.txt`,
            'text/plain'
        );
    };

    const handleFormChange = (event) => {
        const { name, value } = event.target;
        setFormData((prev) => ({
            ...prev,
            [name]: name === 'code' ? value.toUpperCase() : value
        }));
    };

    const handleFormSubmit = async (event) => {
        event.preventDefault();

        // Clear error when user retries operation
        clearError();

        const normalized = {
            ...formData,
            code: formData.code.trim().toUpperCase(),
            name: formData.name.trim(),
            email: formData.email.trim(),
            phone: formData.phone.trim(),
            address: formData.address.trim(),
            majorId: formData.majorId.trim(),
            majorName: formData.majorName.trim(),
            militaryStatus: formData.militaryStatus.trim(),
            notes: (formData.notes ?? '').trim(),
            completedHours: Number(formData.completedHours) || 0,
            fees: Number(formData.fees) || 0,
            cgpa: Number(formData.cgpa) || 0,
            gradYear: formData.gradYear ? Number(formData.gradYear) : ''
        };

        if (!normalized.code || !normalized.name) {
            setValidationError('Student code and name are required.');
            return;
        }

        if (formMode === 'add') {
            // if (students.some((student) => student.code === normalized.code)) {
            //     setValidationError('Student code already exists.');
            //     return;
            // }

            const newStudent = {
                ...emptyStudent,
                ...normalized,
                academicHistory: [],
                currentRegistrations: [],
                holds: []
            };

            // Call backend API to create student
            setLoading(true);
            try {
                await createStudent(newStudent);

                closeForm();
            } catch (err) {
                const errorMessage = err.message || 'Failed to create student';
                setError(errorMessage);
                setValidationError(errorMessage);
            } finally {
                setLoading(false);
            }
            return;
        }

        if (formMode === 'edit' && student) {
            // NOTE: Edit mode updates local state only.
            // The backend Admin Controller does not currently provide a PUT endpoint for updating student records.
            // Once a PUT endpoint is implemented (e.g., PUT /api/admin/students/{studentId}),
            // this section should be updated to call an updateStudent API function similar to createStudent.
            // Until then, edits are maintained in the frontend state only and will not persist across sessions.

            closeForm();
        }
    };

    const handleDeleteStudent = async (code) => {
        const confirmation = window.confirm(
            `This will remove ${code} and their academic record from the dashboard. Continue?`
        );
        if (!confirmation) {
            return;
        }

        // Clear error when user retries operation
        clearError();
        setLoading(true);

        try {
            // Call deleteStudent API function after user confirmation
            const successMessage = await deleteStudent(code);
            const confirmation = window.confirm(
                `Removed ${code} and their academic record from the dashboard successfully!`
            );

        } catch (err) {
            // Handle case where student is not found or other errors
            const errorMessage = err.message || 'Failed to delete student';
            setError(errorMessage);
        } finally {
            setLoading(false);
        }
    };

    const handleExport = (student, format) => {
        const snapshot = buildStudentSnapshot(student);

        if (format === 'json') {
            downloadBlob(
                JSON.stringify(snapshot, null, 2),
                `${student.code}-record.json`,
                'application/json'
            );
            return;
        }

        const escapeCsv = (value) => {
            const stringValue = String(value ?? '').replace(/"/g, '""');
            return `"${stringValue}"`;
        };

        const summarySection = [
            ['Field', 'Value'],
            ['Code', student.code],
            ['Name', student.name],
            ['Email', student.email],
            ['Major', student.majorName],
            ['Status', student.status],
            ['CGPA', snapshot.cgpa ?? 'N/A'],
            ['Completed Hours', snapshot.completedHours],
            ['Fees Due', snapshot.feesDue],
            ['Graduation Year', snapshot.gradYear ?? 'N/A'],
            ['Military Status', student.militaryStatus],
            ['Address', student.address],
            ['Notes', snapshot.notes]
        ]
            .map((row) => row.map(escapeCsv).join(','))
            .join('\n');

        const transcriptHeader = '\n\n"Course Code","Course Title","Credits","Grade","Term"';
        const transcriptBody = (student.academicHistory ?? [])
            .map((record) =>
                [
                    escapeCsv(record.code),
                    escapeCsv(record.title),
                    record.credits ?? 3,
                    escapeCsv(record.grade),
                    escapeCsv(record.term)
                ].join(',')
            )
            .join('\n');

        const csvContent = `${summarySection}${transcriptHeader}\n${transcriptBody}`;
        downloadBlob(csvContent, `${student.code}-record.csv`, 'text/csv');
    };
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
                            <p> {student.studentId} {student.major?.majorName}</p>
                            <p>{student.email}</p>
                        </div>
                    </div>
                    <div className="detail-actions">
                        <button type="button" className="ghost-btn" onClick={() => openEditStudent(student)}>
                            {Icon.edit16} <span>Edit</span>
                        </button>
                        <button type="button" className="ghost-btn danger" onClick={() => handleDeleteStudent(student.studentId)}>
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
                            </div>
                        ))}
                        {(!student.currentCourses || student.currentCourses.length === 0) && (
                            <div className="empty-state small">No active courses.</div>
                        )}
                    </div>
                </div>
            </article>


            {formMode && (
                <div className="modal-backdrop">
                    <div className="modal">
                        {/* HEADER */}
                        <header className="modal-header">
                            <h3>{formMode === 'add' ? 'Add Student' : 'Edit Student'}</h3>
                            <button type="button" className="close-btn" onClick={closeForm}>
                                {Icon.close16}
                            </button>
                        </header>

                        {/* BODY (Scrollable) */}
                        <div className="modal-body">
                            <form id="studentForm" onSubmit={handleFormSubmit}>
                                <div className="form-grid">
                                    <label>
                                        <span>Student Code</span>
                                        <input name="code" value={formData.code} onChange={handleFormChange} required disabled={formMode === 'edit'} />
                                    </label>
                                    <label>
                                        <span>Full Name</span>
                                        <input name="name" value={formData.name} onChange={handleFormChange} required />
                                    </label>
                                    <label>
                                        <span>Email</span>
                                        <input type="email" name="email" value={formData.email} onChange={handleFormChange} />
                                    </label>
                                    <label>
                                        <span>Phone</span>
                                        <input name="phone" value={formData.phone} onChange={handleFormChange} />
                                    </label>
                                    <label>
                                        <span>Major</span>
                                        <input name="majorName" value={formData.majorName} onChange={handleFormChange} />
                                    </label>
                                    <label>
                                        <span>Major ID</span>
                                        <input name="majorId" value={formData.majorId} onChange={handleFormChange} />
                                    </label>
                                    <label>
                                        <span>Status</span>
                                        <select name="status" value={formData.status} onChange={handleFormChange}>
                                            <option value="Active">Active</option>
                                            <option value="Probation">Probation</option>
                                            <option value="Suspended">Suspended</option>
                                            <option value="Graduated">Graduated</option>
                                        </select>
                                    </label>
                                    <label>
                                        <span>CGPA</span>
                                        <input name="cgpa" value={formData.cgpa} onChange={handleFormChange} />
                                    </label>
                                    <label>
                                        <span>Hours</span>
                                        <input name="completedHours" value={formData.completedHours} onChange={handleFormChange} />
                                    </label>
                                    <label>
                                        <span>Fees</span>
                                        <input name="fees" value={formData.fees} onChange={handleFormChange} />
                                    </label>
                                    <label>
                                        <span>Grad Year</span>
                                        <input name="gradYear" value={formData.gradYear} onChange={handleFormChange} />
                                    </label>
                                    <label>
                                        <span>Military Status</span>
                                        <input name="militaryStatus" value={formData.militaryStatus} onChange={handleFormChange} />
                                    </label>
                                    <label className="full">
                                        <span>Address</span>
                                        <input name="address" value={formData.address} onChange={handleFormChange} />
                                    </label>
                                    <label className="full">
                                        <span>Notes</span>
                                        <textarea name="notes" value={formData.notes} onChange={handleFormChange} />
                                    </label>
                                </div>
                            </form>
                            {validationError && (
                                <p className="form-error" style={{ marginTop: '1rem' }}>{validationError}</p>
                            )}
                        </div>

                        {/* FOOTER (Fixed at bottom) */}
                        <footer className="modal-footer">
                            <button type="button" className="ghost-btn" onClick={closeForm} disabled={loading}>
                                Cancel
                            </button>
                            <button
                                type="submit"
                                form="studentForm"
                                className="primary-btn"
                                disabled={loading}
                            >
                                {loading ? Icon.spinner : Icon.check16}
                                <span>{loading ? 'Saving...' : 'Save'}</span>
                            </button>
                        </footer>
                    </div>
                </div>
            )}

            {/* --- MODAL: TRANSCRIPT --- */}
            {/*{transcriptModal.open && (*/}
            {/*    <div className="modal-backdrop" role="dialog" aria-modal="true">*/}
            {/*        <div className="modal">*/}
            {/*            <header className="modal-header">*/}
            {/*                <h3>Student Transcript</h3>*/}
            {/*                <button type="button" className="close-btn" onClick={closeTranscriptModal} aria-label="Close">*/}
            {/*                    {Icon.close16}*/}
            {/*                </button>*/}
            {/*            </header>*/}
            {/*            <div className="modal-body">*/}
            {/*                {transcriptModal.loading ? (*/}
            {/*                    <LoadingSpinner size="medium" message="Generating transcript..." />*/}
            {/*                ) : (*/}
            {/*                    <pre className="transcript-viewer">{transcriptModal.content}</pre>*/}
            {/*                )}*/}
            {/*            </div>*/}
            {/*            <footer className="modal-footer">*/}
            {/*                <button type="button" className="ghost-btn" onClick={closeTranscriptModal}>*/}
            {/*                    Close*/}
            {/*                </button>*/}
            {/*                <button*/}
            {/*                    type="button"*/}
            {/*                    className="primary-btn"*/}
            {/*                    onClick={downloadTranscript}*/}
            {/*                    disabled={transcriptModal.loading || !transcriptModal.content}*/}
            {/*                >*/}
            {/*                    {Icon.download16}*/}
            {/*                    <span>Download</span>*/}
            {/*                </button>*/}
            {/*            </footer>*/}
            {/*        </div>*/}
            {/*    </div>*/}
            {/*)}*/}
        </div>
    );
};

export default StudentRecord;