import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "./Login.css";
import Illust from "../../assets/loginimage.png";
import umsLogo from "../../assets/UMS Logo.png";
import { login, checkStatus, SetPassword } from "../../auth/login";

function FloatingLabelInput({ id, type, label, value, setValue }) {
    return (
        <div className="floating-label-group">
            <input
                id={id}
                type={type}
                className="floating-input"
                autoComplete="off"
                value={value}
                onChange={e => setValue(e.target.value)}
                placeholder=" "
            />
            <label htmlFor={id} className="floating-label">
                {label}
            </label>
        </div>
    );
}

function Login() {
    // --- MAIN LOGIN STATE ---
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [isFirstTime, setIsFirstTime] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState("");

    // --- MODAL STATE ---
    const [showModal, setShowModal] = useState(false);
    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [modalError, setModalError] = useState("");

    const navigate = useNavigate();

    // 1. DYNAMIC SEARCH (Check status when user stops typing)
    useEffect(() => {
        const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

        // If empty or invalid, default to normal login view
        if (!email || !emailPattern.test(email)) {
            setIsFirstTime(false);
            return;
        }

        const delayDebounceFn = setTimeout(async () => {
            try {
                const data = await checkStatus(email);
                if (data.isFirstTime) {
                    setIsFirstTime(true);
                    setPassword(""); // Clear main password field
                    setError("");
                } else {
                    setIsFirstTime(false);
                }
            } catch (err) {
                setIsFirstTime(false);
            }
        }, 500);

        return () => clearTimeout(delayDebounceFn);
    }, [email]);

    // 2. NEW: RESET MODAL FIELDS WHENEVER IT OPENS
    useEffect(() => {
        if (showModal) {
            setNewPassword("");
            setConfirmPassword("");
            setModalError("");
        }
    }, [showModal]);

    // 3. HANDLE MAIN SUBMIT
    const handleSubmit = async (event) => {
        event.preventDefault();

        if (isFirstTime) {
            setShowModal(true);
        } else {
            await handleNormalLogin();
        }
    };

    const handleNormalLogin = async () => {
        setIsLoading(true);
        setError("");
        try {
            if (!email || !password) throw new Error("Please fill all fields!");

            const data = await login(email, password);

            localStorage.setItem("token", data.token);
            localStorage.setItem("role", data.role);
            if (data.userId) localStorage.setItem("userId", data.userId);

            if(data.role === "ADMIN") navigate("/admin");
            else if(data.role === "STUDENT") navigate("/student");
            else if(data.role === "PROFESSOR") navigate("/professor");
            else setError("User role not recognized.");

        } catch (err) {
            setError(err.message || "Login failed.");
        } finally {
            setIsLoading(false);
        }
    }

    // 4. HANDLE ASSIGN PASSWORD (SUCCESS)
    const handleAssignPassword = async () => {
        setModalError("");
        if (newPassword !== confirmPassword) {
            setModalError("Passwords do not match.");
            return;
        }

        try {
            await SetPassword(email, newPassword);

            alert("Password set successfully! Please login.");

            // --- SUCCESS RESET LOGIC ---
            setShowModal(false);     // Close modal
            setIsFirstTime(false);   // IMPORTANT: Flips UI back to "Normal Login" (shows password field)
            setPassword("");         // Ensure main password field is empty so they can type the new one

        } catch (err) {
            setModalError(err.message || "Failed to update password.");
        }
    };

    return (
        <div className="login-shell">
            <div className="login-card">
                <section className="login-illustration">
                    <div className="illustration-surface">
                        <img className="illustration-image" src={Illust} alt="Campus" />
                        <div className="illustration-text">
                            <h1 className="login-campus-title">Welcome to the University Portal</h1>
                            <p className="login-message">Empowering education through smart management.</p>
                        </div>
                    </div>
                </section>

                <section className="login-form-section">
                    <div className="login-brand">
                        <div className="login-logo-shell">
                            <img src={String(umsLogo)} alt="UMS logo" className="login-logo" />
                        </div>
                        <div className="login-brand-text">
                            <span className="login-brand-title">University Management System</span>
                            <span className="login-brand-sub">Secure Access</span>
                        </div>
                    </div>

                    <form className="login-form" onSubmit={handleSubmit}>
                        <h2 className="login-title">
                            {isFirstTime ? "Activate Account" : "Sign in to Your Account"}
                        </h2>

                        <FloatingLabelInput
                            id="email"
                            type="text"
                            label="Email"
                            value={email}
                            setValue={setEmail}
                        />

                        {/* DYNAMIC: Hide Password if First Time, Show if Normal */}
                        {!isFirstTime && (
                            <div className="fade-in">
                                <FloatingLabelInput
                                    id="password"
                                    type="password"
                                    label="Password"
                                    value={password}
                                    setValue={setPassword}
                                />
                            </div>
                        )}

                        {error && <div className="login-error-bar"><strong>{error}</strong></div>}

                        <button type="submit" className="login-button" disabled={isLoading}>
                            {isLoading ? <span className="loader"></span> : (isFirstTime ? "Assign Password" : "Login")}
                        </button>

                        {!isFirstTime && (
                            <div className="login-footer">
                                <label className="login-remember" htmlFor="remember">
                                    <input type="checkbox" id="remember" />
                                    <span>Remember me</span>
                                </label>
                            </div>
                        )}
                    </form>
                </section>
            </div>

            {/* --- MODAL --- */}
            {showModal && (
                <div className="modal-overlay">
                    <div className="modal-content">
                        <h3>Set New Password</h3>
                        <p style={{marginBottom: '15px', color: '#666'}}>
                            Create a password for <br/><strong>{email}</strong>
                        </p>

                        <div className="modal-input-group">
                            <input
                                type="password"
                                placeholder="New Password"
                                value={newPassword}
                                onChange={(e) => setNewPassword(e.target.value)}
                                style={{width: '100%', padding: '10px', marginBottom: '10px'}}
                            />
                            <input
                                type="password"
                                placeholder="Confirm Password"
                                value={confirmPassword}
                                onChange={(e) => setConfirmPassword(e.target.value)}
                                style={{width: '100%', padding: '10px', marginBottom: '10px'}}
                            />
                        </div>

                        {modalError && <div style={{color: 'red', marginBottom: '10px'}}>{modalError}</div>}

                        <div className="modal-actions">
                            <button
                                type="button"
                                onClick={() => setShowModal(false)}
                                className="cancel-btn"
                                style={{marginRight: '10px', padding: '8px 15px', cursor: 'pointer'}}
                            >
                                Cancel
                            </button>
                            <button
                                type="button"
                                onClick={handleAssignPassword}
                                className="submit-btn"
                                style={{padding: '8px 15px', background: '#007bff', color: 'white', border: 'none', cursor: 'pointer'}}
                            >
                                Save Password
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

export default Login;
