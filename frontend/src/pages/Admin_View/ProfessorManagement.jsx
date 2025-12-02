import React, { useState, useEffect } from 'react';
import ProfessorRecord from './ProfessorRecords'; // The component you created earlier
import { getAllProfessors, createProfessor, deleteProfessor, updateProfessor } from './Admin-Professor-Api';
import { Icon, LoadingSpinner, ErrorMessage } from './Admin-Student-Api'; // Reusing UI helpers
import './ProfessorRecord.css'; // Reusing the same CSS

const ProfessorsManagement = () => {
    const [professors, setProfessors] = useState([]);
    const [selectedProfessor, setSelectedProfessor] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);


    // --- MODAL STATES ---
    const [isAddModalOpen, setAddModalOpen] = useState(false);
    const [isEditModalOpen, setEditModalOpen] = useState(false);

    // New Professor Form State
    const [newProfData, setNewProfData] = useState({
        professorId: '',
        professorName: '',
        professorEmail: '',
        professorDepartment: ''
    });

    const [editProfData, setEditProfData] = useState({
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
        // 1. Refresh the main list
        await fetchProfessors();

        // 2. If a specific professor is open, refresh their details too
        if (selectedProfessor) {
            const data = await getAllProfessors();

            // FIX: Extract the array from the pagination object
            const fullList = data.content ? data.content : data;

            // Now 'fullList' is an array, so .find() will work
            const updatedProf = fullList.find(p => p.professorId === selectedProfessor.professorId);

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

    // --- EDIT HANDLERS ---
    const openEditModal = (prof, e) => {
        e.stopPropagation(); // Prevent opening the Detail View
        setEditProfData({
            professorId: prof.professorId,
            professorName: prof.professorName,
            professorEmail: prof.professorEmail,
            professorDepartment: prof.professorDepartment
        });
        setEditModalOpen(true);
    };

    const handleEditSubmit = async (e) => {
        e.preventDefault();
        try {
            // We use editProfData.professorId to identify which one to update
            await updateProfessor(editProfData.professorId, editProfData);
            setEditModalOpen(false);
            fetchProfessors(); // Refresh list
        } catch (err) {
            alert(err.message);
        }
    };

    // --- DELETE HANDLER ---
    const handleDeleteClick = async (profId, profName, e) => {
        e.stopPropagation(); // Prevent opening the Detail View

        if (!window.confirm(`Are you sure you want to permanently delete ${profName}?`)) {
            return;
        }

        try {
            await deleteProfessor(profId);
            // If the deleted professor was currently open in detail view, close it
            if (selectedProfessor && selectedProfessor.professorId === profId) {
                setSelectedProfessor(null);
            }
            fetchProfessors(); // Refresh list
        } catch (err) {
            alert(err.message);
        }
    };

    // --- RENDER ---

    // Detail View
    if (selectedProfessor) {
        return (
            <div>
                <div style={{ marginBottom: '20px', display: 'flex', alignItems: 'center', gap: '10px' }}>
                    <button className="ghost-btn" onClick={() => setSelectedProfessor(null)}>
                        ← Back to List
                    </button>
                </div>
                <ProfessorRecord
                    professor={selectedProfessor}
                    onRefresh={handleRefresh}
                />
            </div>
        );
    }

    // Otherwise, show the List View (Table)
    // List View
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
                                <th style={{ textAlign: 'right' }}>Actions</th>
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

                                    {/* --- ACTION BUTTONS --- */}
                                    <td style={{ textAlign: 'right' }}>
                                        <div style={{ display: 'flex', gap: '8px', justifyContent: 'flex-end' }}>

                                            {/* Edit Button */}
                                            <button
                                                className="ghost-btn"
                                                title="Edit Details"
                                                onClick={(e) => openEditModal(prof, e)}
                                            >
                                                {Icon.edit16}
                                            </button>

                                            {/* Delete Button */}
                                            <button
                                                className="ghost-btn danger"
                                                title="Delete Professor"
                                                onClick={(e) => handleDeleteClick(prof.professorId, prof.professorName, e)}
                                            >
                                                {Icon.trash16}
                                            </button>

                                            {/* View Button (Optional, since row is clickable) */}
                                            <button className="ghost-btn" onClick={(e) => {
                                                e.stopPropagation();
                                                setSelectedProfessor(prof);
                                            }}>
                                                View
                                            </button>
                                        </div>
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

            {/* --- EDIT PROFESSOR MODAL --- */}
            {isEditModalOpen && (
                <div className="modal-backdrop">
                    <div className="modal">
                        <header className="modal-header">
                            <h3>Edit Professor</h3>
                            <button className="close-btn" onClick={() => setEditModalOpen(false)}>{Icon.close16}</button>
                        </header>
                        <div className="modal-body">
                            <form id="editProfListForm" onSubmit={handleEditSubmit}>
                                <div className="form-grid">
                                    {/* ID is usually not editable, so we make it disabled or read-only */}
                                    <label>
                                        <span>ID (Read Only)</span>
                                        <input
                                            value={editProfData.professorId}
                                            disabled
                                            style={{ background: '#f1f5f9', color: '#64748b' }}
                                        />
                                    </label>
                                    <label>
                                        <span>Full Name</span>
                                        <input
                                            required
                                            value={editProfData.professorName}
                                            onChange={e => setEditProfData({...editProfData, professorName: e.target.value})}
                                        />
                                    </label>
                                    <label>
                                        <span>Email</span>
                                        <input
                                            type="email"
                                            required
                                            value={editProfData.professorEmail}
                                            onChange={e => setEditProfData({...editProfData, professorEmail: e.target.value})}
                                        />
                                    </label>
                                    <label>
                                        <span>Department</span>
                                        <input
                                            required
                                            value={editProfData.professorDepartment}
                                            onChange={e => setEditProfData({...editProfData, professorDepartment: e.target.value})}
                                        />
                                    </label>
                                </div>
                            </form>
                        </div>
                        <footer className="modal-footer">
                            <button className="ghost-btn" onClick={() => setEditModalOpen(false)}>Cancel</button>
                            <button className="primary-btn" type="submit" form="editProfListForm">Save Changes</button>
                        </footer>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ProfessorsManagement;