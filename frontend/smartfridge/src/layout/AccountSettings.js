import React, {useEffect} from "react";
import './AccountSettings.css';
import defaultHeaders from "./defaultHeaders";

function AccountSettings(props) {

    const [userDetails, setUserDetails] = React.useState({
        username: props.username,
        email: props.email,
        password: 'aaaaaaaaaaaaaaaaa',
    });

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

    useEffect(() => {
        setUserDetails(props.userData);
        const requestOptions = {
            method: 'GET',
            headers: defaultHeaders()
        };
        getFridgeDetails();
        fetch('http://localhost:8080/api/invites', requestOptions)
            .then(async response => {
                const data = await response.json();
                setInvitesList(data);
            })
            .catch((err) => {
                console.log(err);
            })

    }, []);


    const [confirmPasswordInput, setConfirmPasswordInput] = React.useState(false);
    const [changePassword, setChangePassword] = React.useState({
        oldPassword: '',
        newPassword: '',
        confirmPassword: '',
    });
    const handleChangePassword = () => {
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
                })
                .catch((err) => {
                    console.log(err)
                })
        }
    }

    const [isFridgeConnected, setIsFridgeConnected] = React.useState(false);
    const [fridgeDetails, setFridgeDetails] = React.useState({});
    const [fridgeSettingsToggle, setFridgeSettingsToggle] = React.useState(false);
    const handleSettingsToggle = () => {
        setFridgeSettingsToggle(!fridgeSettingsToggle);
    }
    const [invitedPerson, setInvitedPerson] = React.useState("");
    const [inviteToggle, setInviteToggle] = React.useState(false);
    const handleInviteToggle = () => {
        setInviteToggle(!inviteToggle);
    }
    const [invitesList, setInvitesList] = React.useState([]);
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
                getFridgeDetails();
            })
            .catch((err) => {
                console.log(err);
            })
    }

    const [createToggle, setCreateToggle] = React.useState(false);
    const handleCreateToggle = () => {
        setCreateToggle(!createToggle);
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
                setFridgeDetails(data);
                setIsFridgeConnected(true);
            })
            .catch((err) => {
                console.log(err)
            })
    }

    const handleLeaveFridge = () => {
        const requestOptions = {
            method: 'POST',
            headers: defaultHeaders()
        }
        fetch('http://localhost:8080/api/fridge/leave', requestOptions)
            .then(async response => {
                const data = await response.json();
                console.log(data);
                setIsFridgeConnected(false);
            })
            .catch((err) => {
                console.log(err)
            })
    }

    return (
        <div className="account-settings">
            <h2 className="account-settings-header">
                Account Settings
            </h2>
            <div className="account-settings-content">
                <label>Username</label>
                <input
                    type="text"
                    disabled={true}
                    value={userDetails.username}
                />
                <label>Email</label>
                <input
                    type="email"
                    disabled={true}
                    value={userDetails.email}
                />
                {confirmPasswordInput ? <div className="account-settings-content">
                    <label>Old Password</label>
                    <input
                        type="password"
                        value={changePassword.oldPassword}
                        onChange={(e) => setChangePassword({...changePassword, oldPassword: e.target.value})}
                    />
                    <label>New Password</label>
                    <input
                        type="password"
                        value={changePassword.newPassword}
                        onChange={(e) => setChangePassword({...changePassword, newPassword: e.target.value})}
                    />
                    <label>Confirm Password</label>
                    <input
                        type="password"
                        value={changePassword.confirmPassword}
                        onChange={(e) => setChangePassword({...changePassword, confirmPassword: e.target.value})}
                    />
                    <button
                        onClick={() => {
                            setConfirmPasswordInput(false);
                            handleChangePassword();
                        }}
                    >Save new password
                    </button>
                </div> : <div className="account-settings-content">
                    <label>Password</label>
                    <input
                        type="password"
                        disabled={true}
                        value={userDetails.password}
                    />
                    <button
                        onClick={() => {
                            setConfirmPasswordInput(true);
                        }}
                    >Change password
                    </button>
                </div>}
                {isFridgeConnected ? (
                    <div className="account-settings-content">
                        <label>Fridge Name</label>
                        <input
                            type="text"
                            disabled={true}
                            value={fridgeDetails.name}
                        />
                        {(fridgeDetails.ownerId === userDetails.id) ? (
                            <div className="account-settings-content">
                                <button
                                    onClick={handleSettingsToggle}
                                >Manage fridge
                                </button>
                                {fridgeSettingsToggle ? (
                                    <div className="account-settings-content">
                                        <label>Invite user</label>
                                        <input
                                            type="text"
                                            placeholder="Username"
                                            value={invitedPerson}
                                            onChange={(e) => setInvitedPerson(e.target.value)}
                                        />
                                        <button
                                            onClick={handleInvite}
                                        >Send invite
                                        </button>
                                    </div>
                                ) : null}
                            </div>
                        ) : (
                            <div className="account-settings-content">
                                <button
                                    onClick={handleLeaveFridge}
                                >Leave fridge
                                </button>
                            </div>
                        )
                        }
                    </div>
                ) : (
                    <div className="account-settings-content">
                        <label>Fridge</label>
                        <h3>Fridge is not connected</h3>
                        <button
                            onClick={handleCreateToggle}
                        >Create new fridge
                        </button>
                        {createToggle ? (<>
                            <input
                                type="text"
                                value={fridgeDetails.name}
                                onChange={(e) => setFridgeDetails({...fridgeDetails, name: e.target.value})}
                            />
                            <button
                                onClick={handleCreateFridge}
                            >Submit new fridge
                            </button>
                        </>) : null}
                        <button
                            onClick={handleInviteToggle}
                        >Fridge invites
                        </button>
                        {inviteToggle ? (<>
                            <label>Invites list</label>
                            <ol>
                                {invitesList.map((item) => (<>
                                    <li key={item.id}>Fridge name: {item.fridgeName}
                                        <p>Invited by: {item.username}</p>
                                    </li>
                                        <button
                                            onClick={() => handleAcceptInvite(item.id)}
                                        >Accept invite</button>
                                    </>
                                ))}
                            </ol>
                        </>) : null}
                    </div>
                )}

            </div>
        </div>
    )
}

export default AccountSettings;