import React, {useEffect, useState} from "react";
import defaultHeaders from "./defaultHeaders";
import './ActivityLogs.css';

export function ActivityLogs() {

    const [activityLogs, setActivityLogs] = useState([]);
    const [filter, setFilter] = useState("");
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchLogs = async () => {
            setIsLoading(true);
            setError(null);
            try {
                const response = await fetch('http://localhost:8080/api/logs', {
                    method: 'GET',
                    headers: defaultHeaders()
                });
                if (!response.ok) {
                    const errorData = await response.json().catch(() => null);
                    throw new Error(errorData?.message || `HTTP error! Status: ${response.status}`);
                }
                const data = await response.json();
                data.sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));
                setActivityLogs(data);
            } catch (err) {
                console.error("Failed to fetch activity logs:", err);
                setError(err.message || "Could not fetch activity logs.");
            } finally {
                setIsLoading(false);
            }
        };
        fetchLogs();
    }, []);

    const filteredLogs = activityLogs.filter(log => {
        const filterLower = filter.toLowerCase();
        return (
            (log.itemName && log.itemName.toLowerCase().includes(filterLower)) ||
            (log.action && log.action.toLowerCase().includes(filterLower)) ||
            (log.username && log.username.toLowerCase().includes(filterLower)) ||
            (log.details && log.details.toLowerCase().includes(filterLower))
        );
    });

    const formatTimestamp = (timestamp) => {
        const date = new Date(timestamp);
        return date.toLocaleString(undefined, {
            year: 'numeric', month: 'short', day: 'numeric',
            hour: '2-digit', minute: '2-digit', second: '2-digit'
        });
    };

    if (isLoading) {
        return <div className="activity-logs-container loading">Loading activity logs...</div>;
    }

    if (error) {
        return <div className="activity-logs-container error-message">Error: {error}</div>;
    }

    return (
        <div className="activity-logs-container">
            <div className="activity-logs-header">
                <h2 className="activity-logs-title">Activity Logs</h2>
                <input
                    type="text"
                    placeholder="Search logs..."
                    className="activity-logs-searchBar"
                    value={filter}
                    onChange={(e) => setFilter(e.target.value)}
                />
            </div>

            {!activityLogs.length ? (
                <p className="activity-logs-noItems">No activity logs found.</p>
            ) : filteredLogs.length === 0 ? (
                <p className="activity-logs-noItems">No logs match your search criteria.</p>
            ) : (
                <div className="table-responsive-container">
                    <table className="activity-logs-table">
                        <thead>
                        <tr>
                            <th>Time</th>
                            <th>User</th>
                            <th>Action</th>
                            <th>Item/Entity</th>
                            <th>Details</th>
                        </tr>
                        </thead>
                        <tbody>
                        {filteredLogs.map((log) => (
                            <tr key={log.id}> {/* Assuming each log has a unique id */}
                                <td data-label="Time">{formatTimestamp(log.timestamp)}</td>
                                <td data-label="User">{log.username || 'N/A'}</td>
                                <td data-label="Action">
                                    <span className={`action-badge action-type-${log.action?.toLowerCase()}`}>
                                        {log.action}
                                    </span>
                                </td>
                                <td data-label="Item/Entity">{log.itemName || 'N/A'}</td>
                                <td data-label="Details">{log.details}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
}

export function addActivityLog(action, itemName, details) {
    const newLog = {
        timestamp: new Date().toISOString(),
        action: action,
        itemName: itemName,
        details: details
    };

    const requestOptions = {
        method: 'POST',
        headers: defaultHeaders(),
        body: JSON.stringify(newLog)
    };
    fetch('http://localhost:8080/api/logs', requestOptions)
        .then(response => response.json())
        .catch((err) => {
            console.log(err)
        });
}
