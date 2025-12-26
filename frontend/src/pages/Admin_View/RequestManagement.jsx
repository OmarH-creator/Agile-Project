import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Icon } from './Admin-Student-Api'; // Reuse icon or import separately
import './AdminDashboard.css'; // Reuse styles

const RequestManagement = () => {
    const [requests, setRequests] = useState([]);
    const [loading, setLoading] = useState(true);

    const API_BASE_URL = 'http://localhost:8081/api/admin/requests';

    useEffect(() => {
        fetchRequests();
    }, []);

    const fetchRequests = async () => {
        try {
            const response = await axios.get(API_BASE_URL);
            setRequests(response.data);
            setLoading(false);
        } catch (error) {
            console.error("Error fetching requests:", error);
            setLoading(false);
        }
    };

    const handleAction = async (id, action) => {
        try {
            await axios.put(`${API_BASE_URL}/${id}/${action}`); // action = 'approve' or 'reject'
            alert(`Request ${action}d successfully`);
            fetchRequests(); // Refresh list
        } catch (error) {
            console.error(`Error ${action}ing request:`, error);
            alert(`Failed to ${action} request`);
        }
    };

    const getStatusBadge = (status) => {
        const s = status || 'Pending';
        let className = 'status-badge';
        if (s === 'Approved') className += ' approved';
        if (s === 'Rejected') className += ' rejected';
        if (s === 'Pending') className += ' pending';
        return <span className={className}>{s}</span>;
    };

    return (
        <div className="shell">
            <div className="page-header">
                <div className="page-title">
                    <span className="page-title-ico">{Icon.requests}</span>
                    <div>
                        <h2>Requests & Approvals</h2>
                        <p className="page-sub">Manage staff requests.</p>
                    </div>
                </div>
            </div>

            <main className="content-body">
                {loading ? (
                    <div>Loading requests...</div>
                ) : requests.length === 0 ? (
                    <div className="empty-state">No requests found.</div>
                ) : (
                    <table className="prof-table card">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Requester</th>
                                <th>Type</th>
                                <th>Description</th>
                                <th>Status</th>
                                <th style={{ textAlign: 'right' }}>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {requests.map((req) => (
                                <tr key={req.id}>
                                    <td>{req.id}</td>
                                    <td>{req.requester ? req.requester.email : 'Unknown'}</td>
                                    <td>{req.requestType}</td>
                                    <td>
                                        {/* Simplify Description display from EAV if complex, or just show text */}
                                        {req.values && req.values.find(v => v.attribute.attributeName === 'Description')?.valString || '-'}
                                    </td>
                                    <td>{getStatusBadge(req.status)}</td>
                                    <td style={{ textAlign: 'right', gap: '8px', display: 'flex', justifyContent: 'flex-end' }}>
                                        {req.status === 'Pending' && (
                                            <>
                                                <button
                                                    className="primary-btn small"
                                                    style={{ backgroundColor: '#10b981' }}
                                                    onClick={() => handleAction(req.id, 'approve')}
                                                >
                                                    Approve
                                                </button>
                                                <button
                                                    className="primary-btn small"
                                                    style={{ backgroundColor: '#ef4444' }}
                                                    onClick={() => handleAction(req.id, 'reject')}
                                                >
                                                    Reject
                                                </button>
                                            </>
                                        )}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}
            </main>
        </div>
    );
};

export default RequestManagement;
