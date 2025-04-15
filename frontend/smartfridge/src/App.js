import './layout/App.css';
import Header from "./layout/Header";
import FridgeMain from "./layout/FridgeMain";
import Footer from "./layout/Footer";
import React, {useEffect, useState} from "react";
import LoginPage from "./layout/LoginPage";
import SignupPage from "./layout/SignupPage";
import {ActivityLogs} from "./layout/ActivityLogs";
import AddItem from "./layout/AddItem";
import AccountSettings from "./layout/AccountSettings";
import defaultHeaders from "./layout/defaultHeaders";

function App() {

    //const token = localStorage.getItem("token");

    useEffect( () => {
        async function auth() {
            try {

                const response = await fetch('http://localhost:8080/auth/login', {headers: defaultHeaders()})
                if (!response.ok) {
                    setIsAuthenticated(false);
                    handleLoginToggle();
                    throw new Error(`HTTP error! Status: ${response.status}`);
                }
                const data = await response.json();
                setUserData(data);
                setIsAuthenticated(true);
                handleViewToggle();
            } catch (error) {
                console.error("Login failed:", error.message);
            }
        }
        auth();
    }, []);

    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [userData, setUserData] = useState({});

    const [loginToggle, setLoginToggle] = useState(true);
    const [registerToggle, setRegisterToggle] = useState(false);

    const [viewItemsToggle, setViewItemsToggle] = useState(false);

    const [addItemsToggle, setAddItemsToggle] = useState(false);

    const [activityLogsToggle, setActivityLogsToggle] = useState(false);

    const [settingsToggle, setSettingsToggle] = useState(false);

    const handleViewToggle = () => {
        setViewItemsToggle(true);
        setAddItemsToggle(false);
        setActivityLogsToggle(false);
        setLoginToggle(false);
        setRegisterToggle(false);
        setSettingsToggle(false);
    }

    const handleAddToggle = () => {
        setViewItemsToggle(false);
        setAddItemsToggle(true);
        setActivityLogsToggle(false);
        setLoginToggle(false);
        setRegisterToggle(false);
        setSettingsToggle(false);
    }

    const handleActivityLogsToggle = () => {
        setViewItemsToggle(false);
        setAddItemsToggle(false);
        setActivityLogsToggle(true);
        setLoginToggle(false);
        setRegisterToggle(false);
        setSettingsToggle(false);
    }

    const handleRegisterToggle = () => {
        setViewItemsToggle(false);
        setAddItemsToggle(false);
        setActivityLogsToggle(false);
        setLoginToggle(false);
        setRegisterToggle(true);
        setSettingsToggle(false);
    }

    const handleLoginToggle = () => {
        setViewItemsToggle(false);
        setAddItemsToggle(false);
        setActivityLogsToggle(false);
        setLoginToggle(true);
        setRegisterToggle(false);
        setSettingsToggle(false);
    }

    const handleSettingsToggle = () => {
        setViewItemsToggle(false);
        setAddItemsToggle(false);
        setActivityLogsToggle(false);
        setLoginToggle(false);
        setRegisterToggle(false);
        setSettingsToggle(true);
    }

    const handleLogout = () => {
        localStorage.removeItem("token");
        setIsAuthenticated(false);
        handleLoginToggle();
    }


  return (
    <div className="App">
      <Header />
        {isAuthenticated ? (
        <div className="navRow">
            <button
                className="navButton"
                onClick={handleViewToggle}
            > All items
            </button>
            <button
                className="navButton"
                onClick={handleAddToggle}
            > Add item
            </button>
            <button
                className="navButton"
                onClick={handleActivityLogsToggle}
            > Activity Logs
            </button>
            <button
                className="navButton"
                onClick={handleSettingsToggle}
            > Account Settings
            </button>
            <button
                className="navButton"
                onClick={handleLogout}
                >Logout
            </button>
        </div>) :
            (<div className="navRow">
                    <button
                    className="navButton"
                    onClick={handleLoginToggle}
                    >Login
                    </button>
                    <button
                    className="navButton"
                    onClick={handleRegisterToggle}
                    >Signup
                    </button>
            </div>)
        }
        {activityLogsToggle ? <ActivityLogs /> : null}
        {addItemsToggle ? <AddItem/> : null}
        {viewItemsToggle ? <FridgeMain
            fridgeId={userData.fridgeId}
        /> : null}
        {loginToggle ? <LoginPage
            setIsAuthenticated={setIsAuthenticated}
            handleViewToggle={handleViewToggle}/>
            : null}
        {registerToggle ? <SignupPage
                setIsAuthenticated={setIsAuthenticated}
                handleViewToggle={handleViewToggle}/>
            : null}
        {settingsToggle ? <AccountSettings
            userData={userData}
        /> : null}

      <Footer />
    </div>
  );
}

export default App;