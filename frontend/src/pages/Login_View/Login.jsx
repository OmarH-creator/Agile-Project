import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import "./Login.css";
import Illust from "../../assets/loginimage.png";
import umsLogo from "../../assets/UMS Logo.png";
import confetti from "canvas-confetti";
import { login } from "../../auth/login";

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
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState("");
    const navigate = useNavigate();

    const handleLogin = async (event) => {
        event.preventDefault();
        setIsLoading(true);
        setError(""); // clear old errors
        try {
            await new Promise(res => setTimeout(res, 1000));
            const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

            if (!email || !password) {
                setError("Please fill all fields!");
                setIsLoading(false);
                return;
            }
            if (!emailPattern.test(email)) {
                setError("Please enter a valid email address.");
                setIsLoading(false);
                return;
            }

            const data = await login(email, password);
            if (!data.token) {
                setError("Invalid credentials. Please try again.");
                setIsLoading(false);
                return;
            }
            localStorage.setItem("token", data.token);
            confetti({ particleCount: 200, spread: 150, origin: { y: 0.6 } });
            if(data.role === "ADMIN"){
                navigate("/Admin");
            }
            else if(data.role === "STUDENT"){
                navigate("/student");
            }
            else if(data.role === "PROFESSOR"){
                navigate("/professor");
            }
        } catch (err) {
            setError(err.message || "Login failed. Please try again.");
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="login-shell">
            <div className="login-card">
                <section className="login-illustration">
                    <div className="illustration-surface">
                        <img
                            className="illustration-image"
                            src={Illust}
                            alt="Campus illustration"
                        />
                        <div className="illustration-text">
                            <h1 className="login-campus-title">Welcome to the University Portal</h1>
                            <p className="login-message">
                                Empowering education through smart management.
                            </p>
                        </div>
                    </div>
                </section>
                <section className="login-form-section">
                    <div className="login-brand">
                        <div className="login-logo-shell">
                            <img src={String(umsLogo)} alt="UMS logo" className="login-logo" />
                        </div>
                        <div className="login-brand-text">
                            <span className="login-brand-title">University Management System (UMS)</span>
                            <span className="login-brand-sub">Secure Admin Access</span>
                        </div>
                    </div>
                    <form className="login-form" onSubmit={handleLogin}>
                        <h2 className="login-title">Sign in to Your Account</h2>
                        <FloatingLabelInput
                            id="email"
                            type="text"
                            label="Email"
                            value={email}
                            setValue={setEmail}
                        />
                        <FloatingLabelInput
                            id="password"
                            type="password"
                            label="Password"
                            value={password}
                            setValue={setPassword}
                        />
                        {error && (
                            <div className="login-error-bar">
                                <strong>{error}</strong>
                            </div>
                        )}

                        <button type="submit" className="login-button" disabled={isLoading}>
                            {isLoading ? <span className="loader"></span> : "Login"}
                        </button>
                        <div className="login-footer">
                            <label className="login-remember" htmlFor="remember">
                                <input type="checkbox" id="remember" />
                                <span>Remember me</span>
                            </label>
                            <span className="login-forgot">
                                <button
                                    type="button"
                                    className="login-forgot-link"
                                    onClick={() => navigate('/forgot-password')}
                                >
                                    Forgot password?
                                </button>
                            </span>
                        </div>
                    </form>
                </section>
            </div>
        </div>
    );
}

export default Login;
