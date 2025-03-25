import React, {useEffect} from "react";
import './AccountSettings.css';
import defaultHeaders from "./defaultHeaders";

function AccountSettings(props) {

    const [userDetails, setUserDetails] = React.useState({
        password: 'aaaaaaaaaaaaaaaaa',
    });

    useEffect(() => {
        setUserDetails(props.userData);
        setIsFridgeConnected(!!userDetails.fridgeId);
        const requestOptions = {
            method: 'GET',
            headers: defaultHeaders()
        };
        fetch('http://localhost:8080/api/fridge', requestOptions)
            .then(async response => {
                const data = await response.json();
                console.log(data);
                setFridgeDetails(data);
                if (fridgeDetails.id !== 0)
                    setIsFridgeConnected(true);
            })
            .catch((err) => {
                console.log(err)
            })
    },[]);

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
                    >Save new password</button>
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
                    >Change password</button>
                </div>}
                {isFridgeConnected ? (
                    <div className="account-settings-content">
                        <label>Fridge Name</label>
                        <input
                            type="text"
                            disabled={true}
                            value={fridgeDetails.name}
                        />
                        { (fridgeDetails.ownerId === userDetails.id) ? (
                            <div className="account-settings-content">
                                <button
                                    onClick={handleSettingsToggle}
                                >Manage fridge</button>
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
                                        >Send invite</button>
                                    </div>
                                ) : null}
                            </div>
                        ) : (
                            <div className="account-settings-content">
                                <button
                                    onClick={() => {}}
                                >Leave fridge</button>
                            </div>
                        )
                        }
                    </div>
                ) : (
                    <div className="account-settings-content">
                        <label>Fridge</label>
                        <h3>Fridge is not connected</h3>
                        <button

                        >Create new fridge</button>
                    </div>
                )}

            </div>
        </div>
    )
}

export default AccountSettings;