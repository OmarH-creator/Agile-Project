import React, { useState } from 'react';
import umsLogo from "../../assets/UMS Logo.png";

// IMPORT 1: Get the Logic from the NEW file
import {
    updateProfessor,
    deleteProfessor,
    assignCourseToProfessor
} from './Admin-Professor-Api';

// IMPORT 2: Get the Icons and UI helpers from the OLD file
import {
    Icon,
    ErrorMessage
} from './Admin-Student-Api';

import './ProfessorRecord.css'; // Make sure this file exists (Step 3)

const ProfessorRecord = ({ professor, onRefresh }) => {
    // Modes: 'edit' (details), 'assign' (add course), null (view only)
    const [formMode, setFormMode] = useState(null);
    const [formData, setFormData] = useState({});
    const [courseInput, setCourseInput] = useState('');
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);

    // Safety check
    if (!professor) return null;

    const clearError = () => setError(null);

    const closeForm = () => {
        setFormMode(null);
        setFormData({});
        setCourseInput('');
        clearError();
    };

    // --- HANDLERS ---

    const openEditProfessor = () => {
        setFormData({
            professorName: professor.professorName,
            professorEmail: professor.professorEmail,
            professorDepartment: professor.professorDepartment
        });
        setFormMode('edit');
    };

    const openAssignCourse = () => {
        setCourseInput('');
        setFormMode('assign');
    };

    const handleEditSubmit = async (e) => {
        e.preventDefault();
        clearError();
        setLoading(true);

        try {
            await updateProfessor(professor.professorId, formData);
            if (onRefresh) onRefresh(); // Callback to parent to reload data
            closeForm();
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const handleAssignSubmit = async (e) => {
        e.preventDefault();
        if (!courseInput.trim()) return;

        clearError();
        setLoading(true);

        try {
            await assignCourseToProfessor(professor.professorId, courseInput.trim());
            if (onRefresh) onRefresh();
            closeForm();
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async () => {
        if (!window.confirm(`Are you sure you want to delete Professor ${professor.professorName}?`)) {
            return;
        }

        setLoading(true);
        try {
            await deleteProfessor(professor.professorId);
            window.alert('Professor deleted successfully');
            // Depending on your parent component, you might want to redirect or clear selection here
            if (onRefresh) onRefresh();
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
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
                            <h2>{professor.professorName}</h2>
                            <p>{professor.professorId} • {professor.professorDepartment}</p>
                            <p>{professor.professorEmail}</p>
                        </div>
                    </div>
                    <div className="detail-actions">
                        {/* Edit Button */}
                        <button className="ghost-btn" onClick={openEditProfessor}>
                            {Icon.edit16} <span>Edit</span>
                        </button>
                        {/* Delete Button */}
                        <button className="ghost-btn danger" onClick={handleDelete}>
                            {Icon.trash16} <span>Delete</span>
                        </button>
                    </div>
                </header>

                {/* --- KPI Grid --- */}
                <div className="kpi-grid">
                    <div className="kpi-card">
                        <span className="kpi-label">Courses Assigned</span>
                        <strong>{professor.professorCourses?.length || 0}</strong>
                    </div>
                    <div className="kpi-card">
                        <span className="kpi-label">Department</span>
                        <strong>{professor.professorDepartment || 'N/A'}</strong>
                    </div>
                    <div className="kpi-card">
                        <span className="kpi-label">Status</span>
                        <strong>Active</strong>
                    </div>
                </div>
            </article>

            {/* --- ERROR DISPLAY --- */}
            <ErrorMessage error={error} onDismiss={clearError} />

            {/* --- COURSES LIST --- */}
            <article className="history-card">
                <header>
                    <h3>Assigned Courses</h3>
                    <button className="ghost-btn" onClick={openAssignCourse}>
                        {Icon.plus16} <span>Assign Course</span>
                    </button>
                </header>
                <div className="history-table-wrapper">
                    {professor.professorCourses && professor.professorCourses.length > 0 ? (
                        <div className="course-chips-container">
                            {professor.professorCourses.map((course, idx) => (
                                <div key={idx} className="course-chip">
                                    <span className="chip-icon">{Icon.facilities}</span>
                                    <span className="chip-text">{course}</span>
                                </div>
                            ))}
                        </div>
                    ) : (
                        <div className="empty-state">No courses assigned yet.</div>
                    )}
                </div>
            </article>

            {/* --- MODAL: EDIT PROFESSOR --- */}
            {formMode === 'edit' && (
                <div className="modal-backdrop">
                    <div className="modal">
                        <header className="modal-header">
                            <h3>Edit Professor</h3>
                            <button type="button" className="close-btn" onClick={closeForm}>
                                {Icon.close16}
                            </button>
                        </header>
                        <div className="modal-body">
                            <form id="editProfForm" onSubmit={handleEditSubmit}>
                                <div className="form-grid">
                                    <label className="full">
                                        <span>Full Name</span>
                                        <input
                                            name="professorName"
                                            value={formData.professorName}
                                            onChange={handleInputChange}
                                            required
                                        />
                                    </label>
                                    <label className="full">
                                        <span>Email</span>
                                        <input
                                            type="email"
                                            name="professorEmail"
                                            value={formData.professorEmail}
                                            onChange={handleInputChange}
                                            required
                                        />
                                    </label>
                                    <label className="full">
                                        <span>Department</span>
                                        <input
                                            name="professorDepartment"
                                            value={formData.professorDepartment}
                                            onChange={handleInputChange}
                                            required
                                        />
                                    </label>
                                </div>
                            </form>
                        </div>
                        <footer className="modal-footer">
                            <button type="button" className="ghost-btn" onClick={closeForm}>Cancel</button>
                            <button type="submit" form="editProfForm" className="primary-btn" disabled={loading}>
                                {loading ? Icon.spinner : 'Save Changes'}
                            </button>
                        </footer>
                    </div>
                </div>
            )}

            {/* --- MODAL: ASSIGN COURSE --- */}
            {formMode === 'assign' && (
                <div className="modal-backdrop">
                    <div className="modal">
                        <header className="modal-header">
                            <h3>Assign Course</h3>
                            <button type="button" className="close-btn" onClick={closeForm}>
                                {Icon.close16}
                            </button>
                        </header>
                        <div className="modal-body">
                            <form id="assignForm" onSubmit={handleAssignSubmit}>
                                <p style={{marginBottom:'1rem', color:'var(--neo-muted)'}}>
                                    Enter the course name/code exactly as it appears in the system.
                                </p>
                                <label className="full">
                                    <span>Course Name</span>
                                    <input
                                        value={courseInput}
                                        onChange={(e) => setCourseInput(e.target.value)}
                                        placeholder="e.g. CS101 Introduction to CS"
                                        required
                                        autoFocus
                                    />
                                </label>
                            </form>
                        </div>
                        <footer className="modal-footer">
                            <button type="button" className="ghost-btn" onClick={closeForm}>Cancel</button>
                            <button type="submit" form="assignForm" className="primary-btn" disabled={loading}>
                                {loading ? Icon.spinner : 'Assign'}
                            </button>
                        </footer>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ProfessorRecord;