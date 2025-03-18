import './layout/App.css';
import Header from "./layout/Header";
import FridgeMain from "./layout/FridgeMain";
import Footer from "./layout/Footer";
import React from "react";
import LoginPage from "./layout/LoginPage";
import SignupPage from "./layout/SignupPage";

function App() {
    const token = localStorage.getItem("authToken");

    const requestOptions = {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    };
    fetch('http://localhost:8080/auth/login', requestOptions)
        .then(response => response.json())
        .then(data => console.log(data))
        .catch(error => console.error("Error:", error));


  return (
    <div className="App">
      <Header />
      <LoginPage />
      <FridgeMain />
      <Footer />
    </div>
  );
}

export default App;
