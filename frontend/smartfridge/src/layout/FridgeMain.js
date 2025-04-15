import React, {useState} from "react";
import './FridgeMain.css';
import {addActivityLog} from "./ActivityLogs";
import defaultHeaders from "./defaultHeaders";

function FridgeMain(props) {
    const [fridgeItems, setFridgeItems] = useState([]);
    if (props.fridgeId !== 0) {
        fetch('http://localhost:8080/api/items', {headers: defaultHeaders()})
            .then((result) => {
                return result.json();
            })
            .then((data) => {
                setFridgeItems(data);
            })
            .catch((err) => {
                console.log(err)
            })

    }

    const [filter, setFilter] = useState("");

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
            method: 'DELETE',
            headers: defaultHeaders()
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
        } else {
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
                headers: defaultHeaders(),
                body: JSON.stringify(itemToUpdate)
            }
            fetch(`http://localhost:8080/api/items/${itemToUpdate.id}`, requestOptions)
                .then(response => response.json())
                .catch((err) => {
                    console.log(err)
                })
        }
    }

    // Calculate days until expiry
    const getDaysUntilExpiry = (expiryDate) => {
        const today = new Date();
        const expiry = new Date(expiryDate);
        const diffTime = expiry - today;
        return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    };

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

    return (
        <div className="mainFridge">
            <div className="viewAllItems">
                {props.fridgeId === 0 ? (
                    <p className="noItems">You are not connected to any fridge.</p>
                ) : (<>
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
                                                <button type="button" className="arrow-button"
                                                        onClick={() => handleUpdateItem(item)}>
                                                    <svg className="icon" aria-hidden="true"
                                                         xmlns="http://www.w3.org/2000/svg"
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
                                            <td className={`itemsTableData`}
                                                style={{color: getExpiryColor(item.expirationDate)}}>
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
                    </>
                )}

            </div>
        </div>
    );
}

export default FridgeMain;