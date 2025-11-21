import {jwtDecode} from 'jwt-decode';

export async function login(email, password) {
  const response = await fetch("http://localhost:8081/api/auth/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password })
  });

  // Throws an error if not successful
  if (!response.ok) {
    throw new Error("Invalid credentials");
  }
  return await response.json(); // { token: ... }
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