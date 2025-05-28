import React, {useState} from "react";
import './AddItem.css';
import {addActivityLog} from "./ActivityLogs";
import defaultHeaders from "./defaultHeaders";

const defaultItem = {
    name: "",
    description: "",
    quantity: 1,
    price: 0,
    category: "Other",
    expirationDate: new Date().toISOString().split('T')[0],
    buyingDate: new Date().toISOString().split('T')[0]
}

const ITEM_CATEGORIES = [
    "Produce", "Dairy", "Meat", "Bakery", "Pantry", "Frozen", "Beverages", "Snacks", "Leftovers", "Other"
];


function AddItem() {

    const [newItem, setNewItem] = useState(defaultItem);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [submitStatus, setSubmitStatus] = useState(null); // null, 'success', or 'error'
    const [errorMessage, setErrorMessage] = useState("");

    const handleChange = (e) => {
        const {name, value, type} = e.target;
        setNewItem(prev => ({
            ...prev,
            [name]: type === 'number' ? (value === "" ? "" : parseFloat(value)) : value
        }));
    };

    const validateForm = () => {
        if (!newItem.name.trim()) {
            setErrorMessage("Item name is required.");
            return false;
        }
        if (newItem.quantity <= 0) {
            setErrorMessage("Quantity must be greater than 0.");
            return false;
        }
        if (newItem.price < 0) {
            setErrorMessage("Price cannot be negative.");
            return false;
        }
        if (!newItem.expirationDate) {
            setErrorMessage("Expiration date is required.");
            return false;
        }
        if (!newItem.buyingDate) {
            setErrorMessage("Buying date is required.");
            return false;
        }
        setErrorMessage("");
        return true;
    };


    const handleAddItem = async (e) => {
        e.preventDefault();
        if (!validateForm()) {
            setSubmitStatus("error");
            return;
        }
        setIsSubmitting(true);
        setSubmitStatus(null);

        setNewItem(defaultItem);
        try {
            const response = await fetch('http://localhost:8080/api/items', {
                method: 'POST',
                headers: defaultHeaders(),
                body: JSON.stringify(newItem)
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({message: "Failed to add item. Server error."}));
                throw new Error(errorData.message || `HTTP error! Status: ${response.status}`);
            }

            addActivityLog(
                "ADDED",
                newItem.name,
                `Added ${newItem.quantity} ${newItem.name} (${newItem.category}) with expiry date ${newItem.expirationDate}`
            );
            setNewItem(defaultItem);
            setSubmitStatus("success");

        } catch (err) {
            console.error("Failed to add item:", err);
            setErrorMessage(err.message || "An unexpected error occurred.");
            setSubmitStatus("error");
        } finally {
            setIsSubmitting(false);
        }

    };

    return (
        <div className="add-item-container">
            <h2>Add New Item to Fridge</h2>
            <form onSubmit={handleAddItem} className="add-item-form">
                <div className="form-grid">
                    <div className="form-group span-2">
                        <label htmlFor="name">Item Name <span className="required-star">*</span></label>
                        <input
                            type="text"
                            id="name"
                            name="name"
                            placeholder="e.g., Organic Milk"
                            value={newItem.name}
                            onChange={handleChange}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="quantity">Quantity <span className="required-star">*</span></label>
                        <input
                            type="number"
                            id="quantity"
                            name="quantity"
                            min="1"
                            value={newItem.quantity}
                            onChange={handleChange}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="price">Price (Unit)</label>
                        <input
                            type="number"
                            id="price"
                            name="price"
                            min="0"
                            step="0.01"
                            placeholder="e.g., 2.99"
                            value={newItem.price}
                            onChange={handleChange}
                        />
                    </div>

                    <div className="form-group span-2">
                        <label htmlFor="category">Category</label>
                        <select
                            id="category"
                            name="category"
                            value={newItem.category}
                            onChange={handleChange}
                        >
                            {ITEM_CATEGORIES.map(cat => <option key={cat} value={cat}>{cat}</option>)}
                        </select>
                    </div>

                    <div className="form-group">
                        <label htmlFor="buyingDate">Buying Date <span className="required-star">*</span></label>
                        <input
                            type="date"
                            id="buyingDate"
                            name="buyingDate"
                            value={newItem.buyingDate}
                            onChange={handleChange}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="expirationDate">Expiration Date <span className="required-star">*</span></label>
                        <input
                            type="date"
                            id="expirationDate"
                            name="expirationDate"
                            value={newItem.expirationDate}
                            onChange={handleChange}
                            required
                        />
                    </div>

                    <div className="form-group span-2">
                        <label htmlFor="description">Description (Optional)</label>
                        <textarea
                            id="description"
                            name="description"
                            placeholder="e.g., 1 gallon, whole milk"
                            value={newItem.description}
                            onChange={handleChange}
                            rows="3"
                        ></textarea>
                    </div>
                </div>

                <div className="form-actions">
                    <button type="submit" className="submit-button" disabled={isSubmitting}>
                        {isSubmitting ? "Adding..." : "Add Item"}
                    </button>
                </div>

                {submitStatus === "success" && (
                    <div className="form-message success-message">
                        Item added successfully!
                    </div>
                )}
                {submitStatus === "error" && errorMessage && (
                    <div className="form-message error-message">
                        {errorMessage}
                    </div>
                )}
            </form>
        </div>
    );
}
export default AddItem;