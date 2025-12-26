import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './Announcements.css';
import umsLogo from '../../../assets/UMS Logo.png';
import { getAdmin, Icon } from '../Admin-Student-Api';
import { jwtDecode } from "jwt-decode";

const API_BASE_URL = 'http://localhost:8081/api/admin';

const Announcements = () => {
    const [admin, setAdmin] = useState("");
    const [announcements, setAnnouncements] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [showForm, setShowForm] = useState(false);
    const [formData, setFormData] = useState({ title: '', content: '' });
    const [submitting, setSubmitting] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        const token = localStorage.getItem("token");
        if (token) {
            const decoded = jwtDecode(token);
            const email = decoded.sub;

            async function fetchName() {
                const response = await getAdmin(email);
                setAdmin(response);
            }
            fetchName();
        }

        fetchAnnouncements();
    }, []);

    const fetchAnnouncements = async () => {
        try {
            const token = localStorage.getItem("token");
            const response = await fetch(`${API_BASE_URL}/announcements`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                }
            });

            if (!response.ok) {
                throw new Error('Failed to fetch announcements');
            }

            const data = await response.json();
            setAnnouncements(data);
            setLoading(false);
        } catch (err) {
            console.error('Error fetching announcements:', err);
            setError('Failed to load announcements');
            setLoading(false);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!formData.title.trim() || !formData.content.trim()) {
            setError('Both title and content are required');
            return;
        }

        setSubmitting(true);
        setError(null);

        try {
            const token = localStorage.getItem("token");
            const response = await fetch(`${API_BASE_URL}/announcements`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(formData)
            });

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText || 'Failed to create announcement');
            }

            // Reset form and refresh announcements
            setFormData({ title: '', content: '' });
            setShowForm(false);
            await fetchAnnouncements();
        } catch (err) {
            console.error('Error creating announcement:', err);
            setError(err.message);
        } finally {
            setSubmitting(false);
        }
    };

    const formatTimestamp = (timestamp) => {
        const date = new Date(timestamp);
        return date.toLocaleString('en-US', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    return (
        <div className="shell">
            <header className="topbar" role="banner">
                <div className="topbar-left">
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
                            <div className="user-name">{admin ? `Welcome, ${admin}` : "Welcome"}</div>
                        </div>
                    </div>
                </div>
            </header>

            <main className="main" role="main">
                <div className="page-header">
                    <div className="page-title">
                        <span className="page-title-ico">{Icon.megaphone}</span>
                        <div>
                            <h2>Announcements</h2>
                            <p className="page-sub">Create and manage university announcements</p>
                        </div>
                    </div>
                    <div className="header-actions">
                        <button onClick={() => setShowForm(!showForm)} className="btn-primary">
                            {showForm ? 'Cancel' : 'New Announcement'}
                        </button>
                    </div>
                </div>

                {error && (
                    <div className="error-banner">
                        <strong>Error:</strong> {error}
                        <button onClick={() => setError(null)} className="close-btn">
                            {Icon.close16}
                        </button>
                    </div>
                )}

                {showForm && (
                    <div className="announcement-form-pane">
                        <h3>Create New Announcement</h3>
                        <form onSubmit={handleSubmit}>
                            <div className="form-group">
                                <label htmlFor="title">Title</label>
                                <input
                                    type="text"
                                    id="title"
                                    value={formData.title}
                                    onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                                    placeholder="Enter announcement title"
                                    disabled={submitting}
                                    required
                                />
                            </div>
                            <div className="form-group">
                                <label htmlFor="content">Content</label>
                                <textarea
                                    id="content"
                                    value={formData.content}
                                    onChange={(e) => setFormData({ ...formData, content: e.target.value })}
                                    placeholder="Enter announcement content"
                                    rows="6"
                                    disabled={submitting}
                                    required
                                />
                            </div>
                            <div className="form-actions">
                                <button type="submit" className="btn-primary" disabled={submitting}>
                                    {submitting ? 'Posting...' : 'Post Announcement'}
                                </button>
                            </div>
                        </form>
                    </div>
                )}

                <div className="announcements-list">
                    {loading ? (
                        <div className="loading-state">Loading announcements...</div>
                    ) : announcements.length === 0 ? (
                        <div className="empty-state">
                            <p>No announcements yet. Create your first announcement!</p>
                        </div>
                    ) : (
                        announcements.map((announcement) => (
                            <div key={announcement.id} className="announcement-card">
                                <div className="announcement-header">
                                    <h3>{announcement.title}</h3>
                                    <span className="announcement-date">
                                        {formatTimestamp(announcement.timestamp)}
                                    </span>
                                </div>
                                <div className="announcement-content">
                                    {announcement.content}
                                </div>
                            </div>
                        ))
                    )}
                </div>
            </main>
        </div>
    );
};

export default Announcements;
