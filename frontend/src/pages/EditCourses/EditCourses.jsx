import React, { useState, useMemo, useEffect } from 'react';
import { Link } from 'react-router-dom';
import './EditCourses.css';
import umsLogo from '../../assets/UMS Logo.png';
import { getAllCourses, updateCourse, deleteCourse, updatePrerequisites } from '../../api/CoursesApi';

const EditCourses = () => {
    const [searchTerm, setSearchTerm] = useState('');
    const [editingCourse, setEditingCourse] = useState(null);
    const [courses, setCourses] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [newPrereq, setNewPrereq] = useState('');

    useEffect(() => {
        loadCourses();
    }, []);

    const loadCourses = async () => {
        try {
            setLoading(true);
            const data = await getAllCourses();
            // Map backend data to frontend structure
            const formattedCourses = data.map(course => ({
                code: course.courseCode,
                title: course.courseName,
                semester: Number(course.semester) || 0,
                creditHours: course.creditHours,
                prerequisites: course.prerequisites || []
            }));
            // Sort by semester then code
            formattedCourses.sort((a, b) => {
                if (a.semester !== b.semester) return a.semester - b.semester;
                return a.code.localeCompare(b.code);
            });
            setCourses(formattedCourses);
            setError(null);
        } catch (err) {
            console.error("Failed to load courses", err);
            setError("Failed to load courses.");
        } finally {
            setLoading(false);
        }
    };

    const filteredCourses = useMemo(() => {
        return courses.filter(course =>
            course.code.toLowerCase().includes(searchTerm.toLowerCase()) ||
            course.title.toLowerCase().includes(searchTerm.toLowerCase())
        );
    }, [courses, searchTerm]);

    const handleEdit = (course) => {
        setEditingCourse({ ...course });
    };

    const handleSave = async () => {
        if (editingCourse) {
            try {
                // Prepare payload for backend
                const payload = {
                    courseCode: editingCourse.code,
                    courseName: editingCourse.title,
                    creditHours: editingCourse.creditHours,
                    semester: String(editingCourse.semester)
                };

                await updateCourse(editingCourse.code, payload);

                // Update prerequisites separately
                if (editingCourse.prerequisites) {
                    await updatePrerequisites(editingCourse.code, editingCourse.prerequisites);
                }

                // Refresh local list or just update state
                setCourses(prev => prev.map(course =>
                    course.code === editingCourse.code ? editingCourse : course
                ));
                setEditingCourse(null);
                alert("Course saved successfully!");
            } catch (err) {
                console.error("Failed to save course", err);
                alert("Failed to save course changes.");
            }
        }
    };

    const handleDelete = async (courseCode) => {
        if (window.confirm(`Are you sure you want to delete course ${courseCode}? This action cannot be undone.`)) {
            try {
                await deleteCourse(courseCode);
                setCourses(prev => prev.filter(course => course.code !== courseCode));
            } catch (err) {
                console.error("Failed to delete course", err);
                alert("Failed to delete course.");
            }
        }
    };

    const handleInputChange = (field, value) => {
        setEditingCourse(prev => ({
            ...prev,
            [field]: field === 'creditHours' || field === 'semester' ? parseInt(value) || 0 : value
        }));
    };

    const handleCancel = () => {
        setEditingCourse(null);
        setNewPrereq('');
    };

    const handleAddPrerequisite = () => {
        if (newPrereq && !editingCourse.prerequisites.includes(newPrereq)) {
            setEditingCourse(prev => ({
                ...prev,
                prerequisites: [...prev.prerequisites, newPrereq]
            }));
            setNewPrereq('');
        }
    };

    const handleRemovePrerequisite = (codeToRemove) => {
        setEditingCourse(prev => ({
            ...prev,
            prerequisites: prev.prerequisites.filter(p => p !== codeToRemove)
        }));
    };

    // Filter available courses for prerequisites (exclude self and already added)
    const availablePrereqs = courses.filter(c =>
        editingCourse &&
        c.code !== editingCourse.code &&
        !editingCourse.prerequisites.includes(c.code)
    );

    if (loading) return <div className="loading-msg">Loading courses...</div>;

    return (
        <div className="edit-courses-shell">
            <header className="edit-courses-topline" role="banner">
                <div className="topline-brand">
                    <div className="brand-logo-shell">
                        <img src={umsLogo} alt="UMS logo" className="mini-logo" />
                    </div>
                    <div>
                        <p className="eyebrow">University Management – Admin</p>
                        <h1>Course Management</h1>
                        <p className="sub">Edit course details, credit hours, and manage curriculum.</p>
                    </div>
                </div>
                <div className="meta">
                    <Link to="/admin/curriculum" className="back-btn">
                        ← Back to Curriculum
                    </Link>
                </div>
            </header>

            <section className="edit-controls">
                <input
                    type="search"
                    placeholder="Search courses by code or title..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                />
                <div className="results-count">
                    {filteredCourses.length} courses found
                </div>
            </section>

            {error && <div className="error-msg">{error}</div>}

            <section className="courses-management">
                <div className="courses-list">
                    {filteredCourses.map(course => (
                        <div key={course.code} className="course-management-card">
                            <div className="course-info">
                                <div className="course-header">
                                    <span className="course-code">{course.code}</span>
                                    <span className="course-credits">{course.creditHours} credits</span>
                                </div>
                                <h3 className="course-title">{course.title}</h3>
                                <div className="course-meta">
                                    <span>Semester {course.semester}</span>
                                    {course.prerequisites.length > 0 && (
                                        <span>{course.prerequisites.length} prerequisites</span>
                                    )}
                                </div>
                            </div>
                            <div className="course-actions">
                                <button
                                    className="edit-action-btn"
                                    onClick={() => handleEdit(course)}
                                >
                                    Edit
                                </button>
                                <button
                                    className="delete-action-btn"
                                    onClick={() => handleDelete(course.code)}
                                >
                                    Delete
                                </button>
                            </div>
                        </div>
                    ))}
                </div>

                {editingCourse && (
                    <aside className="edit-panel">
                        <div className="edit-panel-header">
                            <h2>Edit Course</h2>
                            <button className="close-btn" onClick={handleCancel}>×</button>
                        </div>
                        <div className="edit-form">
                            <div className="form-group">
                                <label>Course Code</label>
                                <input
                                    type="text"
                                    value={editingCourse.code}
                                    onChange={(e) => handleInputChange('code', e.target.value)}
                                    disabled
                                />
                            </div>
                            <div className="form-group">
                                <label>Course Title</label>
                                <input
                                    type="text"
                                    value={editingCourse.title}
                                    onChange={(e) => handleInputChange('title', e.target.value)}
                                />
                            </div>
                            <div className="form-row">
                                <div className="form-group">
                                    <label>Credit Hours</label>
                                    <select
                                        value={editingCourse.creditHours}
                                        onChange={(e) => handleInputChange('creditHours', e.target.value)}
                                    >
                                        <option value={1}>1 Credit</option>
                                        <option value={2}>2 Credits</option>
                                        <option value={3}>3 Credits</option>
                                        <option value={4}>4 Credits</option>
                                    </select>
                                </div>
                                <div className="form-group">
                                    <label>Semester</label>
                                    <select
                                        value={editingCourse.semester}
                                        onChange={(e) => handleInputChange('semester', e.target.value)}
                                    >
                                        {[3, 4, 5, 6, 7, 8, 9, 10].map(sem => (
                                            <option key={sem} value={sem}>Semester {sem}</option>
                                        ))}
                                    </select>
                                </div>
                            </div>

                            <div className="form-group">
                                <label>Prerequisites</label>
                                <div className="prereq-list">
                                    {editingCourse.prerequisites.map(pr => (
                                        <div key={pr} className="prereq-tag">
                                            <span>{pr}</span>
                                            <button
                                                className="remove-prereq-btn"
                                                onClick={() => handleRemovePrerequisite(pr)}
                                            >
                                                ×
                                            </button>
                                        </div>
                                    ))}
                                    {editingCourse.prerequisites.length === 0 && (
                                        <span className="no-prereqs-text">No prerequisites</span>
                                    )}
                                </div>
                                <div className="add-prereq-row">
                                    <select
                                        value={newPrereq}
                                        onChange={(e) => setNewPrereq(e.target.value)}
                                    >
                                        <option value="">Select course...</option>
                                        {availablePrereqs.map(c => (
                                            <option key={c.code} value={c.code}>
                                                {c.code} - {c.title}
                                            </option>
                                        ))}
                                    </select>
                                    <button
                                        className="add-prereq-btn"
                                        onClick={handleAddPrerequisite}
                                        disabled={!newPrereq}
                                    >
                                        Add
                                    </button>
                                </div>
                            </div>

                            <div className="form-actions">
                                <button className="cancel-btn" onClick={handleCancel}>
                                    Cancel
                                </button>
                                <button className="save-btn" onClick={handleSave}>
                                    Save Changes
                                </button>
                            </div>
                        </div>
                    </aside>
                )}
            </section>

            {filteredCourses.length === 0 && !loading && (
                <div className="empty-state">
                    <h3>No courses found</h3>
                    <p>Try adjusting your search terms</p>
                </div>
            )}
        </div>
    );
};

export default EditCourses;