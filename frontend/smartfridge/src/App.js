import './layout/App.css';
import Header from "./layout/Header";
import FridgeMain from "./layout/FridgeMain";
import Footer from "./layout/Footer";
import React, {useCallback, useEffect} from "react";
import LoginPage from "./layout/LoginPage";
import SignupPage from "./layout/SignupPage";
import {ActivityLogs} from "./layout/ActivityLogs";
import AddItem from "./layout/AddItem";
import AccountSettings from "./layout/AccountSettings";
import defaultHeaders from "./layout/defaultHeaders";
import {useAppContext} from "./context/AppContext";
import GroceryList from "./layout/GroceryList";

function App() {

    const {
        isAuthenticated,
        setIsAuthenticated,
        setUserData,
        view,
        setView
    } = useAppContext();

    const auth = useCallback(async () => {
        try {
            const response = await fetch('http://localhost:8080/auth/login', {headers: defaultHeaders()});
            if (!response.ok) {
                setIsAuthenticated(false);
                setView("login");
                console.error(`HTTP error! Status: ${response.status}`);
                return;
            }
            const data = await response.json();
            setUserData(data);
            console.log(data);
            setIsAuthenticated(true);
            setView("items");
        } catch (error) {
            setIsAuthenticated(false);
            setView("login");
            console.error("Login failed:", error.message);
        }
    }, [setIsAuthenticated, setUserData, setView]);

    useEffect(() => {
        auth();
    }, [auth]);

    const handleView = (newView) => setView(newView);

    const handleLogout = () => {
        localStorage.removeItem("token");
        setIsAuthenticated(false);
        setUserData(null);
        setView("login")
    }

    const navButtons = [
        { label: "All items", view: "items" },
        { label: "Add item", view: "add" },
        { label: "Activity Logs", view: "logs" },
        { label: "Account Settings", view: "settings" },
        {label: "Grocery Lists", view: "grocery"},
    ];


  return (
    <div className="App">
        <Header/>
        {isAuthenticated ? (
            <div className="app-layout-authenticated">
                <nav className="sidebar">
                    {/* <h3 className="sidebar-title">My App</h3> */}
                    {navButtons.map(btn => (
                        <button
                            key={btn.view}
                            className={`sidebar-nav-button ${view === btn.view ? 'active' : ''}`}
                            onClick={() => handleView(btn.view)}
                        >
                            {btn.label} {/* Icons could be added here too */}
                        </button>
                    ))}
                    <button className="sidebar-nav-button logout-button" onClick={handleLogout}>
                        Logout
                    </button>
                </nav>
                <main className="main-content">
                    {view === "logs" && <ActivityLogs/>}
                    {view === "add" && <AddItem/>}
                    {view === "items" && <FridgeMain/>}
                    {view === "settings" && <AccountSettings/>}
                    {view === "grocery" && <GroceryList/>}
                </main>
            </div>
        ) : (
            <div className="auth-container">
                {view === "login" && (
                    <LoginPage/>
                )}
                {view === "signup" && (
                    <SignupPage/>
                )}
            </div>
        )}
        <Footer />
    </div>

  );
}

export default App;