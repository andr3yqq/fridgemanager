import React, { createContext, useContext, useState } from "react";

const AppContext = createContext();

export const AppProvider = ({ children }) => {
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [userData, setUserData] = useState({});
    const [view, setView] = useState("login");

    return (
        <AppContext.Provider value={{
            isAuthenticated,
            setIsAuthenticated,
            userData,
            setUserData,
            view,
            setView
        }}>
            {children}
        </AppContext.Provider>
    );
};

export const useAppContext = () => useContext(AppContext);
