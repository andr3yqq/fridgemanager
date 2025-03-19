import fridgeMain from "./FridgeMain";
import React, {useState} from "react";
import {addActivityLog} from "./ActivityLogs";
import defaultHeaders from "./defaultHeaders";

function AddItem() {

    const [newItem, setNewItem] = useState({
        name: "",
        description: "",
        quantity: 1,
        category: "Other",
        expirationDate: new Date().toISOString().split('T')[0],
        buyingDate: new Date().toISOString().split('T')[0]
    });

    const handleAddItem = () => {
        if (newItem.name.trim() === "") return;
        /*setFridgeItems([
            ...fridgeItems,
            { ...newItem, id: fridgeItems.length + 1 }
        ]);*/
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



    return (
        <div className="addItem">
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
        </div>
    )
}
export default AddItem;