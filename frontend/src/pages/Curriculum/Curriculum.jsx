import React, { useMemo, useState } from 'react';
import './Curriculum.css';
import umsLogo from '../../assets/UMS Logo.png';

const curriculumData = {
    version: 'CESS-bylaw-2018',
    notes: [
        'CSE491 requires standing (>=130 CH) rather than a specific course.',
        "Electives (EL3/EL4) show 'varies' because prerequisites depend on the chosen course outline."
    ],
    courses: [
        { code: 'CSE111', title: 'Logic Design', semester: 3, prerequisites: [] },
        { code: 'CSE131', title: 'Computer Programming', semester: 3, prerequisites: [] },
        { code: 'PHM113', title: 'Differential & Partial Differential Equations', semester: 3, prerequisites: ['PHM013'] },
        { code: 'EPM118', title: 'Electrical & Electronic Circuits', semester: 3, prerequisites: ['PHM022'] },
        { code: 'EPM211', title: 'Properties of Electrical Materials', semester: 3, prerequisites: ['PHM022'] },
        { code: 'ASU112', title: 'Report Writing & Communication Skills', semester: 3, prerequisites: [] },

        { code: 'CSE112', title: 'Computer Organization & Architecture', semester: 4, prerequisites: ['CSE111', 'CSE131'] },
        { code: 'CSE231', title: 'Advanced Computer Programming', semester: 4, prerequisites: ['CSE131'] },
        { code: 'CSE334', title: 'Software Engineering', semester: 4, prerequisites: ['CSE131'] },
        { code: 'PHM111', title: 'Probability & Statistics', semester: 4, prerequisites: ['PHM013'] },
        { code: 'PHM114', title: 'Numerical Analysis', semester: 4, prerequisites: ['PHM113'] },
        { code: 'ASU-EL1', title: 'ASU Elective (1)', semester: 4, prerequisites: [] },

        { code: 'CSE312', title: 'Electronic Design Automation', semester: 5, prerequisites: ['CSE112'] },
        { code: 'CSE335', title: 'Operating Systems', semester: 5, prerequisites: ['CSE112'] },
        { code: 'CSE232', title: 'Advanced Software Engineering', semester: 5, prerequisites: ['CSE334'] },
        { code: 'CSE331', title: 'Data Structures & Algorithms', semester: 5, prerequisites: ['CSE231'] },
        { code: 'PHM211', title: 'Discrete Mathematics', semester: 5, prerequisites: ['PHM111', 'PHM113'] },
        { code: 'ECE251', title: 'Signals & Systems Fundamentals', semester: 5, prerequisites: ['PHM111', 'PHM113'] },

        { code: 'CSE332', title: 'Design & Analysis of Algorithms', semester: 6, prerequisites: ['CSE331'] },
        { code: 'CSE333', title: 'Database Systems', semester: 6, prerequisites: ['CSE331'] },
        { code: 'CSE338', title: 'Software Testing, Validation & Verification', semester: 6, prerequisites: ['CSE232'] },
        { code: 'CSE371', title: 'Control Engineering', semester: 6, prerequisites: ['ECE251'] },
        { code: 'CSE439', title: 'Design of Compilers', semester: 6, prerequisites: ['CSE131'] },
        { code: 'CSE472', title: 'Artificial Intelligence', semester: 6, prerequisites: ['CSE131', 'PHM211'] },

        { code: 'CSE211', title: 'Introduction to Embedded Systems', semester: 7, prerequisites: ['CSE131'] },
        { code: 'CSE233', title: 'Agile Software Engineering', semester: 7, prerequisites: ['CSE232'] },
        { code: 'CSE351', title: 'Computer Networks', semester: 7, prerequisites: ['CSE335'] },
        { code: 'EL3-1', title: 'Level-3 Technical Elective (1)', semester: 7, prerequisites: ['varies'] },
        { code: 'EPM119', title: 'Engineering Economy & Investments', semester: 7, prerequisites: [] },
        { code: 'ASU114', title: 'Selected Topics in Contemporary Issues', semester: 7, prerequisites: [] },
        { code: 'ASU-EL2', title: 'ASU Elective (2)', semester: 7, prerequisites: [] },

        { code: 'CSE341', title: 'Internet Programming', semester: 8, prerequisites: ['CSE231'] },
        { code: 'CSE354', title: 'Distributed Computing', semester: 8, prerequisites: ['CSE231', 'CSE351'] },
        { code: 'CSE411', title: 'Real-Time & Embedded Systems Design', semester: 8, prerequisites: ['CSE211'] },
        { code: 'CSE432', title: 'Automata & Computability', semester: 8, prerequisites: ['CSE332'] },
        { code: 'EL3-2', title: 'Level-3 Technical Elective (2)', semester: 8, prerequisites: ['varies'] },
        { code: 'EL3-3', title: 'Level-3 Technical Elective (3)', semester: 8, prerequisites: ['varies'] },
        { code: 'ASU111', title: 'Human Rights', semester: 8, prerequisites: [] },

        { code: 'CSE336', title: 'Software Design Patterns', semester: 9, prerequisites: ['CSE232'] },
        { code: 'CSE431', title: 'Mobile Programming', semester: 9, prerequisites: ['CSE341'] },
        { code: 'CSE441', title: 'Software Project Management', semester: 9, prerequisites: ['CSE334'] },
        { code: 'EL4-1', title: 'Level-4 Technical Elective (1)', semester: 9, prerequisites: ['varies'] },
        { code: 'EL4-2', title: 'Level-4 Technical Elective (2)', semester: 9, prerequisites: ['varies'] },
        { code: 'CSE491', title: 'Graduation Project (1)', semester: 9, prerequisites: ['standing>=130CH'] },

        { code: 'CSE451', title: 'Computer & Network Security', semester: 10, prerequisites: ['CSE351'] },
        { code: 'CSE455', title: 'High-Performance Computing', semester: 10, prerequisites: ['CSE112'] },
        { code: 'EL4-3', title: 'Level-4 Technical Elective (3)', semester: 10, prerequisites: ['varies'] },
        { code: 'EL4-4', title: 'Level-4 Technical Elective (4)', semester: 10, prerequisites: ['varies'] },
        { code: 'CSE492', title: 'Graduation Project (2)', semester: 10, prerequisites: ['CSE491'] },
        { code: 'ASU113', title: 'Professional Ethics & Legislations', semester: 10, prerequisites: [] }
    ]
};

const escapeRegExp = (string) => string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');

const formatPrerequisite = (code) => {
    if (code === 'varies') return 'Varies by elective';
    if (code === 'standing>=130CH') return 'Standing = 130 CH';
    return code;
};

const Curriculum = () => {
    const [search, setSearch] = useState('');
    const [semesterFilter, setSemesterFilter] = useState('All');
    const [selectedCourse, setSelectedCourse] = useState(null);

    const semesters = useMemo(() => {
        const unique = Array.from(new Set(curriculumData.courses.map((course) => course.semester))).sort((a, b) => a - b);
        return unique;
    }, []);

    const groupedCourses = useMemo(() => {
        return curriculumData.courses.reduce((acc, course) => {
            if (!acc[course.semester]) {
                acc[course.semester] = [];
            }
            acc[course.semester].push(course);
            return acc;
        }, {});
    }, []);

    const dependentsMap = useMemo(() => {
        return curriculumData.courses.reduce((acc, course) => {
            course.prerequisites.forEach((pr) => {
                if (pr === 'varies' || pr === 'standing>=130CH') return;
                if (!acc[pr]) {
                    acc[pr] = [];
                }
                acc[pr].push(course.code);
            });
            return acc;
        }, {});
    }, []);

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

    return (
        <div className="curriculum-shell">
            <header className="curriculum-topline" role="banner">
                <div className="topline-brand">
                    <div className="brand-logo-shell">
                        <img src={umsLogo} alt="UMS logo" className="mini-logo" />
                    </div>
                    <div>
                        <p className="eyebrow">University Management � Admin</p>
                        <h1>Curriculum Roadmap</h1>
                        <p className="sub">Explore course progression, prerequisites, and elective pathways.</p>
                    </div>
                </div>
                <div className="meta">
                    <span className="version">{curriculumData.version}</span>
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
                    {curriculumData.notes.map((note, index) => (
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
        </div>
    );
};

export default Curriculum;
