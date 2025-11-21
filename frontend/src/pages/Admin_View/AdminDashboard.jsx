import React, { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import './AdminDashboard.css';
import umsLogo from '../../assets/UMS Logo.png';
import { curriculumData } from '../../data/CurriculumData';

const courseCatalog = curriculumData.courses ?? [];

const gradePoints = {
    'A+': 4.0,
    A: 4.0,
    'A-': 3.7,
    'B+': 3.3,
    B: 3.0,
    'B-': 2.7,
    'C+': 2.3,
    C: 2.0,
    'C-': 1.7,
    D: 1.0,
    F: 0,
    IP: 0
};

const emptyStudent = {
    code: '',
    name: '',
    email: '',
    phone: '',
    militaryStatus: '',
    address: '',
    majorId: '',
    majorName: '',
    nationalId: '',
    birthdate: '',
    gradYear: '',
    completedHours: 0,
    fees: 0,
    status: 'Active',
    cgpa: 0,
    academicHistory: [],
    currentRegistrations: [],
    notes: '',
    holds: []
};

const sampleStudents = [
    {
        code: '21P0052',
        name: 'Youssef Samir',
        email: 'youssef.samir@university.edu',
        phone: '+20 100 345 6789',
        militaryStatus: 'Completed',
        address: 'Nasr City, Cairo',
        majorId: 'CENG',
        majorName: 'Computer Engineering',
        nationalId: '30105230123456',
        birthdate: '2001-05-23',
        gradYear: 2025,
        completedHours: 96,
        fees: 4100,
        status: 'Active',
        cgpa: 3.42,
        academicHistory: [
            { code: 'CSE131', title: 'Computer Programming', credits: 3, grade: 'A', term: 'Fall 2021' },
            { code: 'CSE231', title: 'Advanced Computer Programming', credits: 3, grade: 'A-', term: 'Spring 2022' },
            { code: 'CSE334', title: 'Software Engineering', credits: 3, grade: 'A-', term: 'Spring 2022' },
            { code: 'CSE331', title: 'Data Structures & Algorithms', credits: 3, grade: 'B+', term: 'Fall 2022' },
            { code: 'CSE335', title: 'Operating Systems', credits: 3, grade: 'B+', term: 'Fall 2022' },
            { code: 'CSE232', title: 'Advanced Software Engineering', credits: 3, grade: 'B', term: 'Spring 2023' },
            { code: 'PHM013', title: 'Calculus III', credits: 3, grade: 'B', term: 'Fall 2021' },
            { code: 'PHM111', title: 'Probability & Statistics', credits: 3, grade: 'A-', term: 'Spring 2022' },
            { code: 'PHM113', title: 'Differential Equations', credits: 3, grade: 'B+', term: 'Fall 2021' }
        ],
        currentRegistrations: [
            { code: 'CSE333', title: 'Database Systems', credits: 3 },
            { code: 'CSE338', title: 'Software Testing, Validation & Verification', credits: 3 }
        ],
        notes: 'Eligible for exchange program; confirm IELTS score submission.',
        holds: ['Library fines']
    },
    {
        code: '22P0141',
        name: 'Mariam El Shennawy',
        email: 'mariam.shennawy@university.edu',
        phone: '+20 114 222 1133',
        militaryStatus: 'Exempted',
        address: 'New Cairo, Cairo',
        majorId: 'SENG',
        majorName: 'Software Engineering',
        nationalId: '30207250123451',
        birthdate: '2002-07-25',
        gradYear: 2026,
        completedHours: 54,
        fees: 2800,
        status: 'Probation',
        cgpa: 2.41,
        academicHistory: [
            { code: 'CSE131', title: 'Computer Programming', credits: 3, grade: 'B', term: 'Fall 2021' },
            { code: 'CSE231', title: 'Advanced Computer Programming', credits: 3, grade: 'C+', term: 'Spring 2022' },
            { code: 'CSE334', title: 'Software Engineering', credits: 3, grade: 'B-', term: 'Spring 2022' },
            { code: 'CSE331', title: 'Data Structures & Algorithms', credits: 3, grade: 'C', term: 'Fall 2022' },
            { code: 'PHM013', title: 'Calculus III', credits: 3, grade: 'C+', term: 'Fall 2021' },
            { code: 'PHM111', title: 'Probability & Statistics', credits: 3, grade: 'C', term: 'Spring 2022' },
            { code: 'ASU112', title: 'Report Writing & Communication Skills', credits: 2, grade: 'B', term: 'Spring 2022' },
            { code: 'CSE335', title: 'Operating Systems', credits: 3, grade: 'IP', term: 'Fall 2023' }
        ],
        currentRegistrations: [
            { code: 'CSE335', title: 'Operating Systems', credits: 3 }
        ],
        notes: 'Academic probation; schedule weekly advising touchpoint.'
    },
    {
        code: '22P0232',
        name: 'Ahmed Galal',
        email: 'ahmed.galal@university.edu',
        phone: '+20 122 551 9070',
        militaryStatus: 'Completed',
        address: 'Heliopolis, Cairo',
        majorId: 'CSCI',
        majorName: 'Computer Science',
        nationalId: '29909120111223',
        birthdate: '1999-09-12',
        gradYear: 2023,
        completedHours: 138,
        fees: 0,
        status: 'Graduated',
        cgpa: 3.78,
        academicHistory: [
            { code: 'CSE131', title: 'Computer Programming', credits: 3, grade: 'A', term: 'Fall 2019' },
            { code: 'CSE231', title: 'Advanced Computer Programming', credits: 3, grade: 'A', term: 'Spring 2020' },
            { code: 'CSE331', title: 'Data Structures & Algorithms', credits: 3, grade: 'A', term: 'Fall 2020' },
            { code: 'CSE332', title: 'Design & Analysis of Algorithms', credits: 3, grade: 'A-', term: 'Spring 2021' },
            { code: 'CSE333', title: 'Database Systems', credits: 3, grade: 'A-', term: 'Spring 2021' },
            { code: 'CSE351', title: 'Computer Networks', credits: 3, grade: 'A', term: 'Fall 2021' },
            { code: 'CSE354', title: 'Distributed Computing', credits: 3, grade: 'A-', term: 'Spring 2022' },
            { code: 'CSE451', title: 'Computer & Network Security', credits: 3, grade: 'A', term: 'Fall 2022' },
            { code: 'CSE491', title: 'Graduation Project (1)', credits: 2, grade: 'A', term: 'Fall 2022' },
            { code: 'CSE492', title: 'Graduation Project (2)', credits: 3, grade: 'A', term: 'Spring 2023' }
        ],
        currentRegistrations: [],
        notes: 'Graduated Fall 2023. All clearance documents archived.',
        holds: []
    }
];
const downloadBlob = (content, filename, mime) => {
    const blob = new Blob([content], { type: mime });
    const url = URL.createObjectURL(blob);
    const anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = filename;
    anchor.click();
    URL.revokeObjectURL(url);
};

const sumCredits = (records) =>
    records.reduce((total, record) => total + (record.credits ?? 3), 0);

const computeGPA = (records) => {
    const { points, credits } = records.reduce(
        (acc, record) => {
            const creditHours = record.credits ?? 3;
            const gradePoint = gradePoints[record.grade] ?? 0;
            acc.points += creditHours * gradePoint;
            acc.credits += creditHours;
            return acc;
        },
        { points: 0, credits: 0 }
    );

    if (!credits) {
        return null;
    }

    return Number((points / credits).toFixed(2));
};

const formatCurrency = (value) =>
    new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'EGP',
        minimumFractionDigits: 0
    }).format(value ?? 0);

const getStandingLabel = (completedHours) => {
    if (completedHours >= 130) return 'Senior';
    if (completedHours >= 95) return 'Junior';
    if (completedHours >= 60) return 'Sophomore';
    return 'Freshman';
};

const isPrerequisiteSatisfied = (prerequisite, completedCodes, completedHours) => {
    if (!prerequisite) return true;
    const normalized = prerequisite.trim().toLowerCase();

    if (normalized === 'varies') {
        return true;
    }

    if (normalized.startsWith('standing>=')) {
        const value = parseInt(normalized.replace('standing>=', ''), 10);
        return Number.isFinite(value) ? completedHours >= value : true;
    }

    return completedCodes.has(prerequisite);
};

const buildStudentSnapshot = (student) => {
    const dynamicGpa = computeGPA(student.academicHistory);
    const totalHours = student.completedHours ?? sumCredits(student.academicHistory);

    return {
        code: student.code,
        name: student.name,
        email: student.email,
        phone: student.phone,
        major: student.majorName,
        status: student.status,
        cgpa: dynamicGpa ?? student.cgpa ?? null,
        completedHours: totalHours,
        feesDue: student.fees,
        gradYear: student.gradYear,
        militaryStatus: student.militaryStatus,
        address: student.address,
        nationalId: student.nationalId,
        notes: student.notes,
        holds: student.holds,
        currentRegistrations: student.currentRegistrations,
        academicHistory: student.academicHistory
    };
};

const Icon = {
    student: (
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
            <path d="M3 7l9-4 9 4-9 4-9-4z" />
            <path d="M12 11v6" />
            <path d="M6 13.5c1.8 1.2 3.8 1.8 6 1.8s4.2-.6 6-1.8" />
            <path d="M18 9l3 1.5-3 1.5" />
        </svg>
    ),
    facilities: (
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
            <path d="M3 10l9-6 9 6" />
            <path d="M4 10h16" />
            <path d="M6 10v9" />
            <path d="M10 10v9" />
            <path d="M14 10v9" />
            <path d="M18 10v9" />
            <path d="M3 22h18" />
        </svg>
    ),
    analytics: (
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
            <path d="M3 3v18h18" />
            <rect x="7" y="12" width="3" height="6" rx="1" />
            <rect x="12" y="9" width="3" height="9" rx="1" />
            <rect x="17" y="6" width="3" height="12" rx="1" />
        </svg>
    ),
    requests: (
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
            <rect x="4" y="3" width="12" height="18" rx="2" />
            <path d="M8 8h4" />
            <path d="M8 12h6" />
            <path d="M14 3v4h4" />
            <path d="M16 17l2 2 4-4" />
        </svg>
    ),
    menu16: (
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M4 6h16M4 12h16M4 18h16" /></svg>
    ),
    home16: (
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M3 12l9-8 9 8" /><path d="M5 10v10h14V10" /></svg>
    ),
    help16: (
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M9.1 9a3 3 0 0 1 5.8 1c0 1.5-1 2.2-1.8 2.8-.7.5-1.1.9-1.1 1.7V15" /><circle cx="12" cy="18" r="0.5" /></svg>
    ),
    bell16: (
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M18 8a6 6 0 1 0-12 0c0 7-3 7-3 7h18s-3 0-3-7" /><path d="M13.73 21a2 2 0 0 1-3.46 0" /></svg>
    ),
    msg16: (
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5" /><path d="M22 4 12 14" /><path d="M16 4h6v6" /></svg>
    ),
    user16: (
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" /><circle cx="12" cy="7" r="4" /></svg>
    ),
    plus16: (
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M12 5v14M5 12h14" /></svg>
    ),
    edit16: (
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M17 3a2.8 2.8 0 0 1 4 4L7 21l-4 1 1-4Z" /><path d="m15 5 4 4" /></svg>
    ),
    trash16: (
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M3 6h18" /><path d="M8 6V4h8v2" /><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6" /><path d="M10 11v6M14 11v6" /></svg>
    ),
    download16: (
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M12 3v12" /><path d="m7 12 5 5 5-5" /><path d="M5 21h14" /></svg>
    ),
    check16: (
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M20 6 9 17l-5-5" /></svg>
    ),
    close16: (
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="m4 4 16 16M20 4 4 20" /></svg>
    )
};

const AdminDashboard = () => {
    const [students, setStudents] = useState(sampleStudents);
    const [selectedCode, setSelectedCode] = useState(sampleStudents[0]?.code ?? null);
    const [filters, setFilters] = useState({ query: '', status: 'all', major: 'all' });
    const [formMode, setFormMode] = useState(null);
    const [formData, setFormData] = useState(emptyStudent);
    const [validationError, setValidationError] = useState('');
    const [pendingRegistration, setPendingRegistration] = useState([]);
    const [notesDraft, setNotesDraft] = useState('');
    const [view, setView] = useState('home');
    const isStudentView = view === 'students';

    const tiles = [
        {
            title: 'Student Records',
            sub: 'Manage enrollment, transcripts, and holds',
            icon: Icon.student,
            accent: 'sunrise',
            mode: 'students'
        },
        {
            title: 'Facilities & Halls',
            sub: 'Schedule maintenance and reservations',
            icon: Icon.facilities,
            accent: 'aqua',
            path: '/admin/facilities'
        },
        {
            title: 'Curriculum',
            sub: 'Review course structure and prerequisites',
            icon: Icon.analytics,
            accent: 'violet',
            path: '/admin/Curriculum'
        },
        {
            title: 'Requests & Approvals',
            sub: 'Track pending requests and actions',
            icon: Icon.requests,
            accent: 'citrus',
            path: '/Requests'
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

    const handleFormSubmit = (event) => {
        event.preventDefault();

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

            setStudents((prev) => [...prev, newStudent]);
            setSelectedCode(newStudent.code);
            setNotesDraft(newStudent.notes);
        }

        if (formMode === 'edit' && selectedStudent) {
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
        }

        closeForm();
    };

    const handleDeleteStudent = (code) => {
        const confirmation = window.confirm(
            `This will remove ${code} and their academic record from the dashboard. Continue?`
        );
        if (!confirmation) {
            return;
        }
        setStudents((prev) => prev.filter((student) => student.code !== code));
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
                    <button className="icon-btn" aria-label="Notifications" title="Notifications">
                        {Icon.bell16}
                    </button>
                    <button className="icon-btn" aria-label="Messages" title="Messages">
                        <span className="badge">0</span>
                        {Icon.msg16}
                    </button>
                    <button className="icon-btn" aria-label="Account" title="Account">
                        {Icon.user16}
                    </button>
                </div>
            </header>

            <aside className="sidebar" aria-label="Sidebar navigation">
                <div className="sidebar-user">
                    <div className="avatar" aria-hidden="true">
                        <span className="avatar-ico">{Icon.user16}</span>
                    </div>
                    <div className="user-meta">
                        <div className="user-name">Admin User</div>
                    </div>
                </div>
                <nav className="side-nav">
                    <button type="button" className="nav-item active">
                        <span className="nav-ico">{Icon.home16}</span>
                        <span>Student Services</span>
                    </button>
                    <button type="button" className="nav-item">
                        <span className="nav-ico">{Icon.help16}</span>
                        <span>Help</span>
                    </button>
                </nav>
            </aside>

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
                    {isStudentView && (
                        <div className="page-actions">
                            <button
                                type="button"
                                className="ghost-btn"
                                onClick={() => setView('home')}
                            >
                                {Icon.home16}
                                <span>Back to dashboard</span>
                            </button>
                            {selectedStudent && (
                                <>
                                    <button
                                        type="button"
                                        className="pill-btn"
                                        onClick={() => handleExport(selectedStudent, 'json')}
                                    >
                                        {Icon.download16}
                                        <span>Export JSON</span>
                                    </button>
                                    <button
                                        type="button"
                                        className="pill-btn"
                                        onClick={() => handleExport(selectedStudent, 'csv')}
                                    >
                                        {Icon.download16}
                                        <span>Export CSV</span>
                                    </button>
                                </>
                            )}
                        </div>
                    )}
                </div>

                {isStudentView && (
                    <section className="dashboard-content">
                        <aside className="student-panel">
                            <header className="student-panel-header">
                                <div className="student-panel-top">
                                    <h3>Students</h3>
                                    <button type="button" className="primary-btn" onClick={openAddStudent}>
                                        {Icon.plus16}
                                        <span>Add Student</span>
                                    </button>
                                </div>
                                <div className="student-filters">
                                    <input
                                        type="search"
                                        placeholder="Search by name, code, or major"
                                        value={filters.query}
                                        onChange={(event) =>
                                            setFilters((prev) => ({
                                                ...prev,
                                                query: event.target.value
                                            }))
                                        }
                                    />
                                    <div className="filter-row">
                                        <select
                                            value={filters.major}
                                            onChange={(event) =>
                                                setFilters((prev) => ({
                                                    ...prev,
                                                    major: event.target.value
                                                }))
                                            }
                                        >
                                            <option value="all">All majors</option>
                                            {majorOptions.map((major) => (
                                                <option key={major} value={major}>
                                                    {major}
                                                </option>
                                            ))}
                                        </select>
                                        <select
                                            value={filters.status}
                                            onChange={(event) =>
                                                setFilters((prev) => ({
                                                    ...prev,
                                                    status: event.target.value
                                                }))
                                            }
                                        >
                                            <option value="all">All statuses</option>
                                            <option value="Active">Active</option>
                                            <option value="Probation">Probation</option>
                                            <option value="Suspended">Suspended</option>
                                            <option value="Graduated">Graduated</option>
                                        </select>
                                    </div>
                                </div>
                            </header>

                            <div className="student-list" role="list">
                                {filteredStudents.length === 0 && (
                                    <div className="empty-state">
                                        No students match the current filters.
                                    </div>
                                )}

                                {filteredStudents.map((student) => (
                                    <button
                                        type="button"
                                        key={student.code}
                                        className={`student-card${
                                            student.code === selectedCode ? ' active' : ''
                                        }`}
                                        onClick={() => setSelectedCode(student.code)}
                                    >
                                        <div className="student-card-main">
                                            <h4>{student.name}</h4>
                                            <span className="student-code">{student.code}</span>
                                        </div>
                                        <div className="student-card-meta">
                                        <span
                                            className="status-chip"
                                            data-status={student.status.toLowerCase()}
                                        >
                                            {student.status}
                                        </span>
                                            <span className="student-major">{student.majorName}</span>
                                            <span className="student-gpa">
                                            GPA {studentGpaDisplay(student)}
                                        </span>
                                        </div>
                                    </button>
                                ))}
                            </div>
                        </aside>
                        <section className="detail-panel">
                            {!selectedStudent && (
                                <div className="empty-state large">
                                    Select a student to view their full record.
                                </div>
                            )}

                            {selectedStudent && (
                                <>
                                    <article className="detail-card">
                                        <header className="detail-card-header">
                                            <div className="detail-ident">
                                                <div className="detail-logo-shell">
                                                    <img
                                                        src={String(umsLogo)}
                                                        alt=""
                                                        aria-hidden="true"
                                                    />
                                                </div>
                                                <div>
                                                    <h2>{selectedStudent.name}</h2>
                                                    <p>
                                                        {selectedStudent.majorName}  {' '}
                                                        {selectedStudent.code}
                                                    </p>
                                                    <p>{selectedStudent.email}</p>
                                                </div>
                                            </div>
                                            <div className="detail-actions">
                                                <button
                                                    type="button"
                                                    className="ghost-btn"
                                                    onClick={() => openEditStudent(selectedStudent)}
                                                >
                                                    {Icon.edit16}
                                                    <span>Edit</span>
                                                </button>
                                                <button
                                                    type="button"
                                                    className="ghost-btn danger"
                                                    onClick={() => handleDeleteStudent(selectedStudent.code)}
                                                >
                                                    {Icon.trash16}
                                                    <span>Delete</span>
                                                </button>
                                            </div>
                                        </header>

                                        <div className="kpi-grid">
                                            <div className="kpi-card">
                                                <span className="kpi-label">CGPA</span>
                                                <strong>
                                                    {(dynamicGpa ?? selectedStudent.cgpa ?? 0).toFixed(2)}
                                                </strong>
                                                <span className="kpi-sub">Target = 2.0</span>
                                            </div>
                                            <div className="kpi-card">
                                                <span className="kpi-label">Completed Hours</span>
                                                <strong>{completedHoursValue}</strong>
                                                <span className="kpi-sub">
                                                Standing {getStandingLabel(completedHoursValue)}
                                            </span>
                                            </div>
                                            <div className="kpi-card">
                                                <span className="kpi-label">Registered Credits</span>
                                                <strong>{registeredCredits}</strong>
                                                <span className="kpi-sub">Current term</span>
                                            </div>
                                            <div className="kpi-card">
                                                <span className="kpi-label">Fees Due</span>
                                                <strong>{formatCurrency(selectedStudent.fees)}</strong>
                                                <span className="kpi-sub">Next billing cycle</span>
                                            </div>
                                        </div>

                                        <div className="meta-grid">
                                            <div>
                                                <span className="meta-label">Phone</span>
                                                <span className="meta-value">
                                                {selectedStudent.phone || ' '}
                                            </span>
                                            </div>
                                            <div>
                                                <span className="meta-label">National ID</span>
                                                <span className="meta-value">
                                                {selectedStudent.nationalId || ' '}
                                            </span>
                                            </div>
                                            <div>
                                                <span className="meta-label">Birthdate</span>
                                                <span className="meta-value">
                                                {selectedStudent.birthdate || ' '}
                                            </span>
                                            </div>
                                            <div>
                                                <span className="meta-label">Military Status</span>
                                                <span className="meta-value">
                                                {selectedStudent.militaryStatus || ' '}
                                            </span>
                                            </div>
                                            <div>
                                                <span className="meta-label">Address</span>
                                                <span className="meta-value">
                                                {selectedStudent.address || ' '}
                                            </span>
                                            </div>
                                            <div>
                                                <span className="meta-label">Graduation Year</span>
                                                <span className="meta-value">
                                                {selectedStudent.gradYear || ' '}
                                            </span>
                                            </div>
                                        </div>
                                    </article>

                                    <article className="history-card">
                                        <header>
                                            <h3>Academic History</h3>
                                            <span>
                                            {(selectedStudent.academicHistory ?? []).length} courses
                                        </span>
                                        </header>
                                        <div className="history-table-wrapper">
                                            <table className="history-table">
                                                <thead>
                                                <tr>
                                                    <th>Code</th>
                                                    <th>Course</th>
                                                    <th>Credits</th>
                                                    <th>Grade</th>
                                                    <th>Term</th>
                                                </tr>
                                                </thead>
                                                <tbody>
                                                {(selectedStudent.academicHistory ?? []).map(
                                                    (record) => (
                                                        <tr key={`${record.code}-${record.term}`}>
                                                            <td>{record.code}</td>
                                                            <td>{record.title}</td>
                                                            <td>{record.credits ?? 3}</td>
                                                            <td>{record.grade}</td>
                                                            <td>{record.term}</td>
                                                        </tr>
                                                    )
                                                )}
                                                </tbody>
                                            </table>
                                        </div>
                                    </article>
                                    <article className="registration-card">
                                        <header>
                                            <div>
                                                <h3>Course Registration</h3>
                                                <p>
                                                    Eligible courses are filtered automatically using
                                                    prerequisite data from the curriculum.
                                                </p>
                                            </div>
                                        </header>
                                        <div className="registration-grid">
                                            <div className="eligible-courses">
                                                <h4>Eligible Courses</h4>
                                                <div className="course-scroll">
                                                    {availableCourses.length === 0 && (
                                                        <div className="empty-state small">
                                                            No additional courses found for the current
                                                            standing.
                                                        </div>
                                                    )}

                                                    {availableCourses.map((course) => (
                                                        <article
                                                            className="course-option"
                                                            key={course.code}
                                                        >
                                                            <header>
                                                                <div>
                                                                <span className="course-code">
                                                                    {course.code}
                                                                </span>
                                                                    <h5>{course.title}</h5>
                                                                </div>
                                                                <span className="course-hours">
                                                                {course.creditHours} cr.
                                                            </span>
                                                            </header>
                                                            <div className="course-meta">
                                                                <span>Semester {course.semester}</span>
                                                            </div>
                                                            <div className="prereq-row">
                                                                {(course.prerequisites ?? []).length === 0 && (
                                                                    <span className="prereq-chip met">
                                                                    No prerequisites
                                                                </span>
                                                                )}
                                                                {(course.prerequisites ?? []).map((prerequisite) => (
                                                                    <span
                                                                        key={`${course.code}-${prerequisite}`}
                                                                        className="prereq-chip met"
                                                                    >
                                                                    {prerequisite}
                                                                </span>
                                                                ))}
                                                            </div>
                                                            {course.requiresManual && (
                                                                <p className="advisor-note">
                                                                    Advisor approval required for elective
                                                                    prerequisite (varies).
                                                                </p>
                                                            )}
                                                            <button
                                                                type="button"
                                                                className="primary-btn"
                                                                onClick={() => handleAddCourse(course)}
                                                            >
                                                                {Icon.plus16}
                                                                <span>Add to plan</span>
                                                            </button>
                                                        </article>
                                                    ))}
                                                </div>
                                            </div>

                                            <div className="pending-panel">
                                                <h4>Pending Submission</h4>
                                                <div className="pending-list">
                                                    {pendingRegistration.length === 0 && (
                                                        <div className="empty-state small">
                                                            No courses selected yet.
                                                        </div>
                                                    )}
                                                    {pendingRegistration.map((course) => (
                                                        <div
                                                            className="pending-item"
                                                            key={`pending-${course.code}`}
                                                        >
                                                            <div>
                                                                <strong>{course.code}</strong>
                                                                <span>{course.title}</span>
                                                            </div>
                                                            <button
                                                                type="button"
                                                                className="ghost-btn"
                                                                onClick={() => handleRemovePending(course.code)}
                                                            >
                                                                {Icon.close16}
                                                                <span>Remove</span>
                                                            </button>
                                                        </div>
                                                    ))}
                                                </div>
                                                <div className="pending-actions">
                                                    <button
                                                        type="button"
                                                        className="ghost-btn"
                                                        onClick={() => setPendingRegistration([])}
                                                        disabled={pendingRegistration.length === 0}
                                                    >
                                                        Clear
                                                    </button>
                                                    <button
                                                        type="button"
                                                        className="primary-btn"
                                                        onClick={confirmRegistration}
                                                        disabled={pendingRegistration.length === 0}
                                                    >
                                                        {Icon.check16}
                                                        <span>Submit registration</span>
                                                    </button>
                                                </div>
                                            </div>
                                        </div>

                                        <div className="current-registrations">
                                            <h4>Current Term</h4>
                                            <div className="registration-list">
                                                {(selectedStudent.currentRegistrations ?? []).length === 0 && (
                                                    <div className="empty-state small">
                                                        No active registrations.
                                                    </div>
                                                )}
                                                {(selectedStudent.currentRegistrations ?? []).map(
                                                    (course) => (
                                                        <div
                                                            className="registration-item"
                                                            key={`active-${course.code}`}
                                                        >
                                                            <div>
                                                                <strong>{course.code}</strong>
                                                                <span>{course.title}</span>
                                                            </div>
                                                            <button
                                                                type="button"
                                                                className="ghost-btn danger"
                                                                onClick={() => handleDropCourse(course.code)}
                                                            >
                                                                {Icon.trash16}
                                                                <span>Drop</span>
                                                            </button>
                                                        </div>
                                                    )
                                                )}
                                            </div>
                                        </div>
                                    </article>

                                    <article className="notes-card">
                                        <header>
                                            <h3>Advisor Notes</h3>
                                            <span>
                                            {selectedStudent.holds?.length
                                                ? `${selectedStudent.holds.length} hold(s)`
                                                : 'No active holds'}
                                        </span>
                                        </header>
                                        <textarea
                                            value={notesDraft}
                                            onChange={(event) => setNotesDraft(event.target.value)}
                                            onBlur={handleNotesBlur}
                                            placeholder="Add advising notes, action items, or follow-ups..."
                                        />
                                    </article>
                                </>
                            )}
                        </section>
                    </section>
                )}
                {!isStudentView && (
                    <div className="grid">
                        {tiles.map((tile) => (
                            tile.mode === 'students' ? (
                                <div
                                    key={tile.title}
                                    className="card"
                                    data-accent={tile.accent}
                                    role="button"
                                    tabIndex={0}
                                    onClick={() => setView('students')}
                                    onKeyDown={(event) => {
                                        if (event.key === 'Enter' || event.key === ' ') {
                                            event.preventDefault();
                                            setView('students');
                                        }
                                    }}
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
                                </div>
                            ) : (
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
                        ))}
                    </div>
                )}
            </main>
            {formMode && (
                <div className="modal-backdrop" role="dialog" aria-modal="true">
                    <div className="modal">
                        <header className="modal-header">
                            <h3>{formMode === 'add' ? 'Add Student' : 'Edit Student'}</h3>
                            <button type="button" className="ghost-btn" onClick={closeForm}>
                                {Icon.close16}
                                <span>Close</span>
                            </button>
                        </header>
                        <form className="modal-body" onSubmit={handleFormSubmit}>
                            <div className="form-grid">
                                <label>
                                    <span>Student Code</span>
                                    <input
                                        name="code"
                                        value={formData.code}
                                        onChange={handleFormChange}
                                        required
                                        disabled={formMode === 'edit'}
                                    />
                                </label>
                                <label>
                                    <span>Full Name</span>
                                    <input
                                        name="name"
                                        value={formData.name}
                                        onChange={handleFormChange}
                                        required
                                    />
                                </label>
                                <label>
                                    <span>Email</span>
                                    <input
                                        type="email"
                                        name="email"
                                        value={formData.email}
                                        onChange={handleFormChange}
                                    />
                                </label>
                                <label>
                                    <span>Phone</span>
                                    <input
                                        name="phone"
                                        value={formData.phone}
                                        onChange={handleFormChange}
                                    />
                                </label>
                                <label>
                                    <span>Major</span>
                                    <input
                                        name="majorName"
                                        value={formData.majorName}
                                        onChange={handleFormChange}
                                    />
                                </label>
                                <label>
                                    <span>Major ID</span>
                                    <input
                                        name="majorId"
                                        value={formData.majorId}
                                        onChange={handleFormChange}
                                    />
                                </label>
                                <label>
                                    <span>Status</span>
                                    <select
                                        name="status"
                                        value={formData.status}
                                        onChange={handleFormChange}
                                    >
                                        <option value="Active">Active</option>
                                        <option value="Probation">Probation</option>
                                        <option value="Suspended">Suspended</option>
                                        <option value="Graduated">Graduated</option>
                                    </select>
                                </label>
                                <label>
                                    <span>CGPA</span>
                                    <input
                                        name="cgpa"
                                        value={formData.cgpa}
                                        onChange={handleFormChange}
                                        inputMode="decimal"
                                    />
                                </label>
                                <label>
                                    <span>Completed Hours</span>
                                    <input
                                        name="completedHours"
                                        value={formData.completedHours}
                                        onChange={handleFormChange}
                                        inputMode="numeric"
                                    />

                                </label>
                                <label>
                                    <span>Fees Due</span>
                                    <input
                                        name="fees"
                                        value={formData.fees}
                                        onChange={handleFormChange}
                                        inputMode="numeric"
                                    />
                                </label>
                                <label>
                                    <span>Graduation Year</span>
                                    <input
                                        name="gradYear"
                                        value={formData.gradYear}
                                        onChange={handleFormChange}
                                        inputMode="numeric"
                                    />
                                </label>
                                <label>
                                    <span>Military Status</span>
                                    <input
                                        name="militaryStatus"
                                        value={formData.militaryStatus}
                                        onChange={handleFormChange}
                                    />
                                </label>
                                <label className="full">
                                    <span>Address</span>
                                    <input
                                        name="address"
                                        value={formData.address}
                                        onChange={handleFormChange}
                                    />
                                </label>
                                <label className="full">
                                    <span>Notes</span>
                                    <textarea
                                        name="notes"
                                        value={formData.notes}
                                        onChange={handleFormChange}
                                    />
                                </label>
                            </div>

                            {validationError && (
                                <p className="form-error" role="alert">
                                    {validationError}
                                </p>
                            )}

                            <footer className="modal-footer">
                                <button type="button" className="ghost-btn" onClick={closeForm}>
                                    Cancel
                                </button>
                                <button type="submit" className="primary-btn">
                                    {Icon.check16}
                                    <span>Save</span>
                                </button>
                            </footer>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default AdminDashboard;