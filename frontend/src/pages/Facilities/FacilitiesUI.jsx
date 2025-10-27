import React, { useMemo, useState } from 'react';
import './FacilitiesUI.css';
import umsLogo from '../../assets/UMS Logo.png';

const hallClusters = [
    {
        building: 'Main Building',
        type: 'Lecture Hall',
        capacity: 80,
        resources: 'Hybrid capture, ceiling speakers, retractable seating',
        status: 'Available',
        identifiers: ['219', '338', '346', '347', '348', '350']
    },
    {
        building: 'Credit Building',
        type: 'Classroom',
        capacity: 60,
        resources: 'Interactive display, lecture recording, dual whiteboards',
        status: 'Available',
        identifiers: ['911', '911A', '912', '913', '914', '914A', '921', '921A', '922', '923', '924', '924A', '931', '931A', '932', '933', '941', '941A', '942', '943', '944', '944A']
    },
    {
        building: 'Credit Building',
        type: 'Auditorium',
        capacity: 180,
        resources: 'Tiered seating, stage lighting, broadcast booth',
        status: 'Available',
        identifiers: ['Hall 1', 'Hall 2', 'Hall 3', 'Hall 4']
    },
    {
        building: 'Architect Annex',
        type: 'Auditorium',
        capacity: 180,
        resources: 'Acoustic treatment, grand LED wall, translation headsets',
        status: 'Available',
        identifiers: ['Hall A', 'Hall B', 'Hall C', 'Hall D']
    },
    {
        building: 'Architecture Building',
        type: 'Grand Auditorium',
        capacity: 250,
        resources: 'Exhibition lighting, retractable truss, live-stream hub',
        status: 'Available',
        identifiers: ['500', '501', '502', '504', '505']
    }
];
const formatHallName = (identifier) => {
    const trimmed = identifier.trim();
    if (/hall/i.test(trimmed)) {
        return trimmed.replace(/\s+/g, ' ');
    }
    if (/^[A-Za-z]/.test(trimmed)) {
        return `Hall ${trimmed.toUpperCase()}`;
    }
    return `Room ${trimmed}`;
};

const hallSeed = hallClusters.flatMap((cluster) =>
    cluster.identifiers.map((identifier) => {
        const name = formatHallName(identifier);
        const normalizedBuilding = cluster.building;
        const status = cluster.status || 'Available';
        return {
            id: `${normalizedBuilding.replace(/\s+/g, '-')}-${identifier}`,
            code: identifier,
            name,
            building: normalizedBuilding,
            type: cluster.type,
            capacity: cluster.capacity,
            resources: cluster.resources,
            status
        };
    })
);

const hallNameByCode = hallSeed.reduce((acc, hall) => {
    acc[hall.code] = hall.name;
    return acc;
}, {});

const requestSeed = [
    {
        id: 'REQ-5101',
        faculty: 'Dr. Layla Hassan',
        course: 'ENG-210 Technical Writing',
        hall: hallNameByCode['911'],
        datetime: '2025-11-02 10:00',
        status: 'Pending'
    },
    {
        id: 'REQ-5102',
        faculty: 'Prof. Omar Farouk',
        course: 'CIS-340 Cloud Computing',
        hall: hallNameByCode['Hall 1'],
        datetime: '2025-11-03 14:00',
        status: 'Pending'
    }
];

const scheduleSeed = [
    {
        id: 'SCH-101',
        hall: hallNameByCode['219'],
        course: 'Math Bridge Workshop',
        faculty: 'College Advising Team',
        day: 'Mon',
        start: '09:00',
        end: '10:30'
    },
    {
        id: 'SCH-102',
        hall: hallNameByCode['Hall A'],
        course: 'Architectural Critique',
        faculty: 'School of Architecture',
        day: 'Tue',
        start: '11:00',
        end: '13:00'
    },
    {
        id: 'SCH-103',
        hall: hallNameByCode['500'],
        course: 'Faculty Senate Meeting',
        faculty: 'University Senate',
        day: 'Thu',
        start: '15:00',
        end: '17:00'
    }
];

const hallTypes = ['Lecture Hall', 'Classroom', 'Auditorium', 'Grand Auditorium', 'Lab'];
const hallStatuses = ['Available', 'Reserved', 'Under Maintenance'];
const daysOfWeek = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri'];
const SCHEDULE_START = '08:00';
const SCHEDULE_END = '18:00';
const SCHEDULE_STEP = 30;
const MIN_EVENT_DISPLAY_MINUTES = 30;
const SLOT_PIXEL_HEIGHT = 64;

const hallPaletteByBuilding = {
    'Main Building': '#6a7dff',
    'Credit Building': '#3bc5f2',
    'Architect Annex': '#39c18d',
    'Architecture Building': '#ff8aa6'
};

const defaultHallForm = {
    name: '',
    code: '',
    type: hallTypes[0],
    capacity: '',
    building: '',
    resources: '',
    status: 'Available'
};

const defaultBooking = {
    hall: hallSeed[0]?.name || '',
    course: '',
    faculty: '',
    day: 'Mon',
    start: '09:00',
    end: '10:00'
};
const toMinutes = (time) => {
    if (!time) return 0;
    const [hours = '0', mins = '0'] = time.split(':');
    return Number(hours) * 60 + Number(mins);
};

const padTime = (value) => String(value).padStart(2, '0');

const minutesToTime = (minutes) => {
    const hrs = Math.floor(minutes / 60);
    const mins = minutes % 60;
    return `${padTime(hrs)}:${padTime(mins)}`;
};

const buildTimeline = (start, end, step) => {
    const slots = [];
    let cursor = toMinutes(start);
    const endMinutes = toMinutes(end);
    while (cursor <= endMinutes) {
        const time = minutesToTime(cursor);
        slots.push({
            time,
            label: cursor % 60 === 0 ? time : ''
        });
        cursor += step;
    }
    return slots;
};

const layoutDayEvents = (events, startBound, endBound) => {
    const totalSpan = Math.max(endBound - startBound, 1);
    const prepared = events
        .map((event) => {
            const start = toMinutes(event.start);
            const end = toMinutes(event.end);
            if (!Number.isFinite(start) || !Number.isFinite(end) || end <= start) {
                return null;
            }
            if (end <= startBound || start >= endBound) {
                return null;
            }
            const clampedStart = Math.max(start, startBound);
            const clampedEnd = Math.min(end, endBound);
            const rawHeight = ((clampedEnd - clampedStart) / totalSpan) * 100;
            const minHeight = Math.min((MIN_EVENT_DISPLAY_MINUTES / totalSpan) * 100, 100);
            const top = ((clampedStart - startBound) / totalSpan) * 100;
            const height = Math.min(Math.max(rawHeight, minHeight), 100 - top);
            return {
                event,
                start,
                end,
                top,
                height
            };
        })
        .filter(Boolean)
        .sort((a, b) => a.start - b.start || a.end - b.end);

    const laneEndTimes = [];
    const withLane = prepared.map((item) => {
        let laneIndex = laneEndTimes.findIndex((finish) => finish <= item.start);
        if (laneIndex === -1) {
            laneIndex = laneEndTimes.length;
            laneEndTimes.push(item.end);
        } else {
            laneEndTimes[laneIndex] = item.end;
        }
        return { ...item, laneIndex };
    });

    return withLane.map((item, _, collection) => {
        const overlaps = collection.filter((other) =>
            other !== item && other.start < item.end && item.start < other.end
        );
        const lanes = new Set([item.laneIndex, ...overlaps.map((o) => o.laneIndex)]);
        const columns = lanes.size || 1;
        const ordered = Array.from(lanes).sort((a, b) => a - b);
        const columnIndex = ordered.indexOf(item.laneIndex);
        return {
            ...item.event,
            layout: {
                top: item.top,
                height: item.height,
                columns,
                columnIndex: columnIndex === -1 ? 0 : columnIndex
            }
        };
    });
};

const hasConflict = (booking, list) =>
    list.some((item) =>
        item.id !== booking.id &&
        item.hall === booking.hall &&
        item.day === booking.day &&
        toMinutes(item.start) < toMinutes(booking.end) &&
        toMinutes(booking.start) < toMinutes(item.end)
    );
const Facilities = () => {
    const [halls, setHalls] = useState(hallSeed);
    const [hallQuery, setHallQuery] = useState('');
    const [hallStatusFilter, setHallStatusFilter] = useState('All');
    const [hallModal, setHallModal] = useState({ open: false, mode: 'add', form: defaultHallForm, id: null });
    const [deletePrompt, setDeletePrompt] = useState({ open: false, hall: null });

    const [requests, setRequests] = useState(requestSeed);
    const [requestFilters, setRequestFilters] = useState({ status: 'Pending', faculty: '', date: '' });
    const [requestAction, setRequestAction] = useState({ open: false, action: 'approve', request: null, comment: '' });

    const [schedule, setSchedule] = useState(scheduleSeed);
    const [calendarFilter, setCalendarFilter] = useState('All');
    const [bookingModal, setBookingModal] = useState({ open: false, editing: null, form: defaultBooking });

    const filteredHalls = useMemo(() => {
        const query = hallQuery.trim().toLowerCase();
        return halls.filter((hall) => {
            const matchesQuery = !query ||
                hall.name.toLowerCase().includes(query) ||
                hall.building.toLowerCase().includes(query) ||
                hall.code.toLowerCase().includes(query);
            const matchesStatus = hallStatusFilter === 'All' || hall.status === hallStatusFilter;
            return matchesQuery && matchesStatus;
        });
    }, [halls, hallQuery, hallStatusFilter]);

    const campusSnapshot = useMemo(() => {
        const totalCapacity = halls.reduce((sum, hall) => sum + Number(hall.capacity || 0), 0);
        const statusBreakdown = hallStatuses.map((status) => ({
            status,
            count: halls.filter((hall) => hall.status === status).length
        }));
        const buildingSummary = halls.reduce((acc, hall) => {
            if (!acc[hall.building]) {
                acc[hall.building] = { count: 0, capacity: 0 };
            }
            acc[hall.building].count += 1;
            acc[hall.building].capacity += Number(hall.capacity || 0);
            return acc;
        }, {});
        const buildingRows = Object.entries(buildingSummary)
            .map(([building, info]) => ({ building, ...info }))
            .sort((a, b) => b.capacity - a.capacity);
        return {
            totalHalls: halls.length,
            totalCapacity,
            statusBreakdown,
            buildingRows
        };
    }, [halls]);

    const filteredRequests = useMemo(() => {
        return requests.filter((req) => {
            const matchesStatus = requestFilters.status === 'All' || req.status === requestFilters.status;
            const matchesFaculty = !requestFilters.faculty || req.faculty.toLowerCase().includes(requestFilters.faculty.toLowerCase());
            const matchesDate = !requestFilters.date || req.datetime.startsWith(requestFilters.date);
            return matchesStatus && matchesFaculty && matchesDate;
        });
    }, [requests, requestFilters]);

    const scheduleWindow = useMemo(() => ({
        start: toMinutes(SCHEDULE_START),
        end: toMinutes(SCHEDULE_END)
    }), []);

    const timelineSlots = useMemo(
        () => buildTimeline(SCHEDULE_START, SCHEDULE_END, SCHEDULE_STEP),
        []
    );

    const timelineHeight = useMemo(
        () => Math.max((timelineSlots.length - 1) * SLOT_PIXEL_HEIGHT, SLOT_PIXEL_HEIGHT),
        [timelineSlots]
    );

    const scheduleWithConflicts = useMemo(
        () => schedule.map((event) => ({ ...event, conflict: hasConflict(event, schedule) })),
        [schedule]
    );

    const filteredSchedule = useMemo(() => {
        const base = calendarFilter === 'All'
            ? scheduleWithConflicts
            : scheduleWithConflicts.filter((event) => {
                const hall = halls.find((h) => h.name === event.hall);
                return hall ? hall.building === calendarFilter : false;
            });

        return [...base].sort((a, b) => {
            const dayIndexA = daysOfWeek.indexOf(a.day);
            const dayIndexB = daysOfWeek.indexOf(b.day);
            if (dayIndexA !== dayIndexB) {
                return dayIndexA - dayIndexB;
            }
            return toMinutes(a.start) - toMinutes(b.start);
        });
    }, [scheduleWithConflicts, calendarFilter, halls]);

    const eventsByDay = useMemo(() => {
        const grouped = {};
        daysOfWeek.forEach((day) => {
            const dayEvents = filteredSchedule.filter((event) => event.day === day);
            grouped[day] = layoutDayEvents(dayEvents, scheduleWindow.start, scheduleWindow.end);
        });
        return grouped;
    }, [filteredSchedule, scheduleWindow]);
    const openHallModal = (hall) => {
        if (hall) {
            setHallModal({ open: true, mode: 'edit', form: { ...hall }, id: hall.id });
        } else {
            setHallModal({ open: true, mode: 'add', form: defaultHallForm, id: null });
        }
    };

    const submitHallForm = (e) => {
        e.preventDefault();
        const payload = {
            ...hallModal.form,
            name: hallModal.form.name.trim(),
            code: (hallModal.form.code || hallModal.form.name).trim(),
            building: hallModal.form.building.trim(),
            capacity: Number(hallModal.form.capacity || 0)
        };

        if (!payload.name || !payload.building || payload.capacity <= 0) {
            return;
        }

        if (hallModal.mode === 'edit' && hallModal.id) {
            setHalls((prev) => prev.map((hall) => (hall.id === hallModal.id ? { ...hall, ...payload } : hall)));
        } else {
            const newHall = {
                ...payload,
                id: `H-${Math.floor(Math.random() * 900 + 100)}`
            };
            setHalls((prev) => [newHall, ...prev]);
        }

        setHallModal({ open: false, mode: 'add', form: defaultHallForm, id: null });
    };

    const deleteHall = () => {
        if (!deletePrompt.hall) return;
        setHalls((prev) => prev.filter((hall) => hall.id !== deletePrompt.hall.id));
        setDeletePrompt({ open: false, hall: null });
    };

    const openRequestAction = (request, action) => {
        setRequestAction({ open: true, action, request, comment: '' });
    };

    const submitRequestAction = (e) => {
        e.preventDefault();
        if (!requestAction.request) return;

        const nextStatus = requestAction.action === 'approve' ? 'Approved' : 'Rejected';
        setRequests((prev) => prev.map((req) =>
            req.id === requestAction.request.id
                ? { ...req, status: nextStatus, comment: requestAction.comment }
                : req
        ));

        if (nextStatus === 'Approved') {
            setHalls((prev) => prev.map((hall) =>
                hall.name === requestAction.request.hall ? { ...hall, status: 'Reserved' } : hall
            ));
        }

        setRequestAction({ open: false, action: 'approve', request: null, comment: '' });
    };

    const openBookingModal = (booking) => {
        if (booking) {
            const { id, hall, course, faculty, day, start, end } = booking;
            setBookingModal({ open: true, editing: id, form: { hall, course, faculty, day, start, end } });
        } else {
            setBookingModal({ open: true, editing: null, form: { ...defaultBooking, hall: halls[0]?.name || defaultBooking.hall } });
        }
    };

    const submitBooking = (e) => {
        e.preventDefault();
        const payload = {
            ...bookingModal.form,
            id: bookingModal.editing || `SCH-${Math.floor(Math.random() * 9000 + 1000)}`
        };

        const startMinutes = toMinutes(payload.start);
        const endMinutes = toMinutes(payload.end);

        if (endMinutes <= startMinutes) {
            alert('End time must be after the start time.');
            return;
        }

        if (!daysOfWeek.includes(payload.day)) {
            payload.day = daysOfWeek[0];
        }

        if (!payload.hall) {
            payload.hall = halls[0]?.name || '';
        }

        const conflict = hasConflict(payload, schedule);
        payload.conflict = conflict;

        if (conflict) {
            const proceed = window.confirm('This booking conflicts with another reservation for this hall. Save anyway?');
            if (!proceed) {
                return;
            }
        }

        if (bookingModal.editing) {
            setSchedule((prev) => prev.map((item) => (item.id === bookingModal.editing ? payload : item)));
        } else {
            setSchedule((prev) => [...prev, payload]);
        }

        setBookingModal({ open: false, editing: null, form: defaultBooking });
    };

    const deleteBooking = () => {
        if (!bookingModal.editing) return;
        setSchedule((prev) => prev.filter((item) => item.id !== bookingModal.editing));
        setBookingModal({ open: false, editing: null, form: defaultBooking });
    };

    const getEventColor = (hallName) => {
        const hall = halls.find((h) => h.name === hallName);
        if (!hall) return '#6a7dff';
        return hallPaletteByBuilding[hall.building] || '#6a7dff';
    };

    const exportSchedule = (format) => {
        console.log(`Export to ${format} triggered`, filteredSchedule);
        alert(`Export to ${format} will be connected to the backend.`);
    };
    return (
        <div className="facilities-shell">
            <header className="facilities-topline" role="banner">
                <div className="topline-brand">
                    <div className="brand-logo-shell">
                        <img src={umsLogo} alt="UMS logo" className="mini-logo" />
                    </div>
                    <div>
                        <p className="eyebrow">University Management - Admin</p>
                        <h1>Facilities Command Center</h1>
                        <p className="sub">Manage halls, approvals, and schedules in one glassy surface.</p>
                    </div>
                </div>
                <button className="ghost-btn" onClick={() => openHallModal()}>
                    + Add Hall
                </button>
            </header>

            <section className="section-card inventory">
                <header className="section-head">
                    <div>
                        <h2>Campus Inventory Snapshot</h2>
                        <p>Quick glance over capacity, utilisation status, and building load.</p>
                    </div>
                </header>
                <div className="inventory-grid">
                    <div className="inventory-stat">
                        <span className="label">Total Halls</span>
                        <strong>{campusSnapshot.totalHalls}</strong>
                        <span className="sub">Across {campusSnapshot.buildingRows.length} buildings</span>
                    </div>
                    <div className="inventory-stat">
                        <span className="label">Aggregate Seating</span>
                        <strong>{campusSnapshot.totalCapacity}</strong>
                        <span className="sub">Seats available campus-wide</span>
                    </div>
                    <div className="inventory-status">
                        {campusSnapshot.statusBreakdown.map(({ status, count }) => (
                            <span key={status} className={`status-chip status-${status.replace(/\s/g, '').toLowerCase()}`}>
                {status} - {count}
              </span>
                        ))}
                    </div>
                    <div className="inventory-buildings">
                        {campusSnapshot.buildingRows.map((row) => (
                            <div key={row.building} className="building-row">
                                <div>
                                    <strong>{row.building}</strong>
                                    <span>{row.count} halls</span>
                                </div>
                                <span className="capacity">{row.capacity} seats</span>
                            </div>
                        ))}
                    </div>
                </div>
            </section>

            <section className="section-card">
                <header className="section-head">
                    <div>
                        <h2>Hall Directory</h2>
                        <p>Manage every hall, lab, and auditorium - create, edit, retire.</p>
                    </div>
                    <div className="filters">
                        <input
                            type="search"
                            placeholder="Search by hall, code or building"
                            value={hallQuery}
                            onChange={(e) => setHallQuery(e.target.value)}
                        />
                        <select value={hallStatusFilter} onChange={(e) => setHallStatusFilter(e.target.value)}>
                            <option value="All">All statuses</option>
                            {hallStatuses.map((status) => (
                                <option key={status} value={status}>{status}</option>
                            ))}
                        </select>
                    </div>
                </header>

                <div className="table">
                    <div className="table-row table-head">
                        <span>Hall</span>
                        <span>Type</span>
                        <span>Capacity</span>
                        <span>Building</span>
                        <span>Status</span>
                        <span>Resources</span>
                        <span>Actions</span>
                    </div>
                    {filteredHalls.map((hall) => (
                        <div className="table-row" key={hall.id}>
              <span>
                <strong>{hall.name}</strong>
                <small>{hall.code}</small>
              </span>
                            <span>{hall.type}</span>
                            <span>{hall.capacity}</span>
                            <span>{hall.building}</span>
                            <span>
                <span className={`status-pill status-${hall.status.replace(/\s/g, '').toLowerCase()}`}>
                  {hall.status}
                </span>
              </span>
                            <span className="resources">{hall.resources || '--'}</span>
                            <span className="row-actions">
                <button onClick={() => openHallModal(hall)} aria-label={`Edit ${hall.name}`}>Edit</button>
                <button onClick={() => setDeletePrompt({ open: true, hall })} aria-label={`Delete ${hall.name}`}>Delete</button>
              </span>
                        </div>
                    ))}
                    {!filteredHalls.length && (
                        <div className="table-row empty">
                            <span>No halls match the current filters.</span>
                        </div>
                    )}
                </div>
            </section>
            <section className="section-card">
                <header className="section-head">
                    <div>
                        <h2>Reservation Requests</h2>
                        <p>Approve or reject upcoming hall bookings from faculty.</p>
                    </div>
                    <div className="filters">
                        <select value={requestFilters.status} onChange={(e) => setRequestFilters((prev) => ({ ...prev, status: e.target.value }))}>
                            {['All', 'Pending', 'Approved', 'Rejected'].map((status) => (
                                <option key={status} value={status}>{status}</option>
                            ))}
                        </select>
                        <input
                            type="search"
                            placeholder="Filter by faculty"
                            value={requestFilters.faculty}
                            onChange={(e) => setRequestFilters((prev) => ({ ...prev, faculty: e.target.value }))}
                        />
                        <input
                            type="date"
                            value={requestFilters.date}
                            onChange={(e) => setRequestFilters((prev) => ({ ...prev, date: e.target.value }))}
                        />
                    </div>
                </header>

                <div className="table">
                    <div className="table-row table-head">
                        <span>Request</span>
                        <span>Faculty / Course</span>
                        <span>Hall</span>
                        <span>Date & Time</span>
                        <span>Status</span>
                        <span>Actions</span>
                    </div>
                    {filteredRequests.map((request) => (
                        <div className="table-row" key={request.id}>
                            <span><strong>{request.id}</strong></span>
                            <span>
                <strong>{request.faculty}</strong>
                <small>{request.course}</small>
              </span>
                            <span>{request.hall}</span>
                            <span>{request.datetime.replace(' ', ' @ ')}</span>
                            <span>
                <span className={`status-pill status-${request.status.toLowerCase()}`}>{request.status}</span>
              </span>
                            <span className="row-actions">
                <button onClick={() => openRequestAction(request, 'approve')} aria-label="Approve request">Approve</button>
                <button onClick={() => openRequestAction(request, 'reject')} aria-label="Reject request">Reject</button>
              </span>
                        </div>
                    ))}
                    {!filteredRequests.length && (
                        <div className="table-row empty">
                            <span>No requests in this filter.</span>
                        </div>
                    )}
                </div>
            </section>

            <section className="section-card">
                <header className="section-head schedule-head">
                    <div>
                        <h2>Hall Scheduling</h2>
                        <p>Assign halls to courses and monitor conflicts in real time.</p>
                    </div>
                    <div className="schedule-controls">
                        <select value={calendarFilter} onChange={(e) => setCalendarFilter(e.target.value)}>
                            <option value="All">All halls</option>
                            {[...new Set(halls.map((hall) => hall.building))].map((building) => (
                                <option key={building} value={building}>{building}</option>
                            ))}
                        </select>
                        <button className="ghost-btn" onClick={() => openBookingModal()}>Assign Hall</button>
                        <div className="export">
                            <button onClick={() => exportSchedule('PDF')}>Export PDF</button>
                            <button onClick={() => exportSchedule('Excel')}>Export Excel</button>
                        </div>
                    </div>
                </header>

                <div className="schedule-legend">
                    <span><i className="dot available" />Available slot</span>
                    <span><i className="dot booked" />Booked</span>
                    <span><i className="dot conflict" />Conflict</span>
                </div>

                <div className="schedule-grid" role="grid">
                    <div className="schedule-grid-head" role="row">
                        <div className="time-column head" role="columnheader">Time</div>
                        {daysOfWeek.map((day) => (
                            <div className="day-column head" key={day} role="columnheader">{day}</div>
                        ))}
                    </div>

                    <div className="schedule-grid-body" style={{ height: `${timelineHeight}px` }}>
                        <div className="time-column" aria-hidden="true">
                            {timelineSlots.slice(0, -1).map((slot) => (
                                <div className="time-slot" key={slot.time}>
                                    {slot.label ? <span>{slot.label}</span> : <span className="time-tick" />}
                                </div>
                            ))}
                            <div className="time-slot time-slot-end">
                                <span>{timelineSlots[timelineSlots.length - 1]?.time}</span>
                            </div>
                        </div>

                        {daysOfWeek.map((day) => (
                            <div className="day-column" key={day} role="gridcell">
                                <div className="slot-stripes" aria-hidden="true">
                                    {timelineSlots.slice(0, -1).map((slot, index) => (
                                        <span className="slot-stripe" key={`${day}-stripe-${slot.time}-${index}`} />
                                    ))}
                                </div>

                                {(eventsByDay[day] || []).map((event) => {
                                    const layout = event.layout;
                                    if (!layout) return null;

                                    const widthPercent = 100 / layout.columns;
                                    const leftPercent = widthPercent * layout.columnIndex;
                                    const spacing = layout.columns > 1 ? 8 : 0;
                                    const inset = 8;
                                    const computedLeft = `calc(${leftPercent}% + ${(layout.columnIndex * spacing) + inset}px)`;
                                    const computedWidth = `calc(${widthPercent}% - ${inset * 2 + spacing}px)`;

                                    return (
                                        <button
                                            key={event.id}
                                            className="calendar-event"
                                            data-conflict={event.conflict}
                                            style={{
                                                top: `calc(${layout.top}% + 2px)`,
                                                height: `calc(${layout.height}% - 4px)`,
                                                left: computedLeft,
                                                width: computedWidth,
                                                background: `linear-gradient(145deg, rgba(255,255,255,0.85), ${getEventColor(event.hall)})`
                                            }}
                                            title={`${event.course} - ${event.hall} - ${event.start} - ${event.end}`}
                                            onClick={() => openBookingModal(event)}
                                        >
                                            <strong>{event.course}</strong>
                                            <span className="event-meta">{event.hall}</span>
                                            <small>{event.start} - {event.end}</small>
                                            <small>{event.faculty}</small>
                                            {event.conflict && <span className="event-conflict">Conflict</span>}
                                        </button>
                                    );
                                })}

                                {!eventsByDay[day]?.length && (
                                    <div className="no-events-hint">No bookings</div>
                                )}
                            </div>
                        ))}
                    </div>
                </div>
            </section>
            {hallModal.open && (
                <div className="modal" role="dialog" aria-modal="true">
                    <form className="modal-card" onSubmit={submitHallForm}>
                        <h3>{hallModal.mode === 'edit' ? 'Edit Hall' : 'Add Hall'}</h3>
                        <label>
                            Name
                            <input value={hallModal.form.name} onChange={(e) => setHallModal((prev) => ({ ...prev, form: { ...prev.form, name: e.target.value } }))} required />
                        </label>
                        <label>
                            Code
                            <input value={hallModal.form.code} onChange={(e) => setHallModal((prev) => ({ ...prev, form: { ...prev.form, code: e.target.value } }))} placeholder="Optional code" />
                        </label>
                        <label>
                            Type
                            <select value={hallModal.form.type} onChange={(e) => setHallModal((prev) => ({ ...prev, form: { ...prev.form, type: e.target.value } }))}>
                                {hallTypes.map((type) => (
                                    <option key={type} value={type}>{type}</option>
                                ))}
                            </select>
                        </label>
                        <label>
                            Capacity
                            <input type="number" value={hallModal.form.capacity} onChange={(e) => setHallModal((prev) => ({ ...prev, form: { ...prev.form, capacity: e.target.value } }))} required />
                        </label>
                        <label>
                            Building / Location
                            <input value={hallModal.form.building} onChange={(e) => setHallModal((prev) => ({ ...prev, form: { ...prev.form, building: e.target.value } }))} required />
                        </label>
                        <label>
                            Resources
                            <textarea value={hallModal.form.resources} onChange={(e) => setHallModal((prev) => ({ ...prev, form: { ...prev.form, resources: e.target.value } }))} placeholder="Optional" />
                        </label>
                        <label>
                            Status
                            <select value={hallModal.form.status} onChange={(e) => setHallModal((prev) => ({ ...prev, form: { ...prev.form, status: e.target.value } }))}>
                                {hallStatuses.map((status) => (
                                    <option key={status} value={status}>{status}</option>
                                ))}
                            </select>
                        </label>
                        <div className="modal-actions">
                            <button type="button" className="ghost" onClick={() => setHallModal({ open: false, mode: 'add', form: defaultHallForm, id: null })}>Cancel</button>
                            <button type="submit">Save Hall</button>
                        </div>
                    </form>
                </div>
            )}

            {deletePrompt.open && (
                <div className="modal" role="alertdialog" aria-modal="true">
                    <div className="modal-card">
                        <h3>Delete hall?</h3>
                        <p>"{deletePrompt.hall?.name}" will be removed from the directory. You can re-create it later.</p>
                        <div className="modal-actions">
                            <button className="ghost" onClick={() => setDeletePrompt({ open: false, hall: null })}>Cancel</button>
                            <button className="danger" onClick={deleteHall}>Delete</button>
                        </div>
                    </div>
                </div>
            )}

            {requestAction.open && (
                <div className="modal" role="dialog" aria-modal="true">
                    <form className="modal-card" onSubmit={submitRequestAction}>
                        <h3>{requestAction.action === 'approve' ? 'Approve Request' : 'Reject Request'}</h3>
                        <p>Request {requestAction.request?.id} - {requestAction.request?.course}</p>
                        <label>
                            Comment (optional)
                            <textarea value={requestAction.comment} onChange={(e) => setRequestAction((prev) => ({ ...prev, comment: e.target.value }))} placeholder="Notes for the faculty" />
                        </label>
                        <div className="modal-actions">
                            <button type="button" className="ghost" onClick={() => setRequestAction({ open: false, action: 'approve', request: null, comment: '' })}>Cancel</button>
                            <button type="submit">{requestAction.action === 'approve' ? 'Approve' : 'Reject'}</button>
                        </div>
                    </form>
                </div>
            )}

            {bookingModal.open && (
                <div className="modal" role="dialog" aria-modal="true">
                    <form className="modal-card" onSubmit={submitBooking}>
                        <h3>{bookingModal.editing ? 'Edit Booking' : 'Assign Hall'}</h3>
                        <label>
                            Course / Event
                            <input value={bookingModal.form.course} onChange={(e) => setBookingModal((prev) => ({ ...prev, form: { ...prev.form, course: e.target.value } }))} required />
                        </label>
                        <label>
                            Faculty / Owner
                            <input value={bookingModal.form.faculty} onChange={(e) => setBookingModal((prev) => ({ ...prev, form: { ...prev.form, faculty: e.target.value } }))} required />
                        </label>
                        <label>
                            Hall
                            <select value={bookingModal.form.hall} onChange={(e) => setBookingModal((prev) => ({ ...prev, form: { ...prev.form, hall: e.target.value } }))}>
                                {halls.map((hall) => (
                                    <option key={hall.id} value={hall.name}>{hall.name}</option>
                                ))}
                            </select>
                        </label>
                        <div className="two-up">
                            <label>
                                Day
                                <select value={bookingModal.form.day} onChange={(e) => setBookingModal((prev) => ({ ...prev, form: { ...prev.form, day: e.target.value } }))}>
                                    {daysOfWeek.map((day) => (
                                        <option key={day} value={day}>{day}</option>
                                    ))}
                                </select>
                            </label>
                            <label>
                                Start
                                <input type="time" value={bookingModal.form.start} onChange={(e) => setBookingModal((prev) => ({ ...prev, form: { ...prev.form, start: e.target.value } }))} required />
                            </label>
                            <label>
                                End
                                <input type="time" value={bookingModal.form.end} onChange={(e) => setBookingModal((prev) => ({ ...prev, form: { ...prev.form, end: e.target.value } }))} required />
                            </label>
                        </div>
                        <label>
                            Recurrence (optional)
                            <input placeholder="e.g., Every Monday for 6 weeks" />
                        </label>
                        <div className="modal-actions">
                            {bookingModal.editing && (
                                <button type="button" className="danger" onClick={deleteBooking}>Remove</button>
                            )}
                            <button type="button" className="ghost" onClick={() => setBookingModal({ open: false, editing: null, form: defaultBooking })}>Cancel</button>
                            <button type="submit">Save Booking</button>
                        </div>
                    </form>
                </div>
            )}
        </div>
    );
};

export default Facilities;
