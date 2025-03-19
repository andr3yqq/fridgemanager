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
    const token = localStorage.getItem("authToken");


    const [viewItemsToggle, setViewItemsToggle] = useState(true);

    const [addItemsToggle, setAddItemsToggle] = useState(false);

    const [activityLogsToggle, setActivityLogsToggle] = useState(false);

    const handleViewToggle = () => {
        setViewItemsToggle(true);
        setAddItemsToggle(false);
        setActivityLogsToggle(false);
    }

    const handleAddToggle = () => {
        setViewItemsToggle(false);
        setAddItemsToggle(true);
        setActivityLogsToggle(false);
    }

    const handleActivityLogsToggle = () => {
        setViewItemsToggle(false);
        setAddItemsToggle(false);
        setActivityLogsToggle(true);
    }


  return (
    <div className="App">
      <Header />
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
        </div>
        {activityLogsToggle ? <ActivityLogs /> : null}
        {addItemsToggle ? <AddItem/> : null}
      <LoginPage />
      <FridgeMain />
      <Footer />
    </div>
  );
}

export default App;
