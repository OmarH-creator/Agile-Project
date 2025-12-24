import {jwtDecode} from 'jwt-decode';

const API_BASE_URL = "http://localhost:8081/api/auth";

export async function login(email, password) {
  const response = await fetch("http://localhost:8081/api/auth/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password })
  });

  // Throws an error if not successful
  if (!response.ok) {
    return response.text().then(text => {
      // Show the error message in your UI
      throw new Error(text);
    });
  }
  return await response.json(); // { token: ... }
}

export async function checkStatus(email) {
  const response = await fetch(`${API_BASE_URL}/check-status`, {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({email})
  });
  // Throws an error if not successful
  if (!response.ok) {
    return response.text().then(text => {
      // Show the error message in your UI
      throw new Error(text);
    });
  }
  return await response.json(); // { token: ... }
}

export async function SetPassword(email, newPassword) {
  const response = await fetch(`${API_BASE_URL}/set-initial-password`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, newPassword })
  });

  // Throws an error if not successful
  if (!response.ok) {
    return response.text().then(text => {
      // Show the error message in your UI
      throw new Error(text);
    });
  }
  return await response.text(); // { token: ... }
}



export function isAuthenticated() {
  const token = localStorage.getItem('token');
  if (!token) return false;

  try {
    const decoded = jwtDecode(token);
    // exp in JWT is in seconds, Date.now() gives ms
    if (decoded.exp && decoded.exp * 1000 < Date.now()) {
      // Token has expired
      localStorage.removeItem('token');
      return false;
    }
    return true;
  } catch (e) {
    // Invalid token
    localStorage.removeItem('token');
    return false;
  }
}