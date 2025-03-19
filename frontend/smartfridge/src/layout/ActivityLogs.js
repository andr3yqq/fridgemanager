import React, {useEffect, useState} from "react";

const token = localStorage.getItem("token");

const defaultHeaders = {
    'Content-Type': 'application/json',
    ...(token && { 'Authorization': `Bearer ${token}` })
    //'Authorization': 'Bearer ' + token,
};

export function ActivityLogs() {

    const [activityLogs, setActivityLogs] = useState([]);
    const [filter, setFilter] = useState("");

    useEffect(() => {
        fetch('http://localhost:8080/api/logs', {headers: defaultHeaders})
            .then((result) => {
                return result.json();
            })
            .then((data) => {
                setActivityLogs(data);
            })
            .catch((err) => {
                console.log(err)
            });
    })

    const filteredLogs = activityLogs.filter(log =>
        log.itemName.toLowerCase().includes(filter.toLowerCase()) ||
        log.action.toLowerCase().includes(filter.toLowerCase())
    );

    const formatTimestamp = (timestamp) => {
        const date = new Date(timestamp);
        return `${date.toLocaleDateString()} ${date.toLocaleTimeString()}`;
    };

    return (
        <div className="viewAllItems">
            <div className="mb-4">
                <input
                    type="text"
                    placeholder="Search logs by item name or action..."
                    className="searchBar"
                    value={filter}
                    onChange={(e) => setFilter(e.target.value)}
                />
            </div>
            <div className="items">
                <h2 className="addItemHead">Activity Logs</h2>
                {filteredLogs.length === 0 ? (
                    <p className="noItems">No activity logs found.</p>
                ) : (
                    <table className="itemsTable">
                        <thead className="itemsTableHeadBase">
                        <tr>
                            <th className="itemsTableHead">Time</th>
                            <th className="itemsTableHead">Action</th>
                            <th className="itemsTableHead">Item</th>
                            <th className="itemsTableHead">Details</th>
                        </tr>
                        </thead>
                        <tbody className="itemsTableBody">
                        {filteredLogs.map((log, index) => (
                            <tr key={index}>
                                <td className="itemsTableData">{formatTimestamp(log.timestamp)}</td>
                                <td className="itemsTableData">
                                            <span className={`itemsCategory ${
                                                log.action === "ADDED" ? "bg-green-100 text-green-800" :
                                                    log.action === "REMOVED" ? "bg-red-100 text-red-800" :
                                                        "bg-yellow-100 text-yellow-800"
                                            }`}>
                                                {log.action}
                                            </span>
                                </td>
                                <td className="itemsTableData">{log.itemName}</td>
                                <td className="itemsTableData">{log.details}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                )}
            </div>
        </div>
    )
}

export function addActivityLog(action, itemName, details) {
    const newLog = {
        timestamp: new Date().toISOString(),
        action: action,
        itemName: itemName,
        details: details
    };

    // Add to state for immediate display
   // setActivityLogs([newLog, ...activityLogs]);

    // Send to backend
    const requestOptions = {
        method: 'POST',
        headers: defaultHeaders,
        body: JSON.stringify(newLog)
    };
    fetch('http://localhost:8080/api/logs', requestOptions)
        .then(response => response.json())
        .catch((err) => {
            console.log(err)
        });
};
