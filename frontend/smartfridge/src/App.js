import './layout/App.css';
import Header from "./layout/Header";
import FridgeMain from "./layout/FridgeMain";
import Footer from "./layout/Footer";
import React, {useState} from "react";
import LoginPage from "./layout/LoginPage";
import SignupPage from "./layout/SignupPage";
import {ActivityLogs} from "./layout/ActivityLogs";
import AddItem from "./layout/AddItem";





function App() {

    const token = localStorage.getItem("token");

    const [isAuthenticated, setIsAuthenticated] = useState(!!token);

    const [loginToggle, setLoginToggle] = useState(!token);
    const [registerToggle, setRegisterToggle] = useState(false);

    const [viewItemsToggle, setViewItemsToggle] = useState(!!token);

    const [addItemsToggle, setAddItemsToggle] = useState(false);

    const [activityLogsToggle, setActivityLogsToggle] = useState(false);

    //setIsAuthenticated(token);

    const handleViewToggle = () => {
        setViewItemsToggle(true);
        setAddItemsToggle(false);
        setActivityLogsToggle(false);
        setLoginToggle(false);
        setRegisterToggle(false);
    }

    const handleAddToggle = () => {
        setViewItemsToggle(false);
        setAddItemsToggle(true);
        setActivityLogsToggle(false);
        setLoginToggle(false);
        setRegisterToggle(false);
    }

    const handleActivityLogsToggle = () => {
        setViewItemsToggle(false);
        setAddItemsToggle(false);
        setActivityLogsToggle(true);
        setLoginToggle(false);
        setRegisterToggle(false);
    }

    const handleRegisterToggle = () => {
        setViewItemsToggle(false);
        setAddItemsToggle(false);
        setActivityLogsToggle(false);
        setLoginToggle(false);
        setRegisterToggle(true);
    }

    const handleLoginToggle = () => {
        setViewItemsToggle(false);
        setAddItemsToggle(false);
        setActivityLogsToggle(false);
        setLoginToggle(true);
        setRegisterToggle(false);
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
        {viewItemsToggle ? <FridgeMain /> : null}
        {loginToggle ? <LoginPage
            setIsAuthenticated={setIsAuthenticated}
            handleViewToggle={handleViewToggle}/>
            : null}
        {registerToggle ? <SignupPage
                setIsAuthenticated={setIsAuthenticated}
                handleViewToggle={handleViewToggle}/>
            : null}

      <Footer />
    </div>
  );
}

export default App;