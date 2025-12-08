import React, { useState, useEffect } from 'react'; // Added useEffect
import { Link } from 'react-router-dom';
import StudentRecord from './StudentRecord'; // Importing the child component
import ConfirmModal, {
    getStudent,
    getAllStudents,
    updateStudent, // IMPORTED
    deleteStudent, // IMPORTED
    getTranscript,
    downloadBlob,
    emptyStudent,
    sumCredits, createStudent, buildStudentSnapshot
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
    const [validationError, setValidationError] = useState('');
    const [selectedCode, setSelectedCode] = useState(null);
    const [filters, setFilters] = useState({ query: '', status: 'all', major: 'all' });
    const [pendingRegistration, setPendingRegistration] = useState([]);
    const [notesDraft, setNotesDraft] = useState('');

    // Edit/Form State
    const [formMode, setFormMode] = useState('view'); // 'view', 'edit'
    const [formData, setFormData] = useState(emptyStudent);

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

    // --- EDIT & UPDATE HANDLERS (NEW) ---

    const handleSelectStudent = (student) => {
        setSelectedStudent(student);
        setFormMode('view'); // Always reset to view when clicking a new student
        setError(null);
    };

    const handleEditClick = () => {
        // Prepare data for the form
        // Backend expects 'majorId', but raw student data might have a nested major object
        const preparedData = {
            ...selectedStudent,
            // Safety check: if major is an object, get id, else assume it's missing
            majorId: selectedStudent.major?.majorId || '',
            majorName: selectedStudent.major?.majorName || ''
        };
        setFormData(preparedData);
        setFormMode('edit');
    };

    const handleFormChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleCancelEdit = () => {
        setFormMode('view');
        setError(null);
    };

    const handleSaveUpdate = async (e) => {
        e.preventDefault();
        //setLoading(true);
        //setError(null);

        try {
            // Call API
            await updateStudent(formData.studentId, formData);

            // Update local list to reflect changes immediately
            const updatedStudents = students.map(s =>
                s.studentId === formData.studentId ? { ...s, ...formData } : s
            );
            setStudents(updatedStudents);

            // Update selected view
            setSelectedStudent({ ...selectedStudent, ...formData });

            setFormMode('view');
            alert('Student updated successfully!');
        } catch (err) {
            console.error(err);
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };
    const [confirmOpen, setConfirmOpen] = useState(false);
    const handleDeleteClick = () => {
        setConfirmOpen(true);
    };
    const confirmDelete = async () => {
        try {
            await deleteStudent(selectedStudent.studentId);

            setStudents(prev => prev.filter(
                s => s.studentId !== selectedStudent.studentId
            ));

            setSelectedStudent(null);
            setFormMode('view');
        } catch (err) {
            alert(err.message);
        } finally {
            setConfirmOpen(false);
        }
    };



    {/*const handleDeleteClick = async () => {
        if(!window.confirm(`Are you sure you want to delete ${selectedStudent.name}?`)) return;

        try {
            await deleteStudent(selectedStudent.studentId);
            // Remove from list
            setStudents(prev => prev.filter(s => s.studentId !== selectedStudent.studentId));
            setSelectedStudent(null);
            setFormMode('view');
        } catch (err) {
            alert(err.message);
        }
    };*/}

    useEffect(() => {
        if (students.length === 0) {
            setSelectedStudent(null);
        }
    }, [students]);

    return (
        <div className="shell">
            <header className="topbar" role="banner">
                {/* ... Topbar code remains the same ... */}
                <div className="topbar-left">
                    <button className="icon-btn">{Icon.menu16}</button>
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
                    {/* LEFT PANEL: LIST */}
                    <aside className="student-panel">
                        <header className="student-panel-header">
                            <div className="student-panel-top">
                                <h3>Students</h3>
                                <button className="primary-btn">{Icon.plus16} Add</button>
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
                                <div style={{padding: '20px'}}>Loading...</div>
                            ) : students.length > 0 ? (
                                students.map(student => (
                                    <button
                                        key={student.studentId}
                                        className={`student-card ${selectedStudent?.studentId === student.studentId ? 'active' : ''}`}
                                        onClick={() => handleSelectStudent(student)}
                                    >
                                        <div className="student-card-main">
                                            <h4>{student.name}</h4>
                                            <span className="student-code">{student.studentId}</span>
                                        </div>
                                    </button>
                                ))
                            ) : (
                                <div style={{padding: '1rem', color: '#666'}}>No students found</div>
                            )}
                        </div>

                        {/* Pagination Footer remains the same */}
                        <div className="pagination-footer">
                            <button disabled={currentPage === 0} onClick={() => handlePageChange(currentPage - 1)} className="page-btn">&lt;</button>
                            <span>{currentPage + 1} / {totalPages || 1}</span>
                            <button disabled={currentPage >= totalPages - 1} onClick={() => handlePageChange(currentPage + 1)} className="page-btn">&gt;</button>
                        </div>
                    </aside>

                    {/* RIGHT PANEL: DETAIL OR EDIT FORM */}
                    <section className="detail-panel">
                        {error && <div className="error-banner" style={{background: '#ffebee', color: '#c62828', padding: '10px', marginBottom: '10px', borderRadius: '4px'}}>{error}</div>}

                        {!selectedStudent ? (
                            <div className="empty-state large">Select a student</div>
                        ) : formMode === 'view' ? (
                            // --- VIEW MODE ---
                            <div className="view-container">
                                {/* Toolbar for View Mode */}
                                <div className="action-toolbar" style={{display: 'flex', gap: '10px', marginBottom: '20px', justifyContent: 'flex-end'}}>
                                    <button className="secondary-btn" onClick={handleEditClick}>
                                        {Icon.edit16} Edit
                                    </button>
                                    <button className="danger-btn" style={{background: '#fee', color: 'red', border: 'none', padding: '8px 12px', borderRadius:'4px', cursor: 'pointer'}} onClick={handleDeleteClick}>
                                        {Icon.trash16} Delete
                                    </button>
                                </div>
                                {/* Use the existing component */}
                                <StudentRecord student={selectedStudent}/>
                            </div>
                        ) : (
                            // --- EDIT MODE ---
                            <div className="edit-form-container" style={{padding: '20px', background: 'white', borderRadius: '8px', boxShadow: '0 2px 10px rgba(0,0,0,0.05)'}}>
                                <h3>Edit Student: {formData.studentId}</h3>
                                <form onSubmit={handleSaveUpdate} className="student-form">

                                    <div className="form-group">
                                        <label>Full Name</label>
                                        <input type="text" name="name" value={formData.name || ''} onChange={handleFormChange} required />
                                    </div>

                                    <div className="form-group">
                                        <label>Email</label>
                                        <input type="email" name="email" value={formData.email || ''} onChange={handleFormChange} required />
                                    </div>

                                    <div className="form-group">
                                        <label>Phone</label>
                                        <input type="text" name="phone" value={formData.phone || ''} onChange={handleFormChange} />
                                    </div>

                                    <div className="form-group">
                                        <label>Address</label>
                                        <input type="text" name="address" value={formData.address || ''} onChange={handleFormChange} />
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

                                    <div className="form-group">
                                        <label>Major ID (Required)</label>
                                        <input
                                            type="text"
                                            name="majorId"
                                            value={formData.majorId || ''}
                                            onChange={handleFormChange}
                                            placeholder="e.g. CS-001"
                                            required
                                        />
                                        <small style={{color: '#666'}}>Must match an existing Major ID in the database.</small>
                                    </div>

                                    <div className="form-actions" style={{marginTop: '20px', display: 'flex', gap: '10px'}}>
                                        <button type="submit" className="primary-btn" disabled={loading}>
                                            {loading ? 'Saving...' : 'Save Changes'}
                                        </button>
                                        <button type="button" className="ghost-btn" onClick={handleCancelEdit}>
                                            Cancel
                                        </button>


                                    </div>
                                </form>
                            </div>
                        )}
                    </section>
                </section>
            </main>
        </div>
    );
};

export default StudentServices;