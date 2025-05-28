import React, {useState} from "react";
import './Auth.css';
import {useAppContext} from "../context/AppContext";

function SignupPage() {

    const {setIsAuthenticated, setUserData, setView} = useAppContext();
    const [username, setUsername] = useState("");
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');

        if (!username || !email || !password) {
            setError("All fields are required.");
            return;
        }

        if (!/\S+@\S+\.\S+/.test(email)) {
            setError("Please enter a valid email address.");
            return;
        }

        const userData = {
            username: username,
            email: email,
            password: password
        }

        const requestOptions = {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(userData)
        };
        try {
            const response = await fetch('http://localhost:8080/auth/register', requestOptions)
            if (!response.ok) {
                const errorData = await response.json().catch(() => ({message: `HTTP error! Status: ${response.status}`}));
                throw new Error(errorData.message || `HTTP error! Status: ${response.status}`);
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
        }
        catch (error) {
            console.error("Registration failed:", error.message);
            setError(error.message || "Registration failed. Please try again.");
        }
    }

    return (
        <div className="auth-page">
            <div className="auth-form-container">
                <h2 className="auth-title">Create Account</h2>
                <form onSubmit={handleSubmit} className="auth-form">
                    {error && <p className="auth-error-message">{error}</p>}
                    <div className="form-group">
                        <label htmlFor="signup-username">Username</label>
                        <input
                            id="signup-username"
                            type="text"
                            placeholder="Choose a username"
                            className="auth-input"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            required
                        />
                    </div>
                    <div className="form-group">
                        <label htmlFor="signup-email">Email</label>
                        <input
                            id="signup-email"
                            type="email" // Changed to type="email" for better semantics and browser validation
                            placeholder="your.email@example.com"
                            className="auth-input"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            required
                        />
                    </div>
                    <div className="form-group">
                        <label htmlFor="signup-password">Password</label>
                        <input
                            id="signup-password"
                            type="password"
                            placeholder="Create a password"
                            className="auth-input"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                        />
                    </div>
                    <button type="submit" className="auth-button">
                        Sign Up
                    </button>
                </form>
                <p className="auth-switch-link">
                    Already have an account?{' '}
                    <span onClick={() => setView('login')} className="switch-action">
                        Login
                    </span>
                </p>
            </div>
        </div>
    );
}

export default SignupPage;