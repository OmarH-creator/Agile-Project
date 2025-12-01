import React, { useMemo, useState, useEffect,useRef } from 'react';
import './FacilitiesUI.css';
import umsLogo from '../../assets/UMS Logo.png';
import html2canvas from "html2canvas";
import jsPDF from "jspdf";

// API Configuration
const API_BASE_URL = 'http://localhost:8081/api/admin';

const getAuthToken = () => {
    return localStorage.getItem('token');
};

const createHeaders = () => {
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${getAuthToken()}`
    };
};

// Hall API Functions
const hallAPI = {
    createHall: async (hallData) => {
        const response = await fetch(`${API_BASE_URL}/halls`, {
            method: 'POST',
            headers: createHeaders(),
            body: JSON.stringify({
                hallName: hallData.name,
                capacity: parseInt(hallData.capacity)
            })
        });
        if (!response.ok) {
            const error = await response.text();
            throw new Error(error);
        }
        return await response.text();
    },

    deleteHall: async (hallName) => {
        const response = await fetch(`${API_BASE_URL}/halls/${encodeURIComponent(hallName)}`, {
            method: 'DELETE',
            headers: createHeaders()
        });
        if (!response.ok) {
            const error = await response.text();
            throw new Error(error);
        }
        return await response.text();
    },

    fetchHalls: async () => {
        const response = await fetch(`${API_BASE_URL}/halls`, {
            method: 'GET',
            headers: createHeaders()
        });
        if (!response.ok) {
            const error = await response.text();
            throw new Error(error);
        }
        return await response.json();
    },

    gethall: async (hallName) => {
        const response = await fetch(`${API_BASE_URL}/halls/${encodeURIComponent(hallName)}`, {
            method: 'GET',
            headers: createHeaders()
        });
        if (!response.ok) {
            const error = await response.text();
            throw new Error(error);
        }
        return await response.json();
    },

    // Expects originalName for the URL, and hallData for the Body
    updateHall: async (originalName, hallData) => {
        const response = await fetch(`${API_BASE_URL}/halls/${encodeURIComponent(originalName)}`, {
            method: 'PUT',
            headers: createHeaders(),
            body: JSON.stringify({
                hallName: hallData.name, // The NEW name
                capacity: parseInt(hallData.capacity)
            })
        });
        if (!response.ok) {
            const error = await response.text();
            throw new Error(error);
        }
        return await response.text();
    },
};

// --- DATE CONVERSION HELPERS ---
// Frontend (Day/Time) -> Backend (Date Object)
const convertToBackendDate = (day, time) => {
    const dayMap = { 'Mon': 1, 'Tue': 2, 'Wed': 3, 'Thu': 4, 'Fri': 5 };
    const today = new Date();
    const currentDay = today.getDay();
    const targetDay = dayMap[day] || 1;

    let daysToAdd = targetDay - currentDay;
    if (daysToAdd < 0) daysToAdd += 7;

    const targetDate = new Date(today);
    targetDate.setDate(today.getDate() + daysToAdd);

    const [hours, minutes] = time.split(':');

    // CRITICAL: Set Seconds and MS to 0 to ensure clean :00 or :30 slots
    targetDate.setHours(parseInt(hours), parseInt(minutes), 0, 0);

    return targetDate;
};

// Backend (ISO String) -> Frontend (Day/Time)
const parseBackendDate = (isoString) => {
    if (!isoString) return { day: 'Mon', time: '00:00' };

    const date = new Date(isoString);

    const day = date.toLocaleDateString('en-US', { weekday: 'short' });

    // Get Time in 24h format (HH:mm) using local timezone
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');

    const time = `${hours}:${minutes}`;

    return { day, time };
};

// Booking API Functions
const bookingAPI = {
    createBooking: async (bookingData) => {
        const response = await fetch(`${API_BASE_URL}/halls/book`, {
            method: 'POST',
            headers: createHeaders(),
            body: JSON.stringify({
                hallName: bookingData.hallName,
                start: bookingData.start,
                end: bookingData.end,
                purpose: bookingData.purpose,
                reservationId: bookingData.reservationId,
                staffId: bookingData.staffId
            })
        });
        if (!response.ok) {
            const error = await response.text();
            throw new Error(error);
        }
        return await response.text();
    },
    updateBooking: async (id, bookingData) => {
        const response = await fetch(`${API_BASE_URL}/bookings/${id}`, {
            method: 'PUT',
            headers: createHeaders(),
            body: JSON.stringify({
                hallName: bookingData.hallName,
                start: bookingData.start,
                end: bookingData.end,
                purpose: bookingData.purpose,
                staffId: bookingData.staffId
            })
        });
        if (!response.ok) throw new Error(await response.text());
        return await response.text();
    },

    deleteBooking: async (id) => {
        const response = await fetch(`${API_BASE_URL}/bookings/${id}`, {
            method: 'DELETE',
            headers: createHeaders()
        });
        if (!response.ok) throw new Error(await response.text());
        return await response.text();
    },
    fetchBookings: async () => {
        const response = await fetch(`${API_BASE_URL}/bookings`, {
            method: 'GET',
            headers: createHeaders()
        });
        if (!response.ok) {
            const error = await response.text();
            throw new Error(error);
        }
        return await response.json();
    }
};

const hallTypes = ['Lecture Hall', 'Classroom', 'Auditorium', 'Grand Auditorium', 'Lab'];
const hallStatuses = ['Available', 'Reserved', 'Under Maintenance'];
const daysOfWeek = ['Sat','Mon', 'Tue', 'Wed', 'Thu'];
const SCHEDULE_START = '08:00';
const SCHEDULE_END = '19:00';
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
    hall: '',
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
    const [halls, setHalls] = useState([]);
    const [hallQuery, setHallQuery] = useState('');
    const [hallStatusFilter, setHallStatusFilter] = useState('All');
    const [hallModal, setHallModal] = useState({ open: false, mode: 'add', form: defaultHallForm, id: null });
    const [deletePrompt, setDeletePrompt] = useState({ open: false, hall: null });

    const [requests, setRequests] = useState([]);
    const [requestFilters, setRequestFilters] = useState({ status: 'Pending', faculty: '', date: '' });
    const [requestAction, setRequestAction] = useState({ open: false, action: 'approve', request: null, comment: '' });

    const [schedule, setSchedule] = useState([]);
    const [calendarFilter, setCalendarFilter] = useState('All');
    const [bookingModal, setBookingModal] = useState({ open: false, editing: null, form: defaultBooking });

    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);
    const scheduleRef = useRef(null);

    const to12Hour = (time24) => {
        if (!time24) return "";
        let [hour, minute] = time24.split(":").map(Number);
        const ampm = hour >= 12 ? "PM" : "AM";
        hour = hour % 12 || 12;
        return `${hour}:${minute.toString().padStart(2, "0")} ${ampm}`;
    };

    const exportScheduleAsPDF = async () => {
        if (!scheduleRef.current) return;

        const element = scheduleRef.current;

        const canvas = await html2canvas(element, { scale: 2 });
        const imgData = canvas.toDataURL("image/png");

        const pdf = new jsPDF("landscape", "mm", "a4");
        const pdfWidth = pdf.internal.pageSize.getWidth();
        const pdfHeight = (canvas.height * pdfWidth) / canvas.width;

        pdf.addImage(imgData, "PNG", 0, 0, pdfWidth, pdfHeight);
        pdf.save("schedule.pdf");
    };

    const handleAPICall = async (apiFunction, successCallback) => {
        setLoading(true);
        setError(null);
        try {
            const result = await apiFunction();
            setLoading(false);
            if (successCallback) {
                successCallback(result);
            }
            return result;
        } catch (err) {
            setLoading(false);
            let errorMessage = err.message || 'An error occurred';
            if (errorMessage.includes('401')) errorMessage = 'Authentication failed.';
            else if (errorMessage.includes('404')) errorMessage = 'Resource not found.';
            else if (errorMessage.includes('500')) errorMessage = 'Server error.';
            else if (errorMessage.includes('Failed to fetch')) errorMessage = 'Connection error.';

            setError(errorMessage);
            throw err;
        }
    };

    // Helper to map backend hall object to frontend structure
    const mapBackendHall = (h) => ({
        // Use a stable ID fallback (hallName) to prevent React key errors
        id: h.hallId || h.id || `hall-${h.hallName || h.name || 'unknown'}`,
        name: h.hallName || h.name,
        capacity: h.capacity,
        code: h.code || (h.hallName || h.name),
        building: h.building || 'Main Building',
        type: h.type || 'Lecture Hall',
        status: h.status || 'Available',
        resources: h.resources || ''
    });

    // Helper to map backend booking to frontend schedule event
    const mapBackendBooking = (b) => {

        const startStr = b.start || b.startTime;
        const endStr = b.end || b.endTime;

        const startInfo = parseBackendDate(startStr);
        const endInfo = parseBackendDate(endStr);

        return {
            id: `SCH-${b.reservationId}`,
            hall: b.hallName,
            course: b.purpose || 'Event',
            faculty: b.staffId || 'Staff',
            day: startInfo.day,
            start: startInfo.time,
            end: endInfo.time,
            // Keep raw Reservation ID for updates
            reservationId: b.reservationId
        };
    };

    const loadAllHalls = async () => {
        await handleAPICall(
            () => hallAPI.fetchHalls(),
            (data) => {
                const list = Array.isArray(data) ? data : [];
                const formatted = list.map(mapBackendHall);
                setHalls(formatted);
            }
        );
    };

    const loadAllBookings = async () => {
        await handleAPICall(
            () => bookingAPI.fetchBookings(),
            (data) => {
                const list = Array.isArray(data) ? data : [];
                const formatted = list.map(mapBackendBooking);
                setSchedule(formatted);
            }
        );
    };

    useEffect(() => {
        loadAllHalls();
        loadAllBookings();
    }, []);

    const handleSearch = async (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            if (!hallQuery.trim()) {
                await loadAllHalls();
                return;
            }
            await handleAPICall(
                () => hallAPI.gethall(hallQuery.trim()),
                (result) => {
                    if (result) {
                        const singleHall = Array.isArray(result) ? result : [result];
                        const formatted = singleHall.map(mapBackendHall);
                        setHalls(formatted);
                    } else {
                        setHalls([]);
                    }
                }
            );
        }
    };

    const filteredHalls = useMemo(() => {
        return halls.filter((hall) => {
            const matchesStatus = hallStatusFilter === 'All' || hall.status === hallStatusFilter;
            return matchesStatus;
        });
    }, [halls, hallStatusFilter]);

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

    const submitHallForm = async (e) => {
        e.preventDefault();
        const payload = {
            name: hallModal.form.name.trim(),
            capacity: Number(hallModal.form.capacity),
        };

        if (!payload.name || payload.capacity <= 0) {
            alert("Please enter valid details");
            return;
        }

        if (hallModal.mode === 'edit' && hallModal.id) {
            // Find the original hall name, as the user might have changed it in the form.
            const originalHall = halls.find(h => h.id === hallModal.id);
            const originalName = originalHall ? originalHall.name : payload.name;

            await handleAPICall(
                () => hallAPI.updateHall(originalName, payload),
                () => {
                    loadAllHalls();
                    setHallModal({ open: false, mode: 'add', form: defaultHallForm, id: null });
                }
            );
        } else {
            await handleAPICall(
                () => hallAPI.createHall(payload),
                () => {
                    loadAllHalls();
                    setHallModal({ open: false, mode: 'add', form: defaultHallForm, id: null });
                }
            );
        }
    };

    const deleteHall = async () => {
        if (!deletePrompt.hall) return;
        await handleAPICall(
            () => hallAPI.deleteHall(deletePrompt.hall.name),
            (result) => {
                setHalls((prev) => prev.filter((hall) => hall.id !== deletePrompt.hall.id));
                setDeletePrompt({ open: false, hall: null });
            }
        );
    };

    const openRequestAction = (request, action) => {
        setRequestAction({ open: true, action, request, comment: '' });
    };

    const submitRequestAction = async (e) => {
        e.preventDefault();
        if (!requestAction.request) return;

        const nextStatus = requestAction.action === 'approve' ? 'Approved' : 'Rejected';

        if (requestAction.action === 'approve') {
            const [datePart, timePart] = requestAction.request.datetime.split(' ');
            const requestDate = new Date(datePart);
            const dayNames = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
            const dayOfWeek = dayNames[requestDate.getDay()];
            const startTime = timePart || '10:00';
            const [hours, minutes] = startTime.split(':');
            const endHours = (parseInt(hours) + 1).toString().padStart(2, '0');
            const endTime = `${endHours}:${minutes}`;

            const startDate = convertToBackendDate(dayOfWeek, startTime);
            const endDate = convertToBackendDate(dayOfWeek, endTime);

            const backendBookingData = {
                hallName: requestAction.request.hall,
                start: startDate,
                end: endDate,
                purpose: requestAction.request.course,
                reservationId: null,
                staffId: "1"
            };

            await handleAPICall(
                () => bookingAPI.createBooking(backendBookingData),
                (result) => {
                    loadAllBookings();

                    setRequests((prev) => prev.map((req) =>
                        req.id === requestAction.request.id
                            ? { ...req, status: 'Approved', comment: requestAction.comment }
                            : req
                    ));
                    setHalls((prev) => prev.map((hall) =>
                        hall.name === requestAction.request.hall ? { ...hall, status: 'Reserved' } : hall
                    ));

                    setRequestAction({ open: false, action: 'approve', request: null, comment: '' });
                }
            );
        } else {
            setRequests((prev) => prev.map((req) =>
                req.id === requestAction.request.id
                    ? { ...req, status: nextStatus, comment: requestAction.comment }
                    : req
            ));
            setRequestAction({ open: false, action: 'approve', request: null, comment: '' });
        }
    };

    const openBookingModal = (booking) => {
        if (booking) {
            const { id, hall, course, faculty, day, start, end } = booking;
            setBookingModal({ open: true, editing: id, form: { hall, course, faculty, day, start, end } });
        } else {
            const firstHallName = halls.length > 0 ? halls[0].name : '';
            setBookingModal({ open: true, editing: null, form: { ...defaultBooking, hall: firstHallName || defaultBooking.hall } });
        }
    };

    const submitBooking = async (e) => {
        e.preventDefault();
        const payload = bookingModal.form;

        const startM = toMinutes(payload.start);
        const endM = toMinutes(payload.end);
        if (endM <= startM) {
            alert('End time must be after the start time.');
            return;
        }

        // Exclude self from conflict check
        const conflictCheckId = bookingModal.editing || 'temp-new';
        const conflict = hasConflict({ ...payload, id: conflictCheckId }, schedule);

        if (conflict) {
            const proceed = window.confirm('Conflict detected. Save anyway?');
            if (!proceed) return;
        }

        const startDate = convertToBackendDate(payload.day, payload.start);
        const endDate = convertToBackendDate(payload.day, payload.end);

        if (!payload.hall) {
            alert("Please select a Hall.");
            return;
        }

        const backendData = {
            hallName: payload.hall,
            start: startDate,
            end: endDate,
            purpose: payload.course,
            staffId: "1"
        };

        if (bookingModal.editing) {
            // Look up the real DB ID from the schedule array
            const originalBooking = schedule.find(b => b.id === bookingModal.editing);

            if (!originalBooking || !originalBooking.reservationId) {
                alert("Error: Could not find original booking ID for update.");
                return;
            }

            const realDbId = originalBooking.reservationId;

            await handleAPICall(
                () => bookingAPI.updateBooking(realDbId, backendData),
                () => {
                    loadAllBookings();
                    setBookingModal({ open: false, editing: null, form: defaultBooking });
                }
            );
        } else {
            await handleAPICall(
                () => bookingAPI.createBooking({ ...backendData, reservationId: null }),
                () => {
                    loadAllBookings();
                    setBookingModal({ open: false, editing: null, form: defaultBooking });
                }
            );
        }
    };

    const deleteBooking = async () => {
        if (!bookingModal.editing) return;

        // Find the full booking object in state using the UI ID
        const bookingToDelete = schedule.find(b => b.id === bookingModal.editing);

        if (!bookingToDelete) {
            console.error("Could not find booking object in state");
            return;
        }

        // Grab the real database ID
        const rawId = bookingToDelete.reservationId;

        if (!rawId) {
            alert("Error: Cannot delete a booking that hasn't been saved to the DB yet.");
            return;
        }

        await handleAPICall(
            () => bookingAPI.deleteBooking(rawId),
            () => {
                loadAllBookings();
                setBookingModal({ open: false, editing: null, form: defaultBooking });
            }
        );
    };

    const getEventColor = (hallName) => {
        const hall = halls.find((h) => h.name === hallName);
        if (!hall) return '#6a7dff';
        return hallPaletteByBuilding[hall.building] || '#6a7dff';
    };

    const exportSchedule = (format) => {
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
                    </div>
                </div>
                <button className="ghost-btn" onClick={() => openHallModal()}>
                    + Add Hall
                </button>
            </header>

            {error && (
                <div className="error-banner" style={{
                    padding: '1rem',
                    margin: '1rem 0',
                    backgroundColor: '#fee',
                    border: '1px solid #fcc',
                    borderRadius: '8px',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center'
                }}>
                    <span style={{ color: '#c00' }}>{error}</span>
                    <button onClick={() => setError(null)} style={{
                        background: 'transparent',
                        border: 'none',
                        cursor: 'pointer',
                        fontSize: '1.2rem',
                        color: '#c00'
                    }}>×</button>
                </div>
            )}

            {loading && (
                <div className="loading-overlay" style={{
                    position: 'fixed',
                    top: 0,
                    left: 0,
                    right: 0,
                    bottom: 0,
                    backgroundColor: 'rgba(0, 0, 0, 0.5)',
                    display: 'flex',
                    justifyContent: 'center',
                    alignItems: 'center',
                    zIndex: 9999
                }}>
                    <div style={{
                        backgroundColor: 'white',
                        padding: '2rem',
                        borderRadius: '8px',
                        boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)'
                    }}>
                        Processing...
                    </div>
                </div>
            )}

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
                            placeholder="Search by hall name (Press Enter to search API)"
                            value={hallQuery}
                            onChange={(e) => setHallQuery(e.target.value)}
                            onKeyDown={handleSearch}
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
                        <span>Hall ID</span>
                        <span>Room Name</span>
                        <span>Capacity</span>
                        <span>Status</span>
                        <span>Actions</span>
                    </div>
                    {filteredHalls.map((hall) => (
                        <div className="table-row" key={hall.id}>
                            <span><strong>{hall.id}</strong></span>
                            <span>{hall.name}</span>
                            <span>{hall.capacity}</span>
                            <span>
                                <span className={`status-pill status-${hall.status.replace(/\s/g, '').toLowerCase()}`}>
                                    {hall.status}
                                </span>
                            </span>
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
                            <button onClick={exportScheduleAsPDF}>Export PDF</button>
                        </div>
                    </div>
                </header>

                <div className="schedule-legend">
                    <span><i className="dot available" />Available slot</span>
                    <span><i className="dot booked" />Booked</span>
                    <span><i className="dot conflict" />Conflict</span>
                </div>

                <div className="schedule-grid" role="grid" ref={scheduleRef}>
                <div className="schedule-grid-head" role="row">
                        <div className="time-column head" role="columnheader">Time</div>
                        {daysOfWeek.map((day) => (
                            <div className="day-column head" key={day} role="columnheader">{day}</div>
                        ))}
                    </div>

                    <div className="schedule-grid-body" style={{height: `${timelineHeight}px`, position: 'relative'}}>
                        <div className="time-column" aria-hidden="true">
                            {timelineSlots.slice(0, -1).map((slot) => (
                                <div className="time-slot" key={slot.time} style={{height: `${SLOT_PIXEL_HEIGHT}px`}}>
                                    {slot.label ? <span>{to12Hour(slot.label)}</span> : <span className="time-tick"/>}
                                </div>
                            ))}
                            <div className="time-slot time-slot-end">
                                <span>{to12Hour(timelineSlots[timelineSlots.length - 1]?.time)}</span>
                            </div>
                        </div>

                        {daysOfWeek.map((day) => (
                            <div className="day-column" key={day} role="gridcell" style={{position: 'relative'}}>
                                <div className="slot-stripes" aria-hidden="true">
                                    {timelineSlots.slice(0, -1).map((slot, index) => (
                                        <span
                                            className="slot-stripe"
                                            key={`${day}-stripe-${slot.time}-${index}`}
                                            style={{height: `${SLOT_PIXEL_HEIGHT}px`}}
                                        />
                                    ))}
                                </div>

                                {(eventsByDay[day] || []).map((event) => {
                                    const layout = event.layout;
                                    if (!layout) return null;

                                    const widthPercent = 100 / layout.columns;
                                    const leftPercent = widthPercent * layout.columnIndex;
                                    const spacing = layout.columns > 1 ? 4 : 0;
                                    const inset = 4;

                                    const computedLeft = `calc(${leftPercent}% + ${inset}px)`;
                                    const computedWidth = `calc(${widthPercent}% - ${inset * 2 + spacing}px)`;

                                    // Style events with conflicts differently
                                    const backgroundStyle = event.conflict
                                        ? `linear-gradient(135deg, #fee2e2 0%, #ef4444 100%)`
                                        : `linear-gradient(145deg, rgba(255,255,255,0.9), ${getEventColor(event.hall)})`;

                                    const borderStyle = event.conflict ? '2px solid #b91c1c' : '1px solid rgba(0,0,0,0.1)';
                                    const textColor = event.conflict ? '#7f1d1d' : '#1e293b';

                                    return (
                                        <button
                                            key={event.id}
                                            className="calendar-event"
                                            data-conflict={event.conflict}
                                            style={{
                                                position: 'absolute',
                                                top: `${layout.top}%`,
                                                height: `${layout.height}%`,
                                                left: computedLeft,
                                                width: computedWidth,
                                                background: backgroundStyle,
                                                border: borderStyle,
                                                color: textColor,
                                                zIndex: event.conflict ? 10 : 1
                                            }}
                                            title={`${event.course} (${event.start} - ${event.end})`}
                                            onClick={() => openBookingModal(event)}
                                        >
                                            <strong>{event.course}</strong>
                                            <span className="event-meta" style={{color: textColor}}>{event.hall}</span>
                                            <small style={{color: textColor}}>{event.start} - {event.end}</small>

                                            {event.conflict && (
                                                <div style={{
                                                    backgroundColor: '#991b1b', color: 'white',
                                                    fontSize: '9px', padding: '2px 4px',
                                                    borderRadius: '4px', marginTop: '2px', display: 'inline-block'
                                                }}>
                                                    CONFLICT
                                                </div>
                                            )}
                                        </button>
                                    );
                                })}
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
                            <input
                                value={hallModal.form.name}
                                onChange={(e) => setHallModal((prev) => ({ ...prev, form: { ...prev.form, name: e.target.value } }))}
                                required
                                /* Disable name changes on edit to protect the primary key */
                                disabled={hallModal.mode === 'edit'}
                            />
                        </label>
                        <label>
                            Code
                            <input value={hallModal.form.code} onChange={(e) => setHallModal((prev) => ({
                                ...prev,
                                form: {...prev.form, code: e.target.value}
                            }))} placeholder="Optional code"/>
                        </label>
                        <label>
                            Type
                            <select value={hallModal.form.type} onChange={(e) => setHallModal((prev) => ({
                                ...prev,
                                form: {...prev.form, type: e.target.value}
                            }))}>
                                {hallTypes.map((type) => (
                                    <option key={type} value={type}>{type}</option>
                                ))}
                            </select>
                        </label>
                        <label>
                            Capacity
                            <input type="number" value={hallModal.form.capacity}
                                   onChange={(e) => setHallModal((prev) => ({
                                       ...prev,
                                       form: {...prev.form, capacity: e.target.value}
                                   }))} required/>
                        </label>
                        <label>
                            Building / Location
                            <input value={hallModal.form.building} onChange={(e) => setHallModal((prev) => ({
                                ...prev,
                                form: {...prev.form, building: e.target.value}
                            }))} required/>
                        </label>
                        <label>
                            Resources
                            <textarea value={hallModal.form.resources} onChange={(e) => setHallModal((prev) => ({
                                ...prev,
                                form: {...prev.form, resources: e.target.value}
                            }))} placeholder="Optional"/>
                        </label>
                        <label>
                            Status
                            <select value={hallModal.form.status} onChange={(e) => setHallModal((prev) => ({
                                ...prev,
                                form: {...prev.form, status: e.target.value}
                            }))}>
                                {hallStatuses.map((status) => (
                                    <option key={status} value={status}>{status}</option>
                                ))}
                            </select>
                        </label>
                        <div className="modal-actions">
                            <button type="button" className="ghost" onClick={() => setHallModal({
                                open: false,
                                mode: 'add',
                                form: defaultHallForm,
                                id: null
                            })}>Cancel
                            </button>
                            <button type="submit" disabled={loading}>
                                {loading ? 'Saving...' : 'Save Hall'}
                            </button>
                        </div>
                    </form>
                </div>
            )}

            {deletePrompt.open && (
                <div className="modal" role="alertdialog" aria-modal="true">
                    <div className="modal-card">
                        <h3>Delete hall?</h3>
                        <p>"{deletePrompt.hall?.name}" will be removed from the directory. You can re-create it
                            later.</p>
                        <div className="modal-actions">
                            <button className="ghost" onClick={() => setDeletePrompt({open: false, hall: null})}
                                    disabled={loading}>Cancel
                            </button>
                            <button className="danger" onClick={deleteHall} disabled={loading}>
                                {loading ? 'Deleting...' : 'Delete'}
                            </button>
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
                            <textarea value={requestAction.comment}
                                      onChange={(e) => setRequestAction((prev) => ({...prev, comment: e.target.value}))}
                                      placeholder="Notes for the faculty"/>
                        </label>
                        <div className="modal-actions">
                            <button type="button" className="ghost" onClick={() => setRequestAction({
                                open: false,
                                action: 'approve',
                                request: null,
                                comment: ''
                            })} disabled={loading}>Cancel
                            </button>
                            <button type="submit" disabled={loading}>
                                {loading ? 'Processing...' : (requestAction.action === 'approve' ? 'Approve' : 'Reject')}
                            </button>
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
                                <button type="button" className="danger" onClick={deleteBooking} disabled={loading}>Remove</button>
                            )}
                            <button type="button" className="ghost" onClick={() => setBookingModal({ open: false, editing: null, form: defaultBooking })} disabled={loading}>Cancel</button>
                            <button type="submit" disabled={loading}>
                                {loading ? 'Saving...' : 'Save Booking'}
                            </button>
                        </div>
                    </form>
                </div>
            )}
        </div>
    );
};

export default Facilities;