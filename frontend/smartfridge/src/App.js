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
import { useAppContext } from "./context/AppContext";

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
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            const data = await response.json();
            setUserData(data);
            console.log(data);
            setIsAuthenticated(true);
            setView("items");
        } catch (error) {
            console.error("Login failed:", error.message);
        }
    }, [setIsAuthenticated, setUserData, setView]);

    useEffect(() => {
        auth();
    }, [auth]);

    //const [view, setView] = useState("login"); // 'login', 'signup', 'items', 'add', 'logs', 'settings'
    const handleView = (newView) => setView(newView);

    const handleLogout = () => {
        localStorage.removeItem("token");
        setIsAuthenticated(false);
        setView("login")
    }

    const navButtons = [
        { label: "All items", view: "items" },
        { label: "Add item", view: "add" },
        { label: "Activity Logs", view: "logs" },
        { label: "Account Settings", view: "settings" },
    ];


  return (
    <div className="App">
      <Header />
        {isAuthenticated ? (
            <div className="navRow">
                {navButtons.map(btn => (
                    <button key={btn.view} className="navButton" onClick={() => handleView(btn.view)}>
                        {btn.label}
                    </button>
                ))}
                <button className="navButton" onClick={handleLogout}>Logout</button>
            </div>
        ) : (
            <div className="navRow">
                <button className="navButton" onClick={() => handleView("login")}>Login</button>
                <button className="navButton" onClick={() => handleView("signup")}>Signup</button>
            </div>
        )}
        {view === "logs" && <ActivityLogs />}
        {view === "add" && <AddItem />}
        {view === "items" && <FridgeMain />}
        {view === "login" && (
            <LoginPage handleViewToggle={() => handleView("items")} />
        )}
        {view === "signup" && (
            <SignupPage handleViewToggle={() => handleView("items")} />
        )}
        {view === "settings" && <AccountSettings />}

        <Footer />
    </div>
  );
}

export default App;