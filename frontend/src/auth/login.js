export async function login(email, password) {
  const response = await fetch("http://localhost:8080/api/auth/login", {
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