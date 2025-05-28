import React, {useCallback, useEffect, useState} from "react";
import './FridgeMain.css';
import {addActivityLog} from "./ActivityLogs";
import defaultHeaders from "./defaultHeaders";
import {useAppContext} from "../context/AppContext";

const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString(undefined, {
        year: 'numeric', month: 'short', day: 'numeric'
    });
};


function FridgeMain() {
    const [fridgeItems, setFridgeItems] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [fetchError, setFetchError] = useState(null);
    const [filter, setFilter] = useState("");
    const {userData} = useAppContext();
    const fetchFridgeItems = useCallback(async () => {
        if (userData && userData.fridgeId && userData.fridgeId !== 0) {
            setIsLoading(true);
            setFetchError(null);
            try {
                const response = await fetch('http://localhost:8080/api/items', {headers: defaultHeaders()});
                if (!response.ok) {
                    throw new Error(`HTTP error! Status: ${response.status}`);
                }
                const data = await response.json();
                setFridgeItems(data);
            } catch (err) {
                console.error("Failed to fetch fridge items:", err);
                setFetchError(err.message || "Failed to load items. Please try again.");
                setFridgeItems([]);
            } finally {
                setIsLoading(false);
            }
        } else {
            setFridgeItems([]);
            setIsLoading(false);
        }
    }, [userData]);

    useEffect(() => {
        fetchFridgeItems();
    }, [fetchFridgeItems]);


    const handleRemoveItem = async (id) => {
        const itemToRemove = fridgeItems.find(item => item.id === id);
        if (!itemToRemove) return;

        const originalItems = [...fridgeItems];
        setFridgeItems(currentItems => currentItems.filter(item => item.id !== id));

        if (itemToRemove.quantity >= 1) {
            addActivityLog(
                "REMOVED",
                itemToRemove.name,
                `Removed ${itemToRemove.quantity} ${itemToRemove.name} from fridge`
            );
        }
        try {
            const response = await fetch(`http://localhost:8080/api/items/${id}`, {
                method: 'DELETE',
                headers: defaultHeaders()
            });
            if (!response.ok) {
                throw new Error(`API error! Status: ${response.status}`);
            }
        } catch (err) {
            console.error("Failed to delete item:", err);
            setFridgeItems(originalItems);
            alert(`Failed to remove ${itemToRemove.name}. Please try again.`);
        }
    };

    const handleUpdateItem = async (itemId) => {
        const itemToUpdate = fridgeItems.find(item => item.id === itemId);
        const originalQuantity = itemToUpdate.quantity;
        itemToUpdate.quantity--;
        addActivityLog(
            "CONSUMED",
            itemToUpdate.name,
            `Consumed 1 ${itemToUpdate.name} (${originalQuantity} → ${itemToUpdate.quantity} remaining)`
        );
        if (itemToUpdate.quantity <= 0) {
            await handleRemoveItem(itemToUpdate.id);
        } else {
            const originalItems = [...fridgeItems];
            setFridgeItems(currentItems => currentItems.map(item => item.id === itemToUpdate.id ? itemToUpdate : item));
            try {
                const response = await fetch(`http://localhost:8080/api/items/${itemToUpdate.id}`, {
                    method: 'PUT',
                    headers: defaultHeaders(),
                    body: JSON.stringify(itemToUpdate)
                });
                if (!response.ok) {
                    throw new Error(`API error! Status: ${response.status}`);
                }
            } catch (err) {
                console.error("Failed to update item quantity:", err);
                setFridgeItems(originalItems);
                alert(`Failed to update ${itemToUpdate.name}. Please try again.`);
            }
        }

    }

    const getDaysUntilExpiry = (expiryDate) => {
        if (!expiryDate) return 0;
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        const expiry = new Date(expiryDate);
        expiry.setHours(0, 0, 0, 0);
        const diffTime = expiry - today;
        return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    };

    const getExpiryClass = (expiryDate) => {
        const days = getDaysUntilExpiry(expiryDate);
        if (days < 0) return "expired";
        if (days < 3) return "expires-soon";
        return "fresh";
    };

    const filteredItems = fridgeItems.filter(item =>
        item.name.toLowerCase().includes(filter.toLowerCase()) ||
        item.category.toLowerCase().includes(filter.toLowerCase())
    );

    if (!(userData && userData.fridgeId && userData.fridgeId !== 0)) {
        return (
            <div className="fridge-main-container fridge-message-container">
                <h2>My Fridge</h2>
                <p>You are not connected to any fridge. Please connect to a fridge in your account settings.</p>
            </div>
        );
    }

    if (isLoading) {
        return <div className="fridge-main-container"><p className="fridge-loading">Loading your fridge items...</p>
        </div>;
    }

    if (fetchError) {
        return <div className="fridge-main-container"><p className="fridge-error">Error: {fetchError}</p></div>;
    }

    return (
        <div className="fridge-main-container">
            <div className="fridge-header">
                <h2>My Fridge Items</h2>
                <input
                    type="text"
                    placeholder="Search by name or category..."
                    className="fridge-search-bar"
                    value={filter}
                    onChange={(e) => setFilter(e.target.value)}
                />
            </div>

            {filteredItems.length === 0 ? (
                <div className="fridge-message-container">
                    <p>{filter ? `No items match your search "${filter}".` : "Your fridge is empty. Time to go shopping!"}</p>
                </div>
            ) : (
                <div className="table-responsive-container">
                    <table className="fridge-items-table">
                        <thead>
                        <tr>
                            <th>Item Name</th>
                            <th>Quantity</th>
                            <th>Category</th>
                            <th>Unit Price</th>
                            <th>Total Price</th>
                            <th>Expires On</th>
                            <th>Purchased On</th>
                            <th>Actions</th>
                        </tr>
                        </thead>
                        <tbody>
                        {filteredItems.map(item => {
                            const daysUntilExpiry = getDaysUntilExpiry(item.expirationDate);
                            const expiryClass = getExpiryClass(item.expirationDate);
                            return (
                                <tr key={item.id} className={expiryClass}>
                                    <td data-label="Item Name">{item.name}</td>
                                    <td data-label="Quantity" className="quantity-cell">
                                        <span>{item.quantity}</span>
                                        <div className="quantity-controls">
                                            <button
                                                title="Consume one"
                                                className="action-button consume-button"
                                                onClick={() => handleUpdateItem(item.id)}
                                                disabled={item.quantity <= 0}
                                            >
                                                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20"
                                                     fill="currentColor" width="18" height="18">
                                                    <path fillRule="evenodd"
                                                          d="M4 10a.75.75 0 01.75-.75h10.5a.75.75 0 010 1.5H4.75A.75.75 0 014 10z"
                                                          clipRule="evenodd"/>
                                                </svg>
                                            </button>
                                        </div>
                                    </td>
                                    <td data-label="Category">
                                        <span className="category-badge">{item.category}</span>
                                    </td>
                                    <td data-label="Unit Price">{item.price ? `€${Number(item.price).toFixed(2)}` : 'N/A'}</td>
                                    <td data-label="Total Price">{item.price && item.quantity ? `€${(Number(item.price) * item.quantity).toFixed(2)}` : 'N/A'}</td>
                                    <td data-label="Expires On" className={`expiry-date-cell ${expiryClass}`}>
                                        {formatDate(item.expirationDate)}
                                        <span className="expiry-days">
                                            {daysUntilExpiry < 0 ? ` (Expired ${-daysUntilExpiry}d ago)` : daysUntilExpiry === 0 ? ' (Expires today)' : ` (${daysUntilExpiry} days left)`}
                                        </span>
                                    </td>
                                    <td data-label="Purchased On">{formatDate(item.buyingDate)}</td>
                                    <td data-label="Actions" className="actions-cell">
                                        <button
                                            className="action-button remove-button"
                                            onClick={() => handleRemoveItem(item.id)}
                                            title="Remove item"
                                        >
                                            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20"
                                                 fill="currentColor" width="18" height="18">
                                                <path fillRule="evenodd"
                                                      d="M8.75 1A2.75 2.75 0 006 3.75v.443c-.795.077-1.58.177-2.365.298a.75.75 0 10.23 1.482l.149-.022.841 10.518A2.75 2.75 0 007.596 19h4.807a2.75 2.75 0 002.742-2.53l.841-10.52.149.023a.75.75 0 00.23-1.482A41.03 41.03 0 0014 4.193V3.75A2.75 2.75 0 0011.25 1h-2.5zM10 4c.84 0 1.673.025 2.5.075V3.75c0-.69-.56-1.25-1.25-1.25h-2.5c-.69 0-1.25.56-1.25 1.25V4.075c.827-.05 1.66-.075 2.5-.075zM7.492 17.081a1.25 1.25 0 01-1.244-1.316l-.828-10.348A.75.75 0 016.178 5h7.644a.75.75 0 01.758.618l-.828 10.348a1.25 1.25 0 01-1.244 1.316H7.492z"
                                                      clipRule="evenodd"/>
                                            </svg>
                                            <span className="button-text">Remove</span>
                                        </button>
                                    </td>
                                </tr>
                            );
                        })}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
}

export default FridgeMain;