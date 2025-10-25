import React, { useState } from "react";
import "./Login.css";
import Illust from '../../assets/loginimage.png';
import confetti from 'canvas-confetti';
import { login } from "../../auth/login";
function FloatingLabelInput({ id, type, label, value, setValue }) {
  return (
    <div className="floating-label-group">
      <input
        id={id}
        type={type}
        className="floating-input"
        required
        autoComplete="off"
        value={value}
        onChange={e => setValue(e.target.value)}
        placeholder=" "  // required for :placeholder-shown to trigger
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

  const handleLogin = async (event) => {
  event.preventDefault();
  setIsLoading(true); // Start loading
  try {
    await new Promise(res => setTimeout(res, 2000));
    if (email && password) {
      const data = await login(email, password);
      localStorage.setItem("token", data.token);
      console.log(data.token);
      confetti({ particleCount: 200, spread: 150, origin: { y: 0.6 } });
    } else {
      alert("fill all fields!");
    }
  } catch (err) {
    alert(err.message);
  } finally {
    setIsLoading(false); // Stop loading (success or error)
  }
};

  return (
    <div className="login-split-container">
      <div className="login-illustration">
        <img
          className="illustration-image"
          src={Illust}
          alt="Campus illustration"
        />
        <div className="illustration-text">
          <h1 className="login-campus-title">Welcome to University Portal</h1>
          <p className="login-message">
            Learning starts here. Access resources, grades, and your campus community with one secure login.
          </p>
        </div>
      </div>
      <div className="login-form-section">
        <form className="login-form">
          <h2 className="login-title">Sign in to Your Account</h2>
          <FloatingLabelInput
            id="email"
            type="email"
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
        <button type="submit" onClick={handleLogin} className="login-button" disabled={isLoading}>
          {isLoading ? <span className="loader"></span> : "Login"}
        </button>

          <div className="login-footer">
            <span className="login-remember">
              <input type="checkbox" id="remember" />
              <label htmlFor="remember">Remember me</label>
            </span>
            <span className="login-forgot">
              <a href="#">Forgot password?</a>
            </span>
          </div>
        </form>
      </div>
    </div>
  );
}

export default Login;
