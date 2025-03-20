import React, { useState } from "react";
import './Auth.css';

function LoginPage(props) {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState('');
    const [loginResult, setLoginResult] = useState({})

    const handleSubmit = async () => {
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
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            const data = await response.json();
            console.log(data);
            localStorage.setItem('token', data.token);
            setLoginResult(data);
            props.setIsAuthenticated(true);
            props.handleViewToggle();
            //console.log(response.status());
        }

    catch (error) {
            console.error("Login failed:", error.message);
        }
    }

    return (
        <div className="LoginPage">
            <h2 className="LoginPageHead">Login</h2>
            <div className="LoginPageBody">
                <label>Username</label>
                <input
                    type="text"
                    placeholder="username"
                    className="addItemInput20"
                    value={username}
                    name="username"
                    onChange={(e) => setUsername(e.target.value)}
                />
                <label>Password</label>
                <input
                    type="password"
                    placeholder="password"
                    className="addItemInput20"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                />
                <button
                    className="LoginButton"
                    onClick={handleSubmit}
                >
                    Login
                </button>
            </div>
        </div>
    )

}

export default LoginPage;