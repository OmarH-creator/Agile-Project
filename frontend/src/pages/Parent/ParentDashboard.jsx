import React, { useEffect, useState } from "react";
import { jwtDecode } from "jwt-decode";
import umsLogo from "../../assets/UMS Logo.png";
import { getChildrenByEmail } from "./ParentApi";
import "./ParentDashboard.css";

export default function ParentDashboard() {
    const [children, setChildren] = useState([]);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const token = localStorage.getItem("token");
                const decoded = jwtDecode(token);
                const email = decoded.sub || decoded.email;
                const data = await getChildrenByEmail(email);
                setChildren(data);
            } catch (err) {
                console.error("Error:", err);
            }
        };
        fetchData();
    }, []);

    return (
        <div className="shell">
            <header className="topbar">
                <img src={umsLogo} width={90} alt="UMS Logo"/>
                <h2>Parent Dashboard</h2>
            </header>

            <main>
                <h3>Your Children</h3>

                {children.length > 0 && (
                    <table className="children-table">
                        <thead>
                        <tr>
                            <th>ID</th>
                            <th>Name</th>
                            <th>Department</th>
                            <th>GPA</th>
                            <th>Email</th>
                        </tr>
                        </thead>
                        <tbody>
                        {children.map((child) => (
                            <tr key={child.studentId}>
                                <td>{child.studentId}</td>
                                <td>{child.name}</td>
                                <td>{child.major?.majorName || "Undeclared"}</td>
                                <td>{child.gpa?.toFixed(2)}</td>
                                <td>{child.email}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                )}
            </main>
        </div>
    );
}