import React, {useState} from "react";
import './Auth.css';
import {useAppContext} from "../context/AppContext";

function LoginPage() {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');

    const {setIsAuthenticated, setUserData, setView} = useAppContext();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');

        if (!username || !password) {
            setError("Username and password are required.");
            return;
        }

        const userData = {
            username: username,
            password: password
        }

        const requestOptions = {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(userData)
        };
        try {
            const response = await fetch('http://localhost:8080/auth/login', requestOptions)
            if (!response.ok) {
                if (response.status === 403) {
                    throw new Error("Please check your password and username and try again.");
                } else {
                    const errorData = await response.json().catch(() => ({message: `HTTP error! Status: ${response.status}`}));
                    throw new Error(errorData.message || `HTTP error! Status: ${response.status}`);
                }
            }
            const data = await response.json();
            localStorage.setItem('token', data.token);
            setIsAuthenticated(true);
            setUserData({
                id: data.id,
                username: data.username,
                email: data.email,
                role: data.role,
                fridgeId: data.fridgeId
            });
            setView("items");
        } catch (error) {
            console.error("Login failed:", error.message);
            setError(error.message || "Login failed. Please try again.");
        }
    }

    return (
        <div className="auth-page">
            <div className="auth-form-container">
                <h2 className="auth-title">Login</h2>
                <form onSubmit={handleSubmit} className="auth-form">
                    {error && <p className="auth-error-message">{error}</p>}
                    <div className="form-group">
                        <label htmlFor="login-username">Username</label>
                        <input
                            id="login-username"
                            type="text"
                            placeholder="Enter your username"
                            className="auth-input"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            required
                        />
                    </div>
                    <div className="form-group">
                        <label htmlFor="login-password">Password</label>
                        <input
                            id="login-password"
                            type="password"
                            placeholder="Enter your password"
                            className="auth-input"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                        />
                    </div>
                    <button type="submit" className="auth-button">
                        Login
                    </button>
                </form>
                <p className="auth-switch-link">
                    Don't have an account?{' '}
                    <span onClick={() => setView('signup')} className="switch-action">
                        Sign Up
                    </span>
                </p>
            </div>
        </div>
    );
}

export default LoginPage;