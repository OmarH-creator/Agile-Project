import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import './AdminDashboard.css';
import umsLogo from '../../assets/UMS Logo.png';
import { getAdmin, Icon } from './Admin-Student-Api'
import { jwtDecode } from "jwt-decode";

const AdminDashboard = () => {
    const [admin, setAdmin] = useState("");

    useEffect(() => {
        const token = localStorage.getItem("token");
        const decoded = jwtDecode(token);
        const email = decoded.sub;
        console.log("Admin data from token decoded:", decoded);

        async function fetchName() {
            const response = await getAdmin(email);
            setAdmin(response);
        }
        fetchName();
    }, []);

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
        // --- Professor BUTTON ADDED BELOW ---
        {
            title: 'Professors',
            sub: 'Manage faculty assignments and staff',
            icon: Icon.professor, /* Make sure to add 'professor' to your Icon export in Admin-Student-Api */
            accent: 'emerald',
            path: '/Admin/Professors'
        },
        {
            title: 'Announcements',
            sub: 'Create and manage university announcements',
            icon: Icon.megaphone,
            accent: 'orange',
            path: '/Admin/Announcements'
        }
    ];

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
                            <div className="user-name"> {admin ? `Welcome, ${admin}` : "Welcome"}</div>
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
                            <h2>Dashboard</h2>
                            <p className="page-sub">
                                Welcome to the University Management System
                            </p>
                        </div>
                    </div>
                </div>
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
            </main>
        </div>
    );
};

export default AdminDashboard;
