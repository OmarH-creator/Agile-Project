import React, { useMemo, useState, useEffect } from 'react';
import toast from 'react-hot-toast';
import './Curriculum.css';
import umsLogo from '../../assets/UMS Logo.png';
import { Link } from 'react-router-dom';
import { getAllCourses, addCourse, updatePrerequisites } from '../../api/CoursesApi';

const NOTES = [
    'CSE491 requires standing (>=130 CH) rather than a specific course.',
    "Electives (EL3/EL4) show 'varies' because prerequisites depend on the chosen course outline."
];
const VERSION = 'CESS-bylaw-2018';

const escapeRegExp = (string) => string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');



const Curriculum = () => {
    const [search, setSearch] = useState('');
    const [semesterFilter, setSemesterFilter] = useState('All');
    const [selectedCourse, setSelectedCourse] = useState(null);
    const [courses, setCourses] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // Add Course State
    const [showAddModal, setShowAddModal] = useState(false);
    const [newCourse, setNewCourse] = useState({
        code: '',
        title: '',
        creditHours: 3,
        semester: 3,
        prerequisites: []
    });
    const [newPrereqCode, setNewPrereqCode] = useState('');

    useEffect(() => {
        const fetchCourses = async () => {
            try {
                const data = await getAllCourses();
                console.log("DEBUG: Fetched courses from API:", data); // Debug Log
                // Map backend data to frontend structure
                const formattedCourses = data.map(course => ({
                    code: course.courseCode,
                    title: course.courseName,
                    semester: Number(course.semester) || 0, // Ensure number
                    creditHours: course.creditHours,
                    prerequisites: course.prerequisites || []
                }));
                // Sort by semester then code
                formattedCourses.sort((a, b) => {
                    if (a.semester !== b.semester) return a.semester - b.semester;
                    return a.code.localeCompare(b.code);
                });
                setCourses(formattedCourses);
            } catch (err) {
                console.error("Failed to load courses", err);
                setError("Failed to load courses. Please check connection.");
            } finally {
                setLoading(false);
            }
        };

        fetchCourses();
    }, []);

    const semesters = useMemo(() => {
        const unique = Array.from(new Set(courses.map((course) => course.semester))).sort((a, b) => a - b);
        return unique;
    }, [courses]);

    const groupedCourses = useMemo(() => {
        return courses.reduce((acc, course) => {
            if (!acc[course.semester]) {
                acc[course.semester] = [];
            }
            acc[course.semester].push(course);
            return acc;
        }, {});
    }, [courses]);

    const dependentsMap = useMemo(() => {
        return courses.reduce((acc, course) => {
            course.prerequisites.forEach((pr) => {
                if (pr === 'varies' || pr === 'standing>=130CH') return;
                if (!acc[pr]) {
                    acc[pr] = [];
                }
                acc[pr].push(course.code);
            });
            return acc;
        }, {});
    }, [courses]);

    const courseMap = useMemo(() => {
        const map = {};
        courses.forEach(c => {
            map[c.code] = c.title;
        });
        return map;
    }, [courses]);//////////////////////////////////////////////////////

    const formatPrerequisite = (code) => {
        if (code === 'varies') return 'Varies by elective';
        if (code === 'standing>=130CH') return 'Standing = 130 CH';
        return courseMap[code] || code;
    };

    const highlight = (text) => {
        if (!search) return text;
        const escaped = escapeRegExp(search);
        const parts = text.split(new RegExp(`(${escaped})`, 'ig'));
        return parts.map((part, index) => (
            part.toLowerCase() === search.toLowerCase() ? <mark key={index}>{part}</mark> : <span key={index}>{part}</span>
        ));
    };

    const matchesSearch = (course) => {
        if (!search) return true;
        const query = search.toLowerCase();
        return course.code.toLowerCase().includes(query) || course.title.toLowerCase().includes(query) ||
            course.prerequisites.some((pr) => formatPrerequisite(pr).toLowerCase().includes(query));
    };

    const displaySemesters = semesterFilter === 'All' ? semesters : [Number(semesterFilter)];

    const handleCourseSelect = (course) => {
        setSelectedCourse((prev) => (prev?.code === course.code ? null : course));
    };

    // --- Add Course Handlers --- till line 194
    const resetNewCourse = () => {
        setNewCourse({
            code: '',
            title: '',
            creditHours: 3,
            semester: 3,
            prerequisites: []
        });
        setNewPrereqCode('');
        setShowAddModal(false);
    };

    const handleAddPrereqToNew = () => {
        if (newPrereqCode && !newCourse.prerequisites.includes(newPrereqCode)) {
            setNewCourse(prev => ({
                ...prev,
                prerequisites: [...prev.prerequisites, newPrereqCode]
            }));
            setNewPrereqCode('');
        }
    };

    const handleRemovePrereqFromNew = (code) => {
        setNewCourse(prev => ({
            ...prev,
            prerequisites: prev.prerequisites.filter(p => p !== code)
        }));
    };

    const isFormValid = newCourse.code && newCourse.title && newCourse.semester && newCourse.creditHours;

    const handleCreateCourse = async () => {
        if (!isFormValid) return;

        try {
            // 1. Create Base Course
            const coursePayload = {
                courseCode: newCourse.code,
                courseName: newCourse.title,
                creditHours: Number(newCourse.creditHours),
                semester: String(newCourse.semester)
            };

            await addCourse(coursePayload);

            // 2. Add Prerequisites if any
            if (newCourse.prerequisites.length > 0) {
                await updatePrerequisites(newCourse.code, newCourse.prerequisites);
            }

            // 3. Refresh & Close
            toast.success("Course created successfully!");
            resetNewCourse();
            // Optional: wait a bit before reload to let toast show, or use state update instead of reload
            setTimeout(() => window.location.reload(), 1000);
        } catch (err) {
            console.error(err);
            toast.error("Failed to create course. Code might already exist.");
        }
    };

    // Filter available for prereq dropdown (exclude self if typed)
    const availableForPrereq = courses.filter(c => c.code !== newCourse.code && !newCourse.prerequisites.includes(c.code));

    if (loading) return <div className="curriculum-loading">Loading curriculum...</div>;
    if (error) return <div className="curriculum-error">{error}</div>;

    return (
        <div className="curriculum-shell">
            <header className="curriculum-topline" role="banner">
                <div className="topline-brand">
                    <div className="brand-logo-shell">
                        <img src={umsLogo} alt="UMS logo" className="mini-logo" />
                    </div>
                    <div>
                        <p className="eyebrow">University Management – Admin</p>
                        <h1>Curriculum Roadmap</h1>
                        <p className="sub">Explore course progression, prerequisites, and elective pathways.</p>
                    </div>
                </div>
                <div className="meta">
                    <span className="version">{VERSION}</span>
                    <Link to="/Admin" className="back-btn">
                        ← Back to Dashboard
                    </Link>
                </div>
            </header>

            <section className="controls">
                <input
                    type="search"
                    placeholder="Search by course code, title or prerequisite"
                    value={search}
                    onChange={(event) => setSearch(event.target.value)}
                />
                <select value={semesterFilter} onChange={(event) => setSemesterFilter(event.target.value)}>
                    <option value="All">All semesters</option>
                    {semesters.map((semester) => (
                        <option key={semester} value={semester}>Semester {semester}</option>
                    ))}
                </select>
                <Link to="/admin/curriculum/edit-courses" className="edit-course-btn">
                    Edit Course
                </Link>
                <button
                    className="add-course-btn"
                    onClick={() => setShowAddModal(true)}
                >
                    + Add Course
                </button>
            </section>

            <section className="curriculum-grid">
                {displaySemesters.map((semester) => {
                    const semesterCourses = (groupedCourses[semester] || []).filter(matchesSearch);
                    return (
                        <div key={semester} className="semester-column">
                            <header className="semester-header">
                                <h2>Semester {semester}</h2>
                                <span>{(groupedCourses[semester] || []).length} courses</span>
                            </header>
                            <div className="course-list">
                                {semesterCourses.length === 0 && (
                                    <div className="empty">No courses match the current filter.</div>
                                )}
                                {semesterCourses.map((course) => (
                                    <article
                                        key={course.code}
                                        className={`course-card${selectedCourse?.code === course.code ? ' active' : ''}`}
                                        onClick={() => handleCourseSelect(course)}
                                        role="button"
                                        tabIndex={0}
                                        onKeyDown={(event) => {
                                            if (event.key === 'Enter' || event.key === ' ') {
                                                event.preventDefault();
                                                handleCourseSelect(course);
                                            }
                                        }}
                                    >
                                        <header>
                                            <span className="course-code">{highlight(course.code)}</span>
                                            <h3>{highlight(course.title)}</h3>
                                        </header>
                                        <div className="prerequisites">
                                            {course.prerequisites.length === 0 && <span className="prereq-badge none">No prerequisites</span>}
                                            {course.prerequisites.map((prerequisite) => (
                                                <span key={prerequisite} className="prereq-badge">
                                                    {highlight(formatPrerequisite(prerequisite))}
                                                </span>
                                            ))}
                                        </div>
                                        <div className="course-credits">
                                            {course.creditHours || 3} credits
                                        </div>
                                    </article>
                                ))}
                            </div>
                        </div>
                    );
                })}
            </section>

            <section className="notes">
                <h2>Advising Notes</h2>
                <ul>
                    {NOTES.map((note, index) => (
                        <li key={index}>{note}</li>
                    ))}
                </ul>
            </section>

            {selectedCourse && (
                <aside className="course-detail" aria-live="polite">
                    <header>
                        <span className="course-code">{selectedCourse.code}</span>
                        <h2>{selectedCourse.title}</h2>
                    </header>
                    <div className="detail-grid">
                        <div>
                            <h3>Semester</h3>
                            <p>Semester {selectedCourse.semester}</p>
                        </div>
                        <div>
                            <h3>Credits</h3>
                            <p>{selectedCourse.creditHours || 3} credit hours</p>
                        </div>
                        <div>
                            <h3>Prerequisites</h3>
                            {selectedCourse.prerequisites.length === 0 ? (
                                <p>No prerequisites</p>
                            ) : (
                                <ul>
                                    {selectedCourse.prerequisites.map((pr) => (
                                        <li key={pr}>{formatPrerequisite(pr)}</li>
                                    ))}
                                </ul>
                            )}
                        </div>
                        <div>
                            <h3>Unlocks</h3>
                            {dependentsMap[selectedCourse.code]?.length ? (
                                <ul>
                                    {dependentsMap[selectedCourse.code].map((dependent) => (
                                        <li key={dependent}>{dependent}</li>
                                    ))}
                                </ul>
                            ) : (
                                <p>No direct dependents</p>
                            )}
                        </div>
                    </div>
                    <button className="ghost-btn" onClick={() => setSelectedCourse(null)}>Close</button>
                </aside>
            )}
            {/* Add Course Modal till line 435*/}
            {showAddModal && (
                <div className="modal-overlay">
                    <div className="modal-content">
                        <header className="modal-header">
                            <h2>Add New Course</h2>
                            <button className="close-modal-btn" onClick={resetNewCourse}>×</button>
                        </header>
                        <div className="modal-body">
                            <div className="input-group">
                                <label>Course Code</label>
                                <input
                                    type="text"
                                    placeholder="e.g. CSE123"
                                    value={newCourse.code}
                                    onChange={(e) => setNewCourse({ ...newCourse, code: e.target.value.toUpperCase() })}
                                />
                            </div>

                            <div className="input-group">
                                <label>Course Title</label>
                                <input
                                    type="text"
                                    placeholder="e.g. Data Structures"
                                    value={newCourse.title}
                                    onChange={(e) => setNewCourse({ ...newCourse, title: e.target.value })}
                                />
                            </div>

                            <div className="row-group">
                                <div className="input-group">
                                    <label>Credits</label>
                                    <select
                                        value={newCourse.creditHours}
                                        onChange={(e) => setNewCourse({ ...newCourse, creditHours: e.target.value })}
                                    >
                                        {[1, 2, 3, 4, 5, 6].map(n => <option key={n} value={n}>{n}</option>)}
                                    </select>
                                </div>
                                <div className="input-group">
                                    <label>Semester</label>
                                    <select
                                        value={newCourse.semester || 3}
                                        onChange={(e) => setNewCourse({ ...newCourse, semester: Number(e.target.value) })}
                                    >
                                        {[3, 4, 5, 6, 7, 8, 9, 10].map(s => <option key={s} value={s}>Sem {s}</option>)}
                                    </select>
                                </div>
                            </div>

                            <div className="prereq-section">
                                <label>Prerequisites</label>
                                <div className="prereq-tags-container">
                                    {newCourse.prerequisites.map(p => (
                                        <span key={p} className="prereq-tag-edit">
                                            {formatPrerequisite(p)}
                                            <button onClick={() => handleRemovePrereqFromNew(p)}>×</button>
                                        </span>
                                    ))}
                                    {newCourse.prerequisites.length === 0 && <small>None selected</small>}
                                </div>
                                <div className="add-prereq-control">
                                    <select
                                        value={newPrereqCode}
                                        onChange={(e) => setNewPrereqCode(e.target.value)}
                                    >
                                        <option value="">Select Course...</option>
                                        {availableForPrereq.map(c => (
                                            <option key={c.code} value={c.code}>{c.code} - {c.title}</option>
                                        ))}
                                    </select>
                                    <button
                                        disabled={!newPrereqCode}
                                        onClick={handleAddPrereqToNew}
                                    >
                                        Add
                                    </button>
                                </div>
                            </div>
                        </div>
                        <footer className="modal-footer">
                            <button className="cancel-btn-modal" onClick={resetNewCourse}>Cancel</button>
                            <button
                                className="confirm-btn-modal"
                                disabled={!isFormValid}
                                onClick={handleCreateCourse}
                            >
                                Add Course
                            </button>
                        </footer>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Curriculum;
