import React, { useEffect } from 'react';
import './AdminDashboard.css';
import umsLogo from '../../assets/UMS Logo.png';

// Simple inline SVG icons for a clean, dependency-free UI
const Icon = {
  // People / Staff
  staff: (
    <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#7393F2" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/>
      <circle cx="9" cy="7" r="4"/>
      <path d="M22 21v-2a4 4 0 0 0-3-3.87"/>
      <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
    </svg>
  ),
  // Facilities / Building (classical pediment + columns)
  facilities: (
    <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#7393F2" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M3 10l9-6 9 6"/>    
      <path d="M4 10h16"/>
      <path d="M6 10v9"/>
      <path d="M10 10v9"/>
      <path d="M14 10v9"/>
      <path d="M18 10v9"/>
      <path d="M3 22h18"/>
    </svg>
  ),
  // Curriculum / Open Book
  curriculum: (
    <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#7393F2" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M12 5c-2-1.2-4-1.5-7-1v14c3-0.5 5-0.2 7 1"/>
      <path d="M12 5c2-1.2 4-1.5 7-1v14c-3-0.5-5-0.2-7 1"/>
      <path d="M12 5v14"/>
    </svg>
  ),
  // Community / Globe
  community: (
    <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#7393F2" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <circle cx="12" cy="12" r="10"/>
      <path d="M2 12h20"/>
      <path d="M12 2a15.3 15.3 0 0 1 0 20a15.3 15.3 0 0 1 0-20z"/>
    </svg>
  ),
  // Analytics / Chart
  analytics: (
    <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#7393F2" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M3 3v18h18"/>
      <rect x="7" y="12" width="3" height="6" rx="1"/>
      <rect x="12" y="9" width="3" height="9" rx="1"/>
      <rect x="17" y="6" width="3" height="12" rx="1"/>
    </svg>
  ),
  // Administration / Settings gear
  admin: (
    <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#7393F2" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <circle cx="12" cy="12" r="3"/>
      <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V22a2 2 0 1 1-4 0v-.09a1.65 1.65 0 0 0-1-1.51 1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H2a2 2 0 1 1 0-4h.09a1.65 1.65 0 0 0 1.51-1 1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33H8a1.65 1.65 0 0 0 1-1.51V2a2 2 0 1 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V8c0 .68.27 1.33.75 1.81.48.48 1.13.75 1.81.75H22a2 2 0 1 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"/>
    </svg>
  ),
  // Requests & Approvals / Document with check
  requests: (
    <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#7393F2" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <rect x="4" y="3" width="12" height="18" rx="2"/>
      <path d="M8 8h4M8 12h6"/>
      <path d="M14 3v4h4"/>
      <path d="M16 17l2 2 4-4"/>
    </svg>
  ),
  // Announcements & Events / Bell (clearer for notifications + events)
  announcements: (
    <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#7393F2" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M18 8a6 6 0 1 0-12 0c0 7-3 7-3 7h18s-3 0-3-7"/>
      <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
      <path d="M20 8.5c.7.6 1 1.5 1 2.5"/>
    </svg>
  ),
  // Events (calendar) — kept in case we want to swap for announcements later
  calendar: (
    <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#7393F2" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <rect x="3" y="4" width="18" height="18" rx="2"/>
      <path d="M16 2v4M8 2v4M3 10h18"/>
    </svg>
  ),
  // UI small icons for sidebar/topbar
  home16: (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d="M3 12l9-8 9 8"/><path d="M5 10v10h14V10"/></svg>
  ),
  help16: (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><circle cx="12" cy="12" r="10"/><path d="M9.09 9a3 3 0 1 1 5.82 1c0 2-3 2-3 4"/><path d="M12 17h.01"/></svg>
  ),
  bell16: (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d="M18 8a6 6 0 1 0-12 0c0 7-3 7-3 7h18s-3 0-3-7"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>
  ),
  msg16: (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d="M21 15a4 4 0 0 1-4 4H7l-4 4V6a4 4 0 0 1 4-4h10a4 4 0 0 1 4 4z"/></svg>
  ),
  user16: (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><circle cx="12" cy="7" r="4"/><path d="M6 21v-2a6 6 0 0 1 12 0v2"/></svg>
  )
};

const AdminDashboard = () => {
  useEffect(() => {
    // Update standard favicon
    const ensureLink = (rel, sizes) => {
      let l = document.querySelector(`link[rel='${rel}']${sizes ? `[sizes='${sizes}']` : ''}`);
      if (!l) {
        l = document.createElement('link');
        l.setAttribute('rel', rel);
        if (sizes) l.setAttribute('sizes', sizes);
        document.head.appendChild(l);
      }
      l.setAttribute('href', umsLogo);
      return l;
    };

    ensureLink('icon');
    ensureLink('icon', '32x32');
    ensureLink('icon', '16x16');
    // Apple touch icon (iOS home screen)
    ensureLink('apple-touch-icon');
  }, []);

  const tiles = [
    { title: 'Facilities', icon: Icon.facilities },
    { title: 'Curriculum', icon: Icon.curriculum },
    { title: 'Staff', icon: Icon.staff },
    { title: 'Community', icon: Icon.community },
    { title: 'Analytics / Reports', icon: Icon.analytics },
    { title: 'Administration (Settings & Roles)', icon: Icon.admin },
    { title: 'Requests & Approvals', icon: Icon.requests },
    { title: 'Announcements & Events', icon: Icon.announcements }
  ];

  return (
    <div className="shell">
      {/* Top navigation bar */}
      <header className="topbar" role="banner">
        <div className="topbar-left">
          <button className="icon-btn" aria-label="Menu" title="Menu">≡</button>
          <div className="brand-mini">
            <img src={umsLogo} alt="UMS logo" className="mini-logo" />
            <span className="brand-text brand-title">University Management — Admin</span>
          </div>
        </div>
        <div className="topbar-right">
          <button className="icon-btn" aria-label="Notifications" title="Notifications">{Icon.bell16}</button>
          <button className="icon-btn" aria-label="Messages" title="Messages"><span className="badge">0</span>{Icon.msg16}</button>
          <button className="icon-btn" aria-label="Account" title="Account">{Icon.user16}</button>
        </div>
      </header>

      {/* Sidebar */}
      <aside className="sidebar" aria-label="Sidebar navigation">
        <div className="sidebar-user">
          <div className="avatar" aria-hidden="true"><span className="avatar-ico">{Icon.user16}</span></div>
          <div className="user-meta">
            <div className="user-name">Admin User</div>
          </div>
        </div>
        <nav className="side-nav">
          <a className="nav-item active" href="#"><span className="nav-ico">{Icon.home16}</span><span>Dashboard</span></a>
          <a className="nav-item" href="#"><span className="nav-ico">{Icon.help16}</span><span>Help</span></a>
        </nav>
      </aside>

      {/* Main content */}
      <main className="main" role="main">
        <div className="page-header">
          <div className="page-title">
            <span className="page-title-ico">{Icon.home16}</span>
            <div>
              <h2>Dashboard</h2>
              <p className="page-sub">Welcome to the University Management System</p>
            </div>
          </div>
        </div>

        <div className="grid">
          {tiles.map((t) => (
            <div className="card" key={t.title}>
              <div className="card-title">
                <div>
                  <div className="card-heading">{t.title}</div>
                </div>
                {t.icon && <div className="card-icon" aria-hidden="true">{t.icon}</div>}
              </div>
            </div>
          ))}
        </div>
      </main>
    </div>
  );
};

export default AdminDashboard;
