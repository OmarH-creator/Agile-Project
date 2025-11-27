import React, { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import './AdminDashboard.css';
import umsLogo from '../../assets/UMS Logo.png';
import { curriculumData } from '../../data/CurriculumData';
import {
    courseCatalog,
    createStudent,
    deleteStudent,
    emptyStudent,
    getStudent,
    getTranscript, gradePoints,
    Icon, isPrerequisiteSatisfied, sumCredits, computeGPA, downloadBlob, buildStudentSnapshot, formatCurrency, getStandingLabel
} from './Admin-Student-Api'



// API Functions

/////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////

// Loading Spinner Component
const LoadingSpinner = ({ size = 'medium', message = 'Loading...' }) => (
    <div className={`loading-spinner ${size}`}>
        {Icon.spinner}
        {message && <span>{message}</span>}
    </div>
);

// Error Message Component
const ErrorMessage = ({ error, onDismiss }) => {
    if (!error) return null;

    return (
        <div className="error-message" role="alert">
            <div className="error-content">
                <strong>Error:</strong> {error}
            </div>
            {onDismiss && (
                <button type="button" className="ghost-btn" onClick={onDismiss} aria-label="Dismiss error">
                    {Icon.close16}
                </button>
            )}
        </div>
    );
};

const AdminDashboard = () => {
    // Initialize with empty array - students will be loaded from backend
    const [students, setStudents] = useState([]);
    const [selectedCode, setSelectedCode] = useState(null);
    const [filters, setFilters] = useState({ query: '', status: 'all', major: 'all' });
    const [formMode, setFormMode] = useState(null);
    const [formData, setFormData] = useState(emptyStudent);
    const [validationError, setValidationError] = useState('');
    const [pendingRegistration, setPendingRegistration] = useState([]);
    const [notesDraft, setNotesDraft] = useState('');
    const [view, setView] = useState('home');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [transcriptModal, setTranscriptModal] = useState({ open: false, content: '', loading: false });
    const [initialLoading, setInitialLoading] = useState(true);
    const isStudentView = view === 'students';

    // Clear error when user retries operation
    const clearError = () => setError(null);


    const tiles = [
        {
            title: 'Student Records',
            sub: 'Manage enrollment, transcripts, and holds',
            icon: Icon.student,
            accent: 'sunrise',
            path: '/Admin/Students'
        },
        {
            title: 'Facilities & Halls',
            sub: 'Schedule maintenance and reservations',
            icon: Icon.facilities,
            accent: 'aqua',
            path: '/Admin/Facilities'
        },
        {
            title: 'Curriculum',
            sub: 'Review course structure and prerequisites',
            icon: Icon.analytics,
            accent: 'violet',
            path: '/Admin/Curriculum'
        },
        {
            title: 'Requests & Approvals',
            sub: 'Track pending requests and actions',
            icon: Icon.requests,
            accent: 'citrus',
            path: '/Admin/Requests'
        }
    ];
    const selectedStudent = useMemo(
        () => students.find((student) => student.code === selectedCode) ?? null,
        [students, selectedCode]
    );

    useEffect(() => {
        if (!selectedStudent && students.length > 0) {
            setSelectedCode(students[0].code);
        }
    }, [selectedStudent, students]);

    useEffect(() => {
        setPendingRegistration([]);
    }, [selectedCode]);

    useEffect(() => {
        setNotesDraft(selectedStudent?.notes ?? '');
    }, [selectedStudent]);

    const filteredStudents = useMemo(() => {
        const query = filters.query.trim().toLowerCase();
        return students.filter((student) => {
            const matchesQuery =
                query.length === 0 ||
                [student.name, student.code, student.majorName]
                    .filter(Boolean)
                    .some((value) => value.toLowerCase().includes(query));

            const matchesStatus =
                filters.status === 'all' || student.status === filters.status;
            const matchesMajor =
                filters.major === 'all' || student.majorName === filters.major;

            return matchesQuery && matchesStatus && matchesMajor;
        });
    }, [students, filters]);

    const majorOptions = useMemo(
        () =>
            Array.from(
                new Set(students.map((student) => student.majorName).filter(Boolean))
            ).sort(),
        [students]
    );

    const availableCourses = useMemo(() => {
        if (!selectedStudent) {
            return [];
        }

        const completedCodes = new Set(
            selectedStudent.academicHistory
                .filter((record) => (gradePoints[record.grade] ?? 0) > 0)
                .map((record) => record.code)
        );

        const completedHours =
            selectedStudent.completedHours ?? sumCredits(selectedStudent.academicHistory);

        const registeredCodes = new Set(
            (selectedStudent.currentRegistrations ?? []).map((course) => course.code)
        );

        const pendingCodes = new Set(pendingRegistration.map((course) => course.code));

        return courseCatalog
            .map((course) => {
                const prerequisites = course.prerequisites ?? [];
                const unsatisfied = prerequisites.filter(
                    (pr) => !isPrerequisiteSatisfied(pr, completedCodes, completedHours)
                );
                const requiresManual = prerequisites.some(
                    (pr) => pr.trim().toLowerCase() === 'varies'
                );
                const alreadyCompleted = completedCodes.has(course.code);
                const alreadyRegistered =
                    registeredCodes.has(course.code) || pendingCodes.has(course.code);

                return {
                    ...course,
                    creditHours: course.creditHours ?? 3,
                    prerequisites,
                    unsatisfied,
                    requiresManual,
                    alreadyCompleted,
                    alreadyRegistered
                };
            })
            .filter(
                (course) =>
                    !course.alreadyCompleted &&
                    !course.alreadyRegistered &&
                    course.unsatisfied.length === 0
            )
            .sort(
                (a, b) =>
                    (a.semester ?? 0) - (b.semester ?? 0) ||
                    a.code.localeCompare(b.code)
            );
    }, [selectedStudent, pendingRegistration]);

    const dynamicGpa = useMemo(
        () => (selectedStudent ? computeGPA(selectedStudent.academicHistory) : null),
        [selectedStudent]
    );

    const completedHoursValue = useMemo(
        () =>
            selectedStudent
                ? selectedStudent.completedHours ??
                sumCredits(selectedStudent.academicHistory)
                : 0,
        [selectedStudent]
    );

    const registeredCredits = useMemo(
        () =>
            selectedStudent
                ? (selectedStudent.currentRegistrations ?? []).reduce(
                    (total, course) => total + (course.credits ?? 3),
                    0
                )
                : 0,
        [selectedStudent]
    );

    const openAddStudent = () => {
        setFormData(emptyStudent);
        setFormMode('add');
        setValidationError('');
    };

    const openEditStudent = (student) => {
        const gpaSnapshot = computeGPA(student.academicHistory);
        setFormData({
            ...student,
            cgpa: (gpaSnapshot ?? student.cgpa ?? 0).toFixed(2),
            completedHours:
                student.completedHours ?? sumCredits(student.academicHistory),
            gradYear: student.gradYear ?? '',
            fees: student.fees ?? 0
        });
        setFormMode('edit');
        setValidationError('');
    };

    const closeForm = () => {
        setFormMode(null);
        setFormData(emptyStudent);
        setValidationError('');
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
            if (students.some((student) => student.code === normalized.code)) {
                setValidationError('Student code already exists.');
                return;
            }

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

                // Update local state only after successful API call
                setStudents((prev) => [...prev, newStudent]);
                setSelectedCode(newStudent.code);
                setNotesDraft(newStudent.notes);
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

        if (formMode === 'edit' && selectedStudent) {
            // NOTE: Edit mode updates local state only.
            // The backend Admin Controller does not currently provide a PUT endpoint for updating student records.
            // Once a PUT endpoint is implemented (e.g., PUT /api/admin/students/{studentId}),
            // this section should be updated to call an updateStudent API function similar to createStudent.
            // Until then, edits are maintained in the frontend state only and will not persist across sessions.

            setStudents((prev) =>
                prev.map((student) =>
                    student.code === selectedStudent.code
                        ? {
                            ...student,
                            ...normalized,
                            code: student.code,
                            academicHistory: student.academicHistory,
                            currentRegistrations:
                                student.currentRegistrations ?? [],
                            holds: student.holds ?? []
                        }
                        : student
                )
            );
            setNotesDraft(normalized.notes);
            closeForm();
        }
    };

    const handleSelectStudent = async (studentId) => {
        // If already selected, no need to fetch again
        if (studentId === selectedCode) {
            return;
        }

        // Clear error when user retries operation
        clearError();
        setLoading(true);

        try {
            const fetchedStudent = await getStudent(studentId);

            // Update the students array with the fetched data
            setStudents((prev) => {
                const existingIndex = prev.findIndex(s => s.code === studentId);
                if (existingIndex >= 0) {
                    // Replace existing student with fetched data
                    const updated = [...prev];
                    updated[existingIndex] = fetchedStudent;
                    return updated;
                } else {
                    // Add new student if not in local state
                    return [...prev, fetchedStudent];
                }
            });

            // Set as selected
            setSelectedCode(studentId);
        } catch (err) {
            const errorMessage = err.message || 'Failed to fetch student details';
            setError(errorMessage);
        } finally {
            setLoading(false);
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

            // Update local students state only after successful deletion
            setStudents((prev) => prev.filter((student) => student.code !== code));

            // If the deleted student was selected, clear selection
            if (selectedCode === code) {
                setSelectedCode(students.length > 1 ? students.find(s => s.code !== code)?.code : null);
            }
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

    const handleAddCourse = (course) => {
        setPendingRegistration((prev) =>
            prev.some((item) => item.code === course.code) ? prev : [...prev, course]
        );
    };

    const handleRemovePending = (code) => {
        setPendingRegistration((prev) =>
            prev.filter((course) => course.code !== code)
        );
    };

    const handleDropCourse = (code) => {
        if (!selectedStudent) return;

        setStudents((prev) =>
            prev.map((student) =>
                student.code === selectedStudent.code
                    ? {
                        ...student,
                        currentRegistrations: (student.currentRegistrations ?? []).filter(
                            (course) => course.code !== code
                        )
                    }
                    : student
            )
        );
    };

    const confirmRegistration = () => {
        if (!selectedStudent || pendingRegistration.length === 0) {
            return;
        }

        setStudents((prev) =>
            prev.map((student) =>
                student.code === selectedStudent.code
                    ? {
                        ...student,
                        currentRegistrations: [
                            ...(student.currentRegistrations ?? []),
                            ...pendingRegistration.map((course) => ({
                                code: course.code,
                                title: course.title,
                                credits: course.creditHours ?? 3
                            }))
                        ]
                    }
                    : student
            )
        );
        setPendingRegistration([]);
    };

    const handleNotesBlur = () => {
        if (!selectedStudent) return;

        setStudents((prev) =>
            prev.map((student) =>
                student.code === selectedStudent.code
                    ? { ...student, notes: notesDraft.trim() }
                    : student
            )
        );
    };

    const handleGenerateTranscript = async () => {
        if (!selectedStudent) return;

        // Clear error when user retries operation
        clearError();
        setTranscriptModal({ open: true, content: '', loading: true });

        try {
            const transcriptText = await getTranscript(selectedStudent.code);
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
        if (!selectedStudent || !transcriptModal.content) return;

        downloadBlob(
            transcriptModal.content,
            `${selectedStudent.code}-transcript.txt`,
            'text/plain'
        );
    };

    const studentGpaDisplay = (student) => {
        const snapshot = computeGPA(student.academicHistory);
        const fallback = typeof student.cgpa === 'number' ? student.cgpa : null;

        if (snapshot !== null) {
            return snapshot.toFixed(2);
        }

        if (fallback !== null) {
            return fallback.toFixed(2);
        }

        return 'N/A';
    };

    return (
        <div className="shell">
            <header className="topbar" role="banner">
                <div className="topbar-left">
                    <button className="icon-btn" aria-label="Menu" title="Menu">
                        {Icon.menu16}
                    </button>
                    <div className="brand-mini">
                        <div className="brand-logo-shell">
                            <img src={String(umsLogo)} alt="UMS logo" className="mini-logo" />
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
            {/*// header section end*/}

            {/*/////////////////////////////////////////////////////////////////////////////////////////////////*/}
            {/*/////////////////////////////////////////////////////////////////////////////////////////////////*/}

            <main className="main" role="main">
                <div className="page-header">
                    <div className="page-title">
                        <span className="page-title-ico">{Icon.home16}</span>
                        <div>
                            <h2>{isStudentView ? 'Student Management' : 'Dashboard'}</h2>
                            <p className="page-sub">
                                {isStudentView
                                    ? 'Review records, manage enrollment, and export documents.'
                                    : 'Welcome to the University Management System'}
                            </p>
                        </div>
                    </div>

                </div>


                {!isStudentView && (
                    <div className="grid">
                        {tiles.map((tile) => (
                                <Link
                                    to={tile.path ?? '#'}
                                    key={tile.title}
                                    className="card"
                                    data-accent={tile.accent}
                                >
                                    <div className="card-title">
                                        <div>
                                            <div className="card-heading">{tile.title}</div>
                                            {tile.sub && <p className="card-sub">{tile.sub}</p>}
                                        </div>
                                        {tile.icon && (
                                            <div className="card-icon" aria-hidden="true" data-accent={tile.accent}>
                                                {tile.icon}
                                            </div>
                                        )}
                                    </div>
                                </Link>
                            )
                        )}
                    </div>
                )}
            </main>

            {/*{formMode && (*/}
            {/*    <div className="modal-backdrop">*/}
            {/*        <div className="modal">*/}
            {/*            /!* HEADER *!/*/}
            {/*            <header className="modal-header">*/}
            {/*                <h3>{formMode === 'add' ? 'Add Student' : 'Edit Student'}</h3>*/}
            {/*                <button type="button" className="close-btn" onClick={closeForm}>*/}
            {/*                    {Icon.close16}*/}
            {/*                </button>*/}
            {/*            </header>*/}

            {/*            /!* BODY (Scrollable) *!/*/}
            {/*            <div className="modal-body">*/}
            {/*                <form id="studentForm" onSubmit={handleFormSubmit}>*/}
            {/*                    <div className="form-grid">*/}
            {/*                        <label>*/}
            {/*                            <span>Student Code</span>*/}
            {/*                            <input name="code" value={formData.code} onChange={handleFormChange} required disabled={formMode === 'edit'} />*/}
            {/*                        </label>*/}
            {/*                        <label>*/}
            {/*                            <span>Full Name</span>*/}
            {/*                            <input name="name" value={formData.name} onChange={handleFormChange} required />*/}
            {/*                        </label>*/}
            {/*                        <label>*/}
            {/*                            <span>Email</span>*/}
            {/*                            <input type="email" name="email" value={formData.email} onChange={handleFormChange} />*/}
            {/*                        </label>*/}
            {/*                        <label>*/}
            {/*                            <span>Phone</span>*/}
            {/*                            <input name="phone" value={formData.phone} onChange={handleFormChange} />*/}
            {/*                        </label>*/}
            {/*                        <label>*/}
            {/*                            <span>Major</span>*/}
            {/*                            <input name="majorName" value={formData.majorName} onChange={handleFormChange} />*/}
            {/*                        </label>*/}
            {/*                        <label>*/}
            {/*                            <span>Major ID</span>*/}
            {/*                            <input name="majorId" value={formData.majorId} onChange={handleFormChange} />*/}
            {/*                        </label>*/}
            {/*                        <label>*/}
            {/*                            <span>Status</span>*/}
            {/*                            <select name="status" value={formData.status} onChange={handleFormChange}>*/}
            {/*                                <option value="Active">Active</option>*/}
            {/*                                <option value="Probation">Probation</option>*/}
            {/*                                <option value="Suspended">Suspended</option>*/}
            {/*                                <option value="Graduated">Graduated</option>*/}
            {/*                            </select>*/}
            {/*                        </label>*/}
            {/*                        <label>*/}
            {/*                            <span>CGPA</span>*/}
            {/*                            <input name="cgpa" value={formData.cgpa} onChange={handleFormChange} />*/}
            {/*                        </label>*/}
            {/*                        <label>*/}
            {/*                            <span>Hours</span>*/}
            {/*                            <input name="completedHours" value={formData.completedHours} onChange={handleFormChange} />*/}
            {/*                        </label>*/}
            {/*                        <label>*/}
            {/*                            <span>Fees</span>*/}
            {/*                            <input name="fees" value={formData.fees} onChange={handleFormChange} />*/}
            {/*                        </label>*/}
            {/*                        <label>*/}
            {/*                            <span>Grad Year</span>*/}
            {/*                            <input name="gradYear" value={formData.gradYear} onChange={handleFormChange} />*/}
            {/*                        </label>*/}
            {/*                        <label>*/}
            {/*                            <span>Military Status</span>*/}
            {/*                            <input name="militaryStatus" value={formData.militaryStatus} onChange={handleFormChange} />*/}
            {/*                        </label>*/}
            {/*                        <label className="full">*/}
            {/*                            <span>Address</span>*/}
            {/*                            <input name="address" value={formData.address} onChange={handleFormChange} />*/}
            {/*                        </label>*/}
            {/*                        <label className="full">*/}
            {/*                            <span>Notes</span>*/}
            {/*                            <textarea name="notes" value={formData.notes} onChange={handleFormChange} />*/}
            {/*                        </label>*/}
            {/*                    </div>*/}
            {/*                </form>*/}
            {/*                {validationError && (*/}
            {/*                    <p className="form-error" style={{ marginTop: '1rem' }}>{validationError}</p>*/}
            {/*                )}*/}
            {/*            </div>*/}

            {/*            /!* FOOTER (Fixed at bottom) *!/*/}
            {/*            <footer className="modal-footer">*/}
            {/*                <button type="button" className="ghost-btn" onClick={closeForm} disabled={loading}>*/}
            {/*                    Cancel*/}
            {/*                </button>*/}
            {/*                <button*/}
            {/*                    type="submit"*/}
            {/*                    form="studentForm"*/}
            {/*                    className="primary-btn"*/}
            {/*                    disabled={loading}*/}
            {/*                >*/}
            {/*                    {loading ? Icon.spinner : Icon.check16}*/}
            {/*                    <span>{loading ? 'Saving...' : 'Save'}</span>*/}
            {/*                </button>*/}
            {/*            </footer>*/}
            {/*        </div>*/}
            {/*    </div>*/}
            {/*)}*/}

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

export default AdminDashboard;
