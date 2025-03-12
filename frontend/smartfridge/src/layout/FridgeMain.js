import Header from "./Header";
import Footer from "./Footer";
import React, {useEffect, useState} from "react";
import './FridgeMain.css';
import addItem from "./AddItem";

function FridgeMain() {
    const [fridgeItems, setFridgeItems] = useState([
        /*{ id: 1, name: "Milk", quantity: 1, category: "Dairy", expiryDate: "2025-03-10" },
        { id: 2, name: "Eggs", quantity: 12, category: "Dairy", expiryDate: "2025-03-15" },
        { id: 3, name: "Chicken", quantity: 2, category: "Meat", expiryDate: "2025-03-05" },
        { id: 4, name: "Spinach", quantity: 1, category: "Vegetables", expiryDate: "2025-03-07" },
        { id: 5, name: "Yogurt", quantity: 4, category: "Dairy", expiryDate: "2025-03-12" },
        { id: 6, name: "Apples", quantity: 6, category: "Fruits", expiryDate: "2025-03-20" }*/
    ]);
    const [activityLogs, setActivityLogs] = useState([]);

    useEffect(() => {
        fetch('http://localhost:8080/api/items')
            .then((result) => {
                return result.json();
            })
            .then((data) => {
                setFridgeItems(data);
                })
            .catch((err) => {
                console.log(err)
            })
        fetch('http://localhost:8080/api/logs')
            .then((result) => {
                return result.json();
            })
            .then((data) => {
                setActivityLogs(data);
            })
            .catch((err) => {
                console.log(err)
            });
    }, []);

    const [newItem, setNewItem] = useState({
        name: "",
        description: "",
        quantity: 1,
        category: "Other",
        expirationDate: new Date().toISOString().split('T')[0],
        buyingDate: new Date().toISOString().split('T')[0]
    });

    const [filter, setFilter] = useState("");

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

    const addActivityLog = (action, itemName, details) => {
        const newLog = {
            timestamp: new Date().toISOString(),
            action: action,
            itemName: itemName,
            details: details
        };

        // Add to state for immediate display
        setActivityLogs([newLog, ...activityLogs]);

        // Send to backend
        const requestOptions = {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(newLog)
        };
        fetch('http://localhost:8080/api/logs', requestOptions)
            .then(response => response.json())
            .catch((err) => {
                console.log(err)
            });
    };

    const handleAddItem = () => {
        if (newItem.name.trim() === "") return;
        setFridgeItems([
            ...fridgeItems,
            { ...newItem, id: fridgeItems.length + 1 }
        ]);
        addActivityLog(
            "ADDED",
            newItem.name,
            `Added ${newItem.quantity} ${newItem.name} (${newItem.category}) with expiry date ${newItem.expirationDate}`
        );
        setNewItem({
            name: "",
            description: "",
            quantity: 1,
            category: "Other",
            expirationDate: new Date().toISOString().split('T')[0],
            buyingDate: new Date().toISOString().split('T')[0]
        });
        AddItemToDB();
    };

    const AddItemToDB = () => {
        const requestOptions = {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(newItem)
        };
        fetch('http://localhost:8080/api/items', requestOptions)
            .then(response => response.json())
            .catch((err) => {
                console.log(err)
            })
    }

    const handleRemoveItem = (id) => {
        const itemToRemove = fridgeItems.find(item => item.id === id);

        // Log the activity
        if (itemToRemove) {
            addActivityLog(
                "REMOVED",
                itemToRemove.name,
                `Removed ${itemToRemove.quantity} ${itemToRemove.name} from fridge`
            );
        }
        setFridgeItems(fridgeItems.filter(item => item.id !== id));
        const requestOptions = {
            method: 'DELETE'
        };
        fetch(`http://localhost:8080/api/items/${id}`, requestOptions)
            .then(response => response.json())
            .catch((err) => {
                console.log(err)
            })
    };

    const handleUpdateItem = (itemToUpdate) => {
        const originalQuantity = itemToUpdate.quantity;
        itemToUpdate.quantity--;
        if (itemToUpdate.quantity <= 0) {
            handleRemoveItem(itemToUpdate.id);
        }
        else
        {
            addActivityLog(
                "CONSUMED",
                itemToUpdate.name,
                `Consumed 1 ${itemToUpdate.name} (${originalQuantity} → ${itemToUpdate.quantity} remaining)`
            );
            setFridgeItems(fridgeItems.map(item =>
                item.id === itemToUpdate.id ? itemToUpdate : item
            ));
            const requestOptions = {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(itemToUpdate)
            }
            fetch(`http://localhost:8080/api/items/${itemToUpdate.id}`, requestOptions)
                .then(response => response.json())
                .catch((err) => {
                    console.log(err)
                })
        }
    }

    const formatTimestamp = (timestamp) => {
        const date = new Date(timestamp);
        return `${date.toLocaleDateString()} ${date.toLocaleTimeString()}`;
    };

    // Calculate days until expiry
    const getDaysUntilExpiry = (expiryDate) => {
        const today = new Date();
        const expiry = new Date(expiryDate);
        const diffTime = expiry - today;
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        return diffDays;
    };

    // Get text color based on expiry date
    /*const getExpiryColor = (expiryDate) => {
        const days = getDaysUntilExpiry(expiryDate);
        if (days < 0) return "text-red-600";
        if (days < 3) return "text-orange-500";
        return "text-green-600";
    };*/

    const getExpiryColor = (expiryDate) => {
        const days = getDaysUntilExpiry(expiryDate);
        if (days < 0) return "#DC2626";
        if (days < 3) return "rgb(249 115 22)";
        return "#059669";
    };

    // Filter items based on search input
    const filteredItems = fridgeItems.filter(item =>
        item.name.toLowerCase().includes(filter.toLowerCase()) ||
        item.category.toLowerCase().includes(filter.toLowerCase())
    );

    const filteredLogs = activityLogs.filter(log =>
        log.itemName.toLowerCase().includes(filter.toLowerCase()) ||
        log.action.toLowerCase().includes(filter.toLowerCase())
    );

    return (
        <div className="mainFridge">

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

            {/* Add new item form */}
            {addItemsToggle ? <div className="addItem">
                <h2 className="addItemHead">Add New Item</h2>
                <div className="addItemElements">
                    <label>Item Name</label>
                    <input
                        type="text"
                        placeholder="Item name"
                        className="addItemInput20"
                        value={newItem.name}
                        name="itemName"
                        onChange={(e) => setNewItem({...newItem, name: e.target.value})}
                    />
                    <label>Description</label>
                    <input
                        type="text"
                        placeholder="Item description"
                        className="addItemBaseInput"
                        value={newItem.description}
                        onChange={(e) => setNewItem({...newItem, description: e.target.value})}
                    />
                    <label>Quantity</label>
                    <input
                        type="number"
                        min="1"
                        className="addItemCalendarInput"
                        value={newItem.quantity}
                        onChange={(e) => setNewItem({...newItem, quantity: parseInt(e.target.value)})}
                    />
                    <label>Category</label>
                    <select
                        className="addItemInput20"
                        value={newItem.category}
                        onChange={(e) => setNewItem({...newItem, category: e.target.value})}
                    >
                        <option value="Dairy">Dairy</option>
                        <option value="Meat">Meat</option>
                        <option value="Vegetables">Vegetables</option>
                        <option value="Fruits">Fruits</option>
                        <option value="Beverages">Beverages</option>
                        <option value="Leftovers">Leftovers</option>
                        <option value="Other">Other</option>
                    </select>
                    <label>Price</label>
                    <input
                        type="number"
                        className="addItemCalendarInput"
                        value={newItem.price}
                        onChange={(e) => setNewItem({...newItem, price: parseFloat(e.target.value)})}
                    />
                    <label>Expiration Date</label>
                    <input
                        type="date"
                        className="addItemCalendarInput"
                        value={newItem.expirationDate}
                        onChange={(e) => setNewItem({...newItem, expirationDate: e.target.value})}
                    />
                    <label>Buying Date</label>
                    <input
                        type="date"
                        className="addItemCalendarInput"
                        value={newItem.buyingDate}
                        onChange={(e) => setNewItem({...newItem, buyingDate: e.target.value})}
                    />
                    <button
                        className="addItemButton"
                        onClick={handleAddItem}
                    >
                        Add
                    </button>
                </div>
            </div> : null }

            { viewItemsToggle ?
                <div className="viewAllItems">
                    <div className="mb-4">
                    <input
                        type="text"
                        placeholder="Search items or categories..."
                        className="searchBar"
                        value={filter}
                        onChange={(e) => setFilter(e.target.value)}
                    />
                    </div>
                    <div className="items">
                    {filteredItems.length === 0 ? (
                        <p className="noItems">No items in your fridge.</p>
                    ) : (
                        <table className="itemsTable">
                            <thead className="itemsTableHeadBase">
                            <tr>
                                <th className="itemsTableHead">Item</th>
                                <th className="itemsTableHead">Quantity</th>
                                <th className="itemsTableHead">Category</th>
                                <th className="itemsTableHead">Price (1p)</th>
                                <th className="itemsTableHead">Total price</th>
                                <th className="itemsTableHead">Expires</th>
                                <th className="itemsTableHead">Buying date</th>
                                <th className="itemsTableHead">Action</th>
                            </tr>
                            </thead>
                            <tbody className="itemsTableBody">
                            {filteredItems.map(item => (
                                <tr key={item.id}>
                                    <td className="itemsTableData">{item.name}</td>
                                    <td className="itemsTableData">{item.quantity}
                                        <button type="button" className="arrow-button" onClick={() => handleUpdateItem(item)}>
                                            <svg className="icon" aria-hidden="true" xmlns="http://www.w3.org/2000/svg"
                                                 fill="none" viewBox="0 0 14 10">
                                                <path stroke="currentColor" strokeLinecap="round"
                                                      strokeLinejoin="round" strokeWidth="2"
                                                      d="M7 9V1m0 8l-4-4m4 4l4-4"/>
                                            </svg>
                                            <span className="sr-only">Icon description</span>
                                        </button>
                                    </td>
                                    <td className="itemsTableData">
                                        <span className="itemsCategory">{item.category}</span>
                                    </td>
                                    <td className="itemsTableData">{item.price}</td>
                                    <td className="itemsTableData">{item.price * item.quantity}</td>
                                    <td className={`itemsTableData`} style={{color: getExpiryColor(item.expirationDate)}}>
                                        {item.expirationDate}
                                        <span className="text-sm ml-2">
                      ({getDaysUntilExpiry(item.expirationDate)} days)
                    </span>
                                    </td>
                                    <td className="itemsTableData">{item.buyingDate}</td>
                                    <td className="p-4">
                                        <button
                                                className="itemsRemoveButton"
                                            onClick={() => handleRemoveItem(item.id)}
                                        >
                                            Remove
                                        </button>
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    )}
                    </div>
                </div> : null}
            {/* Activity Logs */}
            {activityLogsToggle ? (
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
            ) : null}
            {/* Search filter */}


            {/* Items list */}

        </div>
    );
}

export default FridgeMain;