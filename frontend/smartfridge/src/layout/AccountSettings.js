import React from "react";
import './AccountSettings.css';

function AccountSettings() {

    const [userDetails, setUserDetails] = React.useState({
        username: "test",
        email: "test@test.com",
        password: "test"
    });


    return (
        <div className="account-settings">
            <h2 className="account-settings-header">
                Account Settings
            </h2>
            <div className="account-settings-content">
                <label>Username</label>
                <input
                    type="text"
                    disabled="true"
                    value={userDetails.username}
                />
                <label>Email</label>
                <input
                    type="email"
                    disabled="true"
                    value={userDetails.email}
                />
                <label>Password</label>
                <input
                    type="password"
                    disabled="true"
                    value={userDetails.password}
                />
            </div>
        </div>
    )
}

export default AccountSettings;