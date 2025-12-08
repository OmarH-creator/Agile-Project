import React, { useState, useEffect } from 'react';
import StudentRecord from './StudentRecord'; // Visualization Component
import {
    getAllStudents,
    createStudent, // IMPORTED
    updateStudent, // IMPORTED
    deleteStudent, // IMPORTED
    emptyStudent,
} from './Admin-Student-Api';
import { Icon } from './Admin-Student-Api';
import './StudentServices.css'; // Ensure you have the CSS from previous steps
import umsLogo from "../../assets/UMS Logo.png";

const StudentServices = () => {
    // --- STATE ---
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [studentToDelete, setStudentToDelete] = useState(null);
    const [students, setStudents] = useState([]);
    const [searchQuery, setSearchQuery] = useState('');
    const [selectedStudent, setSelectedStudent] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    // Form State (View | Edit | Add)
    const [formMode, setFormMode] = useState('view');
    const [formData, setFormData] = useState(emptyStudent);

    // Pagination
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const pageSize = 10;

    // --- INITIAL LOAD ---
    useEffect(() => {
        triggerSearch(0, '');
    }, []);

    // --- API & LIST HANDLING ---
    const triggerSearch = async (page, query) => {
        setLoading(true);
        try {
            const data = await getAllStudents(page, pageSize, query);
            setStudents(data.content);
            setTotalPages(data.totalPages);
        } catch (err) {
            console.error(err);
            setError("Failed to load students.");
        } finally {
            setLoading(false);
        }
    };

    // --- VIEW HANDLERS ---
    const handleInputChange = (e) => setSearchQuery(e.target.value);

    const handleKeyDown = (e) => {
        if (e.key === 'Enter') {
            setCurrentPage(0);
            triggerSearch(0, searchQuery);
        }
    };

    const handleSearchClick = () => {
        setCurrentPage(0);
        triggerSearch(0, searchQuery);
    };

    const handlePageChange = (pageNum) => {
        setCurrentPage(pageNum);
        triggerSearch(pageNum, searchQuery);
    };

    const handleSelectStudent = (student) => {
        if (formMode !== 'view' && !window.confirm("Unsaved changes will be lost. Continue?")) return;
        setSelectedStudent(student);
        setFormMode('view');
        setError(null);
    };

    // --- ADD / EDIT / DELETE ACTIONS ---

    // 1. OPEN ADD FORM
    const handleAddClick = () => {
        setSelectedStudent(null); // Deselect current
        setFormData(emptyStudent); // Clear form
        setFormMode('add');
        setError(null);
    };

    // 2. OPEN EDIT FORM
    const handleEditClick = () => {
        if (!selectedStudent) return;

        // Prepare data: flatten nested objects if necessary for the form
        const preparedData = {
            ...selectedStudent,
            code: selectedStudent.studentId || selectedStudent.code,
            majorId: selectedStudent.major?.majorId || selectedStudent.majorId || '',
            majorName: selectedStudent.major?.majorName || selectedStudent.majorName || '',
            // Ensure numbers
            completedHours: selectedStudent.completedHours || 0,
            cgpa: selectedStudent.gpa || selectedStudent.cgpa || 0,
            fees: selectedStudent.fees || 0
        };

        setFormData(preparedData);
        setFormMode('edit');
        setError(null);
    };

    // 3. HANDLE FORM INPUT
    const handleFormChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: (name === 'code' || name === 'majorId') ? value.toUpperCase() : value
        }));
    };

    // 4. SUBMIT (CREATE OR UPDATE)
    const handleFormSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError(null);

        const payload = {
            ...formData,
            // VITAL FIX: Send 'studentId' explicitly so Spring Boot maps it to the @Id field
            studentId: formData.code ? formData.code.trim().toUpperCase() : null,

            // Map other fields
            name: formData.name.trim(),
            email: formData.email?.trim(),
            phone: formData.phone?.trim(),
            address: formData.address?.trim(),
            majorId: formData.majorId?.trim(),
            majorName: formData.majorName?.trim(),
            militaryStatus: formData.militaryStatus,
            notes: (formData.notes ?? '').trim(),

            // Ensure numbers are actual numbers
            completedHours: Number(formData.completedHours) || 0,
            fees: Number(formData.fees) || 0,
            cgpa: Number(formData.cgpa) || 0,
            gradYear: formData.gradYear ? Number(formData.gradYear) : null
        };

        try {
            if (formMode === 'add') {
                // ... rest of your add logic
                await createStudent(payload);
                alert('Student Created Successfully!');

                // Refresh list and select the new student (optional)
                triggerSearch(0, searchQuery);
                setFormMode('view');
                // Ideally, we'd select the new student here if the API returns the full object
            } else if (formMode === 'edit') {
                // --- UPDATE ---
                const id = formData.studentId || formData.code;
                await updateStudent(id, payload);

                // Optimistically update local list
                const updatedList = students.map(s =>
                    (s.studentId === id || s.code === id) ? { ...s, ...payload } : s
                );
                setStudents(updatedList);
                setSelectedStudent({ ...selectedStudent, ...payload });

                alert('Student Updated Successfully!');
                setFormMode('view');
            }
        } catch (err) {
            console.error(err);
            setError(err.message || "Operation failed.");
        } finally {
            setLoading(false);
        }
    };

    // 5. DELETE
    const handleDeleteClick = () => {
        if (!selectedStudent) return;
        setStudentToDelete(selectedStudent);
        setShowDeleteModal(true);
    };

    const handleConfirmDelete = async () => {
        if (!studentToDelete) return;

        const id = studentToDelete.studentId || studentToDelete.code;
        setLoading(true);
        setError(null);

        try {
            await deleteStudent(id);

            // Remove from local list
            setStudents(prev => prev.filter(s =>
                s.studentId !== id && s.code !== id
            ));

            // Clear selection if we deleted the selected student
            if (selectedStudent && (selectedStudent.studentId === id || selectedStudent.code === id)) {
                setSelectedStudent(null);
                setFormMode('view');
            }



        } catch (err) {
            setError(err.message || 'Failed to delete student');
        } finally {
            setLoading(false);
            setShowDeleteModal(false);
            setStudentToDelete(null);
        }
    };

    const handleCancelDelete = () => {
        setShowDeleteModal(false);
        setStudentToDelete(null);
    };

    const handleCancelForm = () => {
        if (selectedStudent) {
            setFormMode('view');
        } else {
            // If we were adding and canceled, just go back to empty view
            setFormMode('view');
            setSelectedStudent(null);
        }
        setError(null);
    };

    return (
        <div className="shell">
            <header className="topbar" role="banner">
                <div className="topbar-left">
                    <div className="brand-mini">
                        <div className="brand-logo-shell"><img src={String(umsLogo)} alt="UMS" className="mini-logo"/></div>
                        <span className="brand-text brand-title">Admin Console</span>
                    </div>
                </div>
            </header>

            <main className="main">
                <div className="page-header">
                    <h2>Student Services</h2>
                </div>

                <section className="dashboard-content">
                    {/* --- LEFT PANEL: LIST --- */}
                    <aside className="student-panel">
                        <header className="student-panel-header">
                            <div className="student-panel-top">
                                <h3>Students</h3>
                                <button className="primary-btn" onClick={handleAddClick}>
                                    {Icon.plus16 || '+'} Add
                                </button>
                            </div>
                            <div className="student-filters">
                                <div className="search-group">
                                    <input
                                        type="search"
                                        placeholder="Filter by ID..."
                                        value={searchQuery}
                                        onChange={handleInputChange}
                                        onKeyDown={handleKeyDown}
                                        className="search-input"
                                    />
                                    <button className="search-btn" onClick={handleSearchClick}>
                                        {Icon.search16}
                                    </button>
                                </div>
                            </div>
                        </header>

                        <div className="student-list">
                            {loading && students.length === 0 ? (
                                <div style={{padding: '20px', textAlign:'center'}}>Loading...</div>
                            ) : students.length > 0 ? (
                                students.map(student => (
                                    <button
                                        key={student.studentId || student.code}
                                        className={`student-card ${selectedStudent?.studentId === student.studentId ? 'active' : ''}`}
                                        onClick={() => handleSelectStudent(student)}
                                    >
                                        <div className="student-card-main">
                                            <h4>{student.name}</h4>
                                            <span className="student-code">{student.studentId || student.code}</span>
                                        </div>
                                    </button>
                                ))
                            ) : (
                                <div style={{padding: '1rem', color: '#666', textAlign:'center'}}>No students found</div>
                            )}
                        </div>

                        <div className="pagination-footer">
                            <button disabled={currentPage === 0} onClick={() => handlePageChange(currentPage - 1)} className="page-btn">&lt;</button>
                            <span>{currentPage + 1} / {totalPages || 1}</span>
                            <button disabled={currentPage >= totalPages - 1} onClick={() => handlePageChange(currentPage + 1)} className="page-btn">&gt;</button>
                        </div>
                    </aside>

                    {/* --- RIGHT PANEL: DETAILS OR FORM --- */}
                    <section className="detail-panel">
                        {error && <div className="error-banner" style={{background: '#ffebee', color: '#c62828', padding: '10px', marginBottom: '10px', borderRadius: '4px'}}>{error}</div>}

                        {/* CASE 1: NO SELECTION & NOT ADDING */}
                        {!selectedStudent && formMode === 'view' ? (
                            <div className="empty-state large">
                                <div style={{marginBottom:'10px', fontSize:'2rem', color:'#ccc'}}>{Icon.user || '👤'}</div>
                                Select a student to view details or click "Add" to create a new one.
                            </div>
                        ) : formMode === 'view' ? (

                            // --- CASE 2: VIEW MODE (Uses StudentRecord Component) ---
                            <div className="view-container">
                                <div className="action-toolbar" style={{display: 'flex', gap: '10px', marginBottom: '20px', justifyContent: 'flex-end'}}>
                                    <button className="secondary-btn" onClick={handleEditClick}>
                                        {Icon.edit16} Edit Record
                                    </button>
                                    <button className="danger-btn" style={{background: '#fee', color: 'red', border: '1px solid #fdd', padding: '8px 12px', borderRadius:'4px', cursor: 'pointer'}} onClick={handleDeleteClick}>
                                        {Icon.trash16} Delete
                                    </button>
                                </div>
                                <StudentRecord student={selectedStudent}/>
                            </div>

                        ) : (

                            // --- CASE 3: ADD / EDIT FORM MODE ---
                            <div className="edit-form-container" style={{padding: '30px', background: 'white', borderRadius: '8px', boxShadow: '0 4px 12px rgba(0,0,0,0.05)'}}>
                                <h3 style={{borderBottom:'1px solid #eee', paddingBottom:'15px', marginBottom:'20px'}}>
                                    {formMode === 'add' ? 'Add New Student' : `Edit Student: ${formData.studentId || formData.code}`}
                                </h3>

                                <form onSubmit={handleFormSubmit} className="student-form">
                                    <div className="form-grid" style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap:'20px'}}>

                                        {/* Row 1 */}
                                        <div className="form-group">
                                            <label>Student ID / Code</label>
                                            <input
                                                type="text"
                                                name="code"
                                                value={formData.code || ''}
                                                onChange={handleFormChange}
                                                required
                                                disabled={formMode === 'edit'} // ID usually immutable on edit
                                                placeholder="e.g. 2023001"
                                            />
                                        </div>

                                        <div className="form-group">
                                            <label>Full Name</label>
                                            <input type="text" name="name" value={formData.name || ''} onChange={handleFormChange} required />
                                        </div>

                                        {/* Row 2 */}
                                        <div className="form-group">
                                            <label>Email</label>
                                            <input type="email" name="email" value={formData.email || ''} onChange={handleFormChange} required />
                                        </div>

                                        <div className="form-group">
                                            <label>Phone</label>
                                            <input type="text" name="phone" value={formData.phone || ''} onChange={handleFormChange} />
                                        </div>

                                        {/* Row 3 - Academic */}
                                        <div className="form-group">
                                            <label>Major ID</label>
                                            <input
                                                type="text"
                                                name="majorId"
                                                value={formData.majorId || ''}
                                                onChange={handleFormChange}
                                                placeholder="e.g. CS-001"
                                                required
                                            />
                                        </div>

                                        <div className="form-group">
                                            <label>Major Name</label>
                                            <input name="majorName" value={formData.majorName || ''} onChange={handleFormChange} />
                                        </div>

                                        {/* Row 4 - Status */}
                                        <div className="form-group">
                                            <label>Status</label>
                                            <select name="status" value={formData.status || 'Active'} onChange={handleFormChange}>
                                                <option value="Active">Active</option>
                                                <option value="Probation">Probation</option>
                                                <option value="Suspended">Suspended</option>
                                                <option value="Graduated">Graduated</option>
                                            </select>
                                        </div>

                                        <div className="form-group">
                                            <label>Military Status</label>
                                            <select name="militaryStatus" value={formData.militaryStatus || ''} onChange={handleFormChange}>
                                                <option value="">Select...</option>
                                                <option value="Exempted">Exempted</option>
                                                <option value="Completed">Completed</option>
                                                <option value="Postponed">Postponed</option>
                                                <option value="Not Applicable">Not Applicable</option>
                                            </select>
                                        </div>

                                        {/* Row 5 - Numeric */}
                                        <div className="form-group">
                                            <label>CGPA</label>
                                            <input name="cgpa" type="number" step="0.01" value={formData.cgpa || 0} onChange={handleFormChange} />
                                        </div>

                                        <div className="form-group">
                                            <label>Completed Hours</label>
                                            <input name="completedHours" type="number" value={formData.completedHours || 0} onChange={handleFormChange} />
                                        </div>

                                        <div className="form-group full-width" style={{gridColumn:'1 / -1'}}>
                                            <label>Address</label>
                                            <input type="text" name="address" value={formData.address || ''} onChange={handleFormChange} />
                                        </div>

                                        <div className="form-group full-width" style={{gridColumn:'1 / -1'}}>
                                            <label>Notes</label>
                                            <textarea name="notes" value={formData.notes || ''} onChange={handleFormChange} rows="3" />
                                        </div>

                                    </div>

                                    <div className="form-actions" style={{marginTop: '30px', display: 'flex', gap: '15px', justifyContent:'flex-end', borderTop:'1px solid #eee', paddingTop:'20px'}}>
                                        <button type="button" className="ghost-btn" onClick={handleCancelForm} disabled={loading}>
                                            Cancel
                                        </button>
                                        <button type="submit" className="primary-btn" disabled={loading}>
                                            {loading ? (Icon.spinner || '...') : (Icon.check16 || 'Save')}
                                            <span>{loading ? ' Saving...' : ' Save Record'}</span>
                                        </button>
                                    </div>
                                </form>
                            </div>
                        )}
                        {/* ... all your existing JSX ... */}

                        {/* Delete Confirmation Modal */}
                        {showDeleteModal && studentToDelete && (
                            <div className="modal-backdrop" onClick={handleCancelDelete}>
                                <div className="modal" onClick={(e) => e.stopPropagation()}>
                                    <div className="modal-header">
                                        <h3>Confirm Deletion</h3>
                                    </div>
                                    <div className="modal-body">
                                        <p>Are you sure you want to delete <strong>{studentToDelete.name}</strong> ({studentToDelete.studentId || studentToDelete.code})?</p>
                                        <p style={{color: '#d24c5f', marginTop: '10px'}}>This action cannot be undone.</p>
                                    </div>
                                    <div className="modal-footer">
                                        <button
                                            className="ghost-btn"
                                            onClick={handleCancelDelete}
                                            disabled={loading}
                                        >
                                            Cancel
                                        </button>
                                        <button
                                            className="primary-btn danger"
                                            onClick={handleConfirmDelete}
                                            disabled={loading}
                                            style={{background: '#d24c5f'}}
                                        >
                                            {loading ? 'Deleting...' : 'Delete Student'}
                                        </button>
                                    </div>
                                </div>
                            </div>
                        )}
                    </section>
                </section>
            </main>
        </div>
    );
};

export default StudentServices;