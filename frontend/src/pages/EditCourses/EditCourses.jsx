import React, { useState, useMemo } from 'react';
import { Link } from 'react-router-dom';
import './EditCourses.css';
import umsLogo from '../../assets/UMS Logo.png';

const EditCourses = () => {
    const [searchTerm, setSearchTerm] = useState('');
    const [editingCourse, setEditingCourse] = useState(null);
    const [courses, setCourses] = useState([
        { code: 'CSE111', title: 'Logic Design', semester: 3, prerequisites: [], creditHours: 3 },
        { code: 'CSE131', title: 'Computer Programming', semester: 3, prerequisites: [], creditHours: 3 },
        { code: 'PHM113', title: 'Differential & Partial Differential Equations', semester: 3, prerequisites: ['PHM013'], creditHours: 3 },
        { code: 'CSE112', title: 'Computer Organization & Architecture', semester: 4, prerequisites: ['CSE111', 'CSE131'], creditHours: 3 },
        { code: 'CSE231', title: 'Advanced Computer Programming', semester: 4, prerequisites: ['CSE131'], creditHours: 3 },
        { code: 'CSE312', title: 'Electronic Design Automation', semester: 5, prerequisites: ['CSE112'], creditHours: 3 },
        { code: 'CSE332', title: 'Design & Analysis of Algorithms', semester: 6, prerequisites: ['CSE331'], creditHours: 3 },
        { code: 'CSE333', title: 'Database Systems', semester: 6, prerequisites: ['CSE331'], creditHours: 3 },
        { code: 'CSE351', title: 'Computer Networks', semester: 7, prerequisites: [], creditHours: 3 }
    ]);

    const filteredCourses = useMemo(() => {
        return courses.filter(course =>
            course.code.toLowerCase().includes(searchTerm.toLowerCase()) ||
            course.title.toLowerCase().includes(searchTerm.toLowerCase())
        );
    }, [courses, searchTerm]);

    const handleEdit = (course) => {
        setEditingCourse({ ...course });
    };

    const handleSave = () => {
        if (editingCourse) {
            setCourses(prev => prev.map(course =>
                course.code === editingCourse.code ? editingCourse : course
            ));
            setEditingCourse(null);
        }
    };

    const handleDelete = (courseCode) => {
        if (window.confirm(`Are you sure you want to delete course ${courseCode}? This action cannot be undone.`)) {
            setCourses(prev => prev.filter(course => course.code !== courseCode));
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
    };

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

            {filteredCourses.length === 0 && (
                <div className="empty-state">
                    <h3>No courses found</h3>
                    <p>Try adjusting your search terms</p>
                </div>
            )}
        </div>
    );
};

export default EditCourses;
//baheb besheer men ma3amee3o ma3moo3 ma3moo3