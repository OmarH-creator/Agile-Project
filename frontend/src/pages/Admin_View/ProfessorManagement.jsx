import React, { useState, useEffect } from 'react';
import ProfessorRecord from './ProfessorRecords'; // The component you created earlier
import { getAllProfessors, createProfessor } from './Admin-Professor-Api';
import { Icon, LoadingSpinner, ErrorMessage } from './Admin-Student-Api'; // Reusing UI helpers
import './ProfessorRecord.css'; // Reusing the same CSS

const ProfessorsManagement = () => {
    const [professors, setProfessors] = useState([]);
    const [selectedProfessor, setSelectedProfessor] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [isAddModalOpen, setAddModalOpen] = useState(false);

    // New Professor Form State
    const [newProfData, setNewProfData] = useState({
        professorId: '',
        professorName: '',
        professorEmail: '',
        professorDepartment: ''
    });

    // 1. Fetch Data on Mount
    useEffect(() => {
        fetchProfessors();
    }, []);

    const fetchProfessors = async () => {
        setLoading(true);
        try {
            const data = await getAllProfessors();

            // FIX: Check if 'data.content' exists (Pagination format), otherwise use 'data' (List format)
            const professorList = data.content ? data.content : data;

            setProfessors(professorList);
            setError(null);
        } catch (err) {
            console.error("Fetch error details:", err); // Helps debugging
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };
    // 2. Refresh wrapper to pass to child
    const handleRefresh = async () => {
        await fetchProfessors();
        // If we have a selected professor, we need to update their object in the selection too
        if (selectedProfessor) {
            const updatedList = await getAllProfessors();
            const updatedProf = updatedList.find(p => p.professorId === selectedProfessor.professorId);
            setSelectedProfessor(updatedProf || null);
        }
    };

    const handleCreateSubmit = async (e) => {
        e.preventDefault();
        try {
            await createProfessor(newProfData);
            setAddModalOpen(false);
            setNewProfData({ professorId: '', professorName: '', professorEmail: '', professorDepartment: '' }); // Reset
            fetchProfessors(); // Reload list
        } catch (err) {
            alert(err.message);
        }
    };

    // --- RENDER ---

    // If a professor is selected, show the Detail View (ProfessorRecord)
    if (selectedProfessor) {
        return (
            <div>
                {/* Back Button Header */}
                <div style={{ marginBottom: '20px', display: 'flex', alignItems: 'center', gap: '10px' }}>
                    <button className="ghost-btn" onClick={() => setSelectedProfessor(null)}>
                        ← Back to List
                    </button>
                </div>

                {/* The Component I gave you earlier */}
                <ProfessorRecord
                    professor={selectedProfessor}
                    onRefresh={handleRefresh}
                />
            </div>
        );
    }

    // Otherwise, show the List View (Table)
    return (
        <div className="detail-panel-content">
            <article className="detail-card">
                <header className="detail-card-header">
                    <div>
                        <h2>Professors Directory</h2>
                        <p style={{ color: 'var(--neo-muted)' }}>Manage faculty members and course assignments</p>
                    </div>
                    <button className="primary-btn" onClick={() => setAddModalOpen(true)}>
                        {Icon.plus16} <span>Add Professor</span>
                    </button>
                </header>

                <ErrorMessage error={error} onDismiss={() => setError(null)} />

                {loading ? (
                    <LoadingSpinner />
                ) : (
                    <div className="history-table-wrapper">
                        <table className="history-table">
                            <thead>
                            <tr>
                                <th>ID</th>
                                <th>Name</th>
                                <th>Department</th>
                                <th>Email</th>
                                <th>Courses</th>
                                <th>Action</th>
                            </tr>
                            </thead>
                            <tbody>
                            {professors.map((prof) => (
                                <tr key={prof.professorId} onClick={() => setSelectedProfessor(prof)} style={{ cursor: 'pointer' }}>
                                    <td style={{ fontWeight: 'bold' }}>{prof.professorId}</td>
                                    <td>{prof.professorName}</td>
                                    <td>
                                            <span style={{
                                                padding: '4px 8px',
                                                borderRadius: '8px',
                                                background: 'var(--sub-card-bg)',
                                                fontSize: '12px'
                                            }}>
                                                {prof.professorDepartment}
                                            </span>
                                    </td>
                                    <td>{prof.professorEmail}</td>
                                    <td>{prof.professorCourses?.length || 0}</td>
                                    <td>
                                        <button className="ghost-btn" onClick={(e) => {
                                            e.stopPropagation(); // Prevent row click
                                            setSelectedProfessor(prof);
                                        }}>
                                            View
                                        </button>
                                    </td>
                                </tr>
                            ))}
                            {professors.length === 0 && (
                                <tr>
                                    <td colSpan="6" className="empty-state">No professors found.</td>
                                </tr>
                            )}
                            </tbody>
                        </table>
                    </div>
                )}
            </article>

            {/* --- ADD PROFESSOR MODAL --- */}
            {isAddModalOpen && (
                <div className="modal-backdrop">
                    <div className="modal">
                        <header className="modal-header">
                            <h3>Add New Professor</h3>
                            <button className="close-btn" onClick={() => setAddModalOpen(false)}>{Icon.close16}</button>
                        </header>
                        <div className="modal-body">
                            <form id="addProfForm" onSubmit={handleCreateSubmit}>
                                <div className="form-grid">
                                    <label>
                                        <span>ID (e.g. P-101)</span>
                                        <input required value={newProfData.professorId} onChange={e => setNewProfData({...newProfData, professorId: e.target.value})} />
                                    </label>
                                    <label>
                                        <span>Full Name</span>
                                        <input required value={newProfData.professorName} onChange={e => setNewProfData({...newProfData, professorName: e.target.value})} />
                                    </label>
                                    <label>
                                        <span>Email</span>
                                        <input type="email" required value={newProfData.professorEmail} onChange={e => setNewProfData({...newProfData, professorEmail: e.target.value})} />
                                    </label>
                                    <label>
                                        <span>Department</span>
                                        <input required value={newProfData.professorDepartment} onChange={e => setNewProfData({...newProfData, professorDepartment: e.target.value})} />
                                    </label>
                                </div>
                            </form>
                        </div>
                        <footer className="modal-footer">
                            <button className="ghost-btn" onClick={() => setAddModalOpen(false)}>Cancel</button>
                            <button className="primary-btn" type="submit" form="addProfForm">Create</button>
                        </footer>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ProfessorsManagement;