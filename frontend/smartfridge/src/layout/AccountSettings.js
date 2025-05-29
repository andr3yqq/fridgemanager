import React, {useEffect, useState} from "react";
import './AccountSettings.css';
import defaultHeaders from "./defaultHeaders";
import {useAppContext} from "../context/AppContext";
import {addActivityLog} from "./ActivityLogs";

function AccountSettings() {

    const { userData } = useAppContext();
    const [userDetails, setUserDetails] = useState({
        username: userData.username,
        email: userData.email,
        password: '***************',
        id: userData.id
    });
    const [isFridgeConnected, setIsFridgeConnected] = useState(false);
    const [fridgeDetails, setFridgeDetails] = useState({});
    const [invitesList, setInvitesList] = useState([]);

    const getFridgeDetails = () => {
        const requestOptions = {
            method: 'GET',
            headers: defaultHeaders()
        };
        fetch('http://localhost:8080/api/fridge', requestOptions)
            .then(async response => {
                const data = await response.json();
                console.log(data);
                setFridgeDetails(data);
                if (data.id !== 0)
                    setIsFridgeConnected(true);
            })
            .catch((err) => {
                console.log(err)
            })
    }

    const fetchInvites = () => {
        fetch('http://localhost:8080/api/invites', {method: 'GET', headers: defaultHeaders()})
            .then(async response => {
                if (!response.ok) throw new Error(`HTTP error! Status: ${response.status}`);
                return response.json();
            })
            .then(data => setInvitesList(data || []))
            .catch(err => {
                console.error("Error fetching invites:", err);
                setInvitesList([]);
            });
    };


    useEffect(() => {
        setUserDetails({
            username: userData.username,
            email: userData.email,
            password: '***************',
            id: userData.id
        });
        getFridgeDetails();
        fetchInvites();

    }, [userData]);


    const [confirmPasswordInput, setConfirmPasswordInput] = useState(false);
    const [changePassword, setChangePassword] = useState({
        oldPassword: '',
        newPassword: '',
        confirmPassword: '',
    });
    const handleChangePassword = () => {
        if (!changePassword.oldPassword || !changePassword.newPassword) {
            alert("All password fields are required.");
            return;
        }
        if (changePassword.newPassword === changePassword.confirmPassword) {
            const requestOptions = {
                method: 'POST',
                headers: defaultHeaders(),
                body: JSON.stringify({
                    oldPassword: changePassword.oldPassword,
                    newPassword: changePassword.newPassword
                })
            };
            fetch('http://localhost:8080/auth/update-password', requestOptions)
                .then(async response => {
                    const data = await response.json();
                    console.log(data);
                    setUserDetails(data);
                    localStorage.setItem('token', JSON.stringify(data.token));
                    alert("Password updated successfully!");
                    setConfirmPasswordInput(false);
                    setChangePassword({
                        oldPassword: '',
                        newPassword: '',
                        confirmPassword: '',
                    });
                })
                .catch((err) => {
                    console.log(err)
                })
        } else {
            alert("Passwords do not match!");
        }
    }

    const [fridgeSettingsToggle, setFridgeSettingsToggle] = useState(false);
    const [invitedPerson, setInvitedPerson] = useState("");
    const [inviteToggle, setInviteToggle] = useState(false);
    const [createToggle, setCreateToggle] = useState(false);
    const [newFridgeName, setNewFridgeName] = useState("");

    const handleInvite = () => {
        const requestOptions = {
            method: 'POST',
            headers: defaultHeaders(),
            body: invitedPerson
        };
        fetch('http://localhost:8080/api/invite', requestOptions)
            .then(async response => {
                const data = await response.json();
                console.log(data);
                alert(`Invite sent to ${invitedPerson}.`);
                addActivityLog(
                    "INVITED",
                    invitedPerson,
                    `${userDetails.username} invited ${invitedPerson} to fridge ${fridgeDetails.name}`
                );
            })
            .catch((err) => {
                console.log(err)
            })
    }

    const handleAcceptInvite = (id) => {
        const requestOptions = {
            method: 'POST',
            headers: defaultHeaders(),
            body: id
        };
        fetch('http://localhost:8080/api/fridge/join', requestOptions)
            .then(async response => {
                const data = await response.json();
                console.log(data);
                alert("Successfully joined fridge!");
                getFridgeDetails();
                fetchInvites();
                const invite = invitesList.find(inv => inv.id === id);
                if (invite) {
                    addActivityLog(
                        "JOINED",
                        userDetails.username,
                        `${userDetails.username} joined fridge ${invite.fridgeName}`
                    );
                }
                })
            .catch((err) => {
                console.log(err);
            })
    }

    const handleCreateFridge = () => {
        const requestOptions = {
            method: 'POST',
            headers: defaultHeaders(),
            body: fridgeDetails.name
        };
        fetch('http://localhost:8080/api/fridge', requestOptions)
            .then(async response => {
                const data = await response.json();
                console.log(data);
                alert(`Fridge "${data.name || newFridgeName}" created successfully!`);
                setFridgeDetails(data);
                setIsFridgeConnected(true);
                setCreateToggle(false);
                setNewFridgeName("");
                addActivityLog(
                    "CREATED",
                    userDetails.username,
                    `${userDetails.username} created fridge ${fridgeDetails.name}`
                );
            })
            .catch((err) => {
                console.log(err)
            })
    }

    const handleLeaveFridge = () => {
        if (!window.confirm("Are you sure you want to leave this fridge?")) return;
        const requestOptions = {
            method: 'POST',
            headers: defaultHeaders()
        }
        fetch('http://localhost:8080/api/fridge/leave', requestOptions)
            .then(async response => {
                const data = await response.json();
                console.log(data);
                alert("You have left the fridge.");
                setIsFridgeConnected(false);
                setFridgeDetails({});
                addActivityLog(
                    "LEFT",
                    userDetails.username,
                    `${userDetails.username} left fridge ${fridgeDetails.name}`
                );
            })
            .catch((err) => {
                console.log(err)
            })
    }

    const handleDeleteFridge = () => {
        if (!window.confirm("Are you sure you want to delete this fridge? This action cannot be undone.")) return;
        const requestOptions = {
            method: 'DELETE',
            headers: defaultHeaders()
        }
        fetch('http://localhost:8080/api/fridge', requestOptions)
            .then(async response => {
                const data = await response.json();
                console.log(data);
                alert("Fridge deleted successfully.");
                setFridgeDetails({});
                setIsFridgeConnected(false);
                setFridgeSettingsToggle(false);
                addActivityLog(
                    "DELETED",
                    userDetails.username,
                    `${userDetails.username} deleted fridge ${fridgeDetails.name}`
                );
            })
            .catch((err) => {
                console.log(err)
            })
    }

    if (!userData) {
        return <div className="account-settings-loading">Loading user data...</div>;
    }


    return (
        <div className="account-settings-container">
            <h1 className="main-header">Account Settings</h1>

            {/* User Profile Section */}
            <div className="settings-section">
                <h2 className="section-title">User Profile</h2>
                <div className="form-group">
                    <label htmlFor="username">Username</label>
                    <input type="text" id="username" disabled value={userDetails.username}/>
                </div>
                <div className="form-group">
                    <label htmlFor="email">Email</label>
                    <input type="email" id="email" disabled value={userDetails.email}/>
                </div>

                {!confirmPasswordInput ? (
                    <div className="form-group">
                        <label htmlFor="password-placeholder">Password</label>
                        <div className="password-display">
                            <input type="password" id="password-placeholder" disabled value={userDetails.password}/>
                            <button onClick={() => setConfirmPasswordInput(true)} className="button-inline">Change
                                Password
                            </button>
                        </div>
                    </div>
                ) : (
                    <div className="change-password-section">
                        <h3 className="subsection-title">Change Password</h3>
                        <div className="form-group">
                            <label htmlFor="oldPassword">Old Password</label>
                            <input type="password" id="oldPassword" value={changePassword.oldPassword}
                                   onChange={(e) => setChangePassword({
                                       ...changePassword,
                                       oldPassword: e.target.value
                                   })}/>
                        </div>
                        <div className="form-group">
                            <label htmlFor="newPassword">New Password</label>
                            <input type="password" id="newPassword" value={changePassword.newPassword}
                                   onChange={(e) => setChangePassword({
                                       ...changePassword,
                                       newPassword: e.target.value
                                   })}/>
                        </div>
                        <div className="form-group">
                            <label htmlFor="confirmPassword">Confirm New Password</label>
                            <input type="password" id="confirmPassword" value={changePassword.confirmPassword}
                                   onChange={(e) => setChangePassword({
                                       ...changePassword,
                                       confirmPassword: e.target.value
                                   })}/>
                        </div>
                        <div className="button-group">
                            <button onClick={handleChangePassword} className="button-primary">Save New Password</button>
                            <button onClick={() => setConfirmPasswordInput(false)} className="button-secondary">Cancel
                            </button>
                        </div>
                    </div>
                )}
            </div>

            {/* Fridge Section */}
            <div className="settings-section">
                <h2 className="section-title">Fridge Management</h2>
                {isFridgeConnected ? (
                    <div className="fridge-connected-section">
                        <div className="form-group">
                            <label>Fridge Name</label>
                            <input type="text" disabled value={fridgeDetails.name || 'N/A'}/>
                        </div>
                        <p className="fridge-owner-info">
                            {fridgeDetails.ownerId === userDetails.id ? "You are the owner of this fridge." : "You are a member of this fridge."}
                        </p>

                        {fridgeDetails.ownerId === userDetails.id && (
                            <div className="fridge-owner-actions">
                                <button onClick={() => setFridgeSettingsToggle(!fridgeSettingsToggle)}
                                        className="button-secondary">
                                    {fridgeSettingsToggle ? 'Hide Fridge Settings' : 'Manage Fridge'}
                                </button>
                                {fridgeSettingsToggle && (
                                    <div className="fridge-manage-options">
                                        <h3 className="subsection-title">Invite User</h3>
                                        <div className="form-group inline-form">
                                            <input
                                                type="text"
                                                placeholder="Username to invite"
                                                value={invitedPerson}
                                                onChange={(e) => setInvitedPerson(e.target.value)}
                                            />
                                            <button onClick={handleInvite} className="button-primary">Send Invite
                                            </button>
                                        </div>
                                        <button onClick={handleDeleteFridge} className="button-danger">Delete Fridge
                                        </button>
                                    </div>
                                )}
                            </div>
                        )}
                        {fridgeDetails.ownerId !== userDetails.id && (
                            <button onClick={handleLeaveFridge} className="button-danger">Leave Fridge</button>
                        )}
                    </div>
                ) : (
                    <div className="fridge-not-connected-section">
                        <p>You are not currently connected to a fridge.</p>
                        <div className="button-group">
                            <button onClick={() => setCreateToggle(!createToggle)} className="button-primary">
                                {createToggle ? 'Cancel Creation' : 'Create New Fridge'}
                            </button>
                            <button onClick={() => setInviteToggle(!inviteToggle)} className="button-secondary">
                                {inviteToggle ? 'Hide Invites' : 'View Pending Invites'} ({invitesList.length})
                            </button>
                        </div>

                        {createToggle && (
                            <div className="create-fridge-form">
                                <h3 className="subsection-title">Create a New Fridge</h3>
                                <div className="form-group">
                                    <label htmlFor="newFridgeName">Fridge Name</label>
                                    <input
                                        type="text"
                                        id="newFridgeName"
                                        placeholder="Enter new fridge name"
                                        value={newFridgeName}
                                        onChange={(e) => setNewFridgeName(e.target.value)}
                                    />
                                </div>
                                <button onClick={handleCreateFridge} className="button-primary">Submit New Fridge
                                </button>
                            </div>
                        )}

                        {inviteToggle && (
                            <div className="invites-list-section">
                                <h3 className="subsection-title">Pending Invites</h3>
                                {invitesList.length > 0 ? (
                                    <ul className="invites-list">
                                        {invitesList.map((invite) => (
                                            <li key={invite.id} className="invite-item">
                                                <span>Fridge: <strong>{invite.fridgeName}</strong> (Invited by: {invite.username})</span>
                                                <button onClick={() => handleAcceptInvite(invite.id)}
                                                        className="button-success">Accept Invite
                                                </button>
                                            </li>
                                        ))}
                                    </ul>
                                ) : (
                                    <p>No pending invites.</p>
                                )}
                            </div>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
}


export default AccountSettings;