import React, {useCallback, useEffect, useState} from 'react';
import {useAppContext} from "../context/AppContext";
import defaultHeaders from "./defaultHeaders";
import {addActivityLog} from "./ActivityLogs";
import './GroceryList.css';

const API_BASE_URL = 'http://localhost:8080/api/grocery';

function GroceryLists() {
    const [lists, setLists] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [successMessage, setSuccessMessage] = useState('');
    const [newListName, setNewListName] = useState('');
    const [newListDescription, setNewListDescription] = useState('');
    const {userData} = useAppContext();

    const initialNewItemState = {
        name: '',
        description: '',
        quantity: 1,
        category: '',
        purchased: false,
    };

    const [addingToListId, setAddingToListId] = useState(null);
    const [newItem, setNewItem] = useState(initialNewItemState);

    const [viewingListId, setViewingListId] = useState(null);
    const [listItems, setListItems] = useState([]);
    const [itemsLoading, setItemsLoading] = useState(false);
    const [itemsError, setItemsError] = useState(null);

    const [isPriceExpiryModalOpen, setIsPriceExpiryModalOpen] = useState(false);
    const [itemsForModal, setItemsForModal] = useState([]);
    const [currentListIdForModal, setCurrentListIdForModal] = useState(null);

    const allItemsPurchasedInView = viewingListId !== null && listItems.length > 0 && listItems.every(item => item.purchased);

    const fetchLists = useCallback(async () => {
        if (userData.fridgeId !== 0) {
            setLoading(true);
            setError(null);
            try {
                const response = await fetch(API_BASE_URL, {headers: defaultHeaders()});
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                const data = await response.json();
                setLists(data);
            } catch (e) {
                console.error("Failed to fetch grocery lists:", e);
                setError('Failed to load lists. Please try refreshing.');
            } finally {
                setLoading(false);
            }
        } else {
            setLoading(false);
            setError("Fridge not connected. Cannot load grocery lists.");
            setLists([]);
        }
    }, [userData.fridgeId]);

    useEffect(() => {
        fetchLists();
    }, [fetchLists]);

    const handleAddList = async (event) => {
        event.preventDefault();
        if (!newListName.trim()) {
            alert('List name cannot be empty.');
            return;
        }
        setError(null);
        setSuccessMessage('');
        try {
            const response = await fetch(`${API_BASE_URL}/${encodeURIComponent(newListName)}`, {
                method: 'POST',
                headers: defaultHeaders(),
                body: JSON.stringify(newListDescription),
            });

            if (!response.ok) {
                const errorData = await response.text();
                throw new Error(`Failed to create list: ${response.status} ${errorData}`);
            }
            const createdList = await response.json();
            setLists(prevLists => [...prevLists, createdList]);
            setNewListName('');
            setNewListDescription('');
            setSuccessMessage(`List "${createdList.name}" added successfully!`);
            addActivityLog("CREATED", createdList.name, `${userData.username} created grocery list "${createdList.name}"`);

        } catch (e) {
            console.error("Error adding list:", e);
            setError(`Failed to add list: ${e.message}`);
        }
    };

    const handleDeleteList = async (listId, listName) => {
        if (!window.confirm('Are you sure you want to delete this list? This cannot be undone.')) {
            return;
        }
        setError(null);
        setSuccessMessage('');
        try {
            const response = await fetch(`${API_BASE_URL}/${listId}`, {
                method: 'DELETE',
                headers: defaultHeaders(),
            });
            if (!response.ok) {
                throw new Error(`Failed to delete list: ${response.status}`);
            }
            setLists(prevLists => prevLists.filter(list => list.id !== listId));
            if (viewingListId === listId) {
                setViewingListId(null);
                setListItems([]);
            }
            setSuccessMessage('List deleted successfully.');
            addActivityLog("DELETED", listName, `${userData.username} deleted grocery list "${listName}"`);
        } catch (e) {
            console.error("Error deleting list:", e);
            setError(`Failed to delete list: ${e.message}`);
        }
    };

    const handleToggleAddItemForm = (listId) => {
        setSuccessMessage('');
        setError(null);
        if (addingToListId === listId) {
            setAddingToListId(null);
        } else {
            setAddingToListId(listId);
            setNewItem(initialNewItemState);
            setViewingListId(null);
            setListItems([]);
        }
    };

    const handleNewItemChange = (event) => {
        const {name, value} = event.target;
        setNewItem(prevItem => ({
            ...prevItem,
            [name]: value,
        }));
    };

    const handleAddItemSubmit = async (event, listId) => {
        event.preventDefault();
        if (!newItem.name.trim()) {
            alert('Item name cannot be empty.');
            return;
        }
        if (newItem.quantity <= 0) {
            alert('Item quantity must be greater than 0.');
            return;
        }
        setError(null);
        setSuccessMessage('');
        const newItemDto = {
            name: newItem.name,
            description: newItem.description || null,
            quantity: newItem.quantity || 1,
            category: newItem.category || null,
            purchased: newItem.purchased || false,
        };

        try {
            const response = await fetch(`${API_BASE_URL}/add/${listId}`, {
                method: 'POST',
                headers: defaultHeaders(),
                body: JSON.stringify(newItemDto),
            });

            if (!response.ok) {
                const errorData = await response.text();
                throw new Error(`Failed to add item: ${response.status} ${errorData}`);
            }

            const addedItem = await response.json();
            setSuccessMessage(`Item "${addedItem.name}" added successfully!`);
            setNewItem(initialNewItemState);
            setAddingToListId(null);
            if (viewingListId === listId) {
                fetchListItems(listId);
            }
            addActivityLog("ADDED_ITEM", addedItem.name, `${userData.username} added "${addedItem.name}" to a grocery list.`);
        } catch (e) {
            console.error("Error adding item:", e);
            setError(`Failed to add item: ${e.message}`);
        }
    };

    const fetchListItems = async (listId) => {
        setItemsLoading(true);
        setItemsError(null);
        try {
            const response = await fetch(`${API_BASE_URL}/${listId}/items`, {headers: defaultHeaders()});
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const data = await response.json();
            setListItems(data);
        } catch (e) {
            console.error("Failed to fetch list items:", e);
            setItemsError('Failed to load items. Please try again.');
            setListItems([]);
        } finally {
            setItemsLoading(false);
        }
    };

    const handleToggleViewItems = (listId) => {
        setSuccessMessage('');
        if (viewingListId === listId) {
            setViewingListId(null);
            setListItems([]);
        } else {
            setViewingListId(listId);
            fetchListItems(listId);
            setAddingToListId(null);
        }
    };

    const handleToggleItemPurchased = async (itemId, currentPurchasedStatus) => {
        const newPurchasedStatus = !currentPurchasedStatus;
        setError(null);
        setSuccessMessage('');
        setListItems(prevItems =>
            prevItems.map(item =>
                item.id === itemId ? {...item, purchased: newPurchasedStatus} : item
            )
        );

        try {
            const response = await fetch(`${API_BASE_URL}/items/${itemId}/purchase?purchased=${newPurchasedStatus}`, {
                method: 'PATCH',
                headers: defaultHeaders(),
            });

            if (!response.ok) {
                setListItems(prevItems =>
                    prevItems.map(item =>
                        item.id === itemId ? {...item, purchased: currentPurchasedStatus} : item
                    )
                );
                throw new Error(`Failed to update purchase status: ${response.status}`);
            }
            const updatedItem = await response.json();
            setListItems(prevItems =>
                prevItems.map(item =>
                    item.id === updatedItem.id ? updatedItem : item
                )
            );
        } catch (e) {
            console.error("Failed to update item purchase status:", e);
            setError('Failed to update item. Please try again.');
            setListItems(prevItems =>
                prevItems.map(item =>
                    item.id === itemId ? {...item, purchased: currentPurchasedStatus} : item
                )
            );
        }
    };

    const handleDeleteItem = async (itemId, listId, itemName) => {
        if (!window.confirm(`Are you sure you want to delete item "${itemName}"?`)) {
            return;
        }
        setError(null);
        setSuccessMessage('');
        try {
            const response = await fetch(`${API_BASE_URL}/items/${itemId}`, {
                method: 'DELETE',
                headers: defaultHeaders(),
            });
            if (!response.ok) {
                throw new Error(`Failed to delete item: ${response.status}`);
            }
            setSuccessMessage(`Item "${itemName}" deleted successfully.`);
            await fetchListItems(listId);
            addActivityLog("DELETED_ITEM", itemName, `${userData.username} deleted item "${itemName}" from a grocery list.`);
        } catch (e) {
            console.error("Error deleting item:", e);
            setError(`Failed to delete item: ${e.message}`);
        }
    };


    const openPriceExpiryModal = (listId) => {
        const purchasedItems = listItems.filter(item => item.purchased);
        if (purchasedItems.length === 0) {
            setError("No purchased items to add to the fridge.");
            return;
        }
        setItemsForModal(purchasedItems.map(item => ({
            groceryItemId: item.id,
            category: item.category,
            name: item.name,
            quantity: item.quantity,
            price: '',
            expirationDate: ''
        })));
        setCurrentListIdForModal(listId);
        setIsPriceExpiryModalOpen(true);
        setError(null);
    };

    const handleModalItemChange = (index, field, value) => {
        setItemsForModal(prevItems =>
            prevItems.map((item, i) =>
                i === index ? {...item, [field]: value} : item
            )
        );
    };

    const handleSubmitFridgeDetails = async () => {
        if (!currentListIdForModal) return;
        setError(null);
        setSuccessMessage('');

        for (const item of itemsForModal) {
            if (item.price && isNaN(parseFloat(item.price))) {
                setError(`Invalid price for ${item.name}. Please enter a number.`);
                return;
            }
            if (item.expirationDate && isNaN(new Date(item.expirationDate).getTime())) {
                setError(`Invalid expiration date for ${item.name}.`);
                return;
            }
        }

        const payload = itemsForModal.map(item => ({
            id: item.groceryItemId,
            name: item.name,
            description: item.description,
            quantity: item.quantity,
            category: item.category,
            price: item.price ? parseFloat(item.price) : null,
            expirationDate: item.expirationDate || null,
        }));

        try {
            const response = await fetch(`${API_BASE_URL}/${currentListIdForModal}/move-to-fridge`, {
                method: 'POST',
                headers: defaultHeaders(),
                body: JSON.stringify(payload),
            });

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(`Failed to move items to fridge: ${response.status} - ${errorText}`);
            }
            setSuccessMessage('Purchased items moved to fridge with details!');
            setIsPriceExpiryModalOpen(false);
            setItemsForModal([]);
            setViewingListId(null);
            for (const newItem of payload) {
                addActivityLog(
                    "ADDED",
                    newItem.name,
                    `Added ${newItem.quantity} ${newItem.name} (${newItem.category}) with expiry date ${newItem.expirationDate}`
                );
            }
        } catch (e) {
            console.error("Error moving items to fridge:", e);
            setError(`Failed to move items: ${e.message}`);
        }
    };

    if (loading && !(userData && userData.fridgeId && userData.fridgeId !== 0)) {
        return <div className="gl-container"><p className="gl-info-message">Connect to a fridge to manage grocery
            lists.</p></div>;
    }
    if (loading) {
        return <div className="gl-container"><p className="gl-loading">Loading grocery lists...</p></div>;
    }
    return (
        <div className="gl-container">
            <h2 className="gl-main-header">My Grocery Lists</h2>

            {error && <div className="gl-alert gl-alert-error">Error: {error}</div>}
            {successMessage && <div className="gl-alert gl-alert-success">{successMessage}</div>}

            <form onSubmit={handleAddList} className="gl-form gl-add-list-form">
                <h2 className="gl-form-title">Add New Grocery List</h2>
                <div className="gl-form-group">
                    <label htmlFor="listName">List Name:</label>
                    <input
                        id="listName"
                        type="text"
                        value={newListName}
                        onChange={(e) => setNewListName(e.target.value)}
                        placeholder="e.g., Weekly Groceries"
                        required
                    />
                </div>
                <div className="gl-form-group">
                    <label htmlFor="listDesc">Description (Optional):</label>
                    <input
                        id="listDesc"
                        type="text"
                        value={newListDescription}
                        onChange={(e) => setNewListDescription(e.target.value)}
                        placeholder="e.g., For the first week of June"
                    />
                </div>
                <button type="submit" className="gl-button gl-button-primary">Add List</button>
            </form>

            {lists.length === 0 && !loading && (
                <p className="gl-info-message">No grocery lists found. Add one above to get started!</p>
            )}

            <ul className="gl-list-group">
                {lists.map(list => (
                    <li key={list.id} className="gl-list-item">
                        <div className="gl-list-item-header">
                            <span className="gl-list-name">{list.name}</span>
                            <span className="gl-list-description">{list.description}</span>
                            <span
                                className="gl-list-created">Created: {new Date(list.createdAt).toLocaleDateString()}</span>
                        </div>
                        <div className="gl-list-item-actions">
                            <button onClick={() => handleToggleViewItems(list.id)}
                                    className="gl-button gl-button-secondary">
                                {viewingListId === list.id ? 'Hide Items' : 'View Items'}
                            </button>
                            <button onClick={() => handleToggleAddItemForm(list.id)}
                                    className="gl-button gl-button-secondary">
                                {addingToListId === list.id ? 'Cancel Add' : 'Add Item'}
                            </button>
                            <button onClick={() => handleDeleteList(list.id, list.name)}
                                    className="gl-button gl-button-danger">
                                Delete List
                            </button>
                        </div>

                        {addingToListId === list.id && (
                            <form onSubmit={(e) => handleAddItemSubmit(e, list.id)}
                                  className="gl-form gl-add-item-form">
                                <h3 className="gl-form-title">Add New Item to "{list.name}"</h3>
                                <div className="gl-form-group">
                                    <label htmlFor={`itemName-${list.id}`}>Item Name:</label>
                                    <input id={`itemName-${list.id}`} type="text" name="name" value={newItem.name}
                                           onChange={handleNewItemChange} placeholder="e.g., Milk" required/>
                                </div>
                                <div className="gl-form-group">
                                    <label htmlFor={`itemDesc-${list.id}`}>Description (Optional):</label>
                                    <input id={`itemDesc-${list.id}`} type="text" name="description"
                                           value={newItem.description} onChange={handleNewItemChange}
                                           placeholder="e.g., Organic, 1 gallon"/>
                                </div>
                                <div className="gl-form-group">
                                    <label htmlFor={`itemQty-${list.id}`}>Quantity:</label>
                                    <input id={`itemQty-${list.id}`} type="number" name="quantity"
                                           value={newItem.quantity} onChange={handleNewItemChange} min="1" required/>
                                </div>
                                <div className="gl-form-group">
                                    <label htmlFor={`itemCat-${list.id}`}>Category (Optional):</label>
                                    <input id={`itemCat-${list.id}`} type="text" name="category"
                                           value={newItem.category} onChange={handleNewItemChange}
                                           placeholder="e.g., Dairy"/>
                                </div>
                                <button type="submit" className="gl-button gl-button-primary">Add Item</button>
                                <button type="button" onClick={() => setAddingToListId(null)}
                                        className="gl-button gl-button-secondary">Cancel
                                </button>
                            </form>
                        )}

                        {viewingListId === list.id && (
                            <div className="gl-items-section">
                                <h3 className="gl-items-title">Items in "{list.name}"</h3>
                                {itemsLoading && <p className="gl-loading">Loading items...</p>}
                                {itemsError && <div className="gl-alert gl-alert-error">Error: {itemsError}</div>}
                                {!itemsLoading && !itemsError && listItems.length === 0 && (
                                    <p className="gl-info-message">No items in this list yet.</p>
                                )}
                                {!itemsLoading && !itemsError && listItems.length > 0 && (
                                    <>
                                        <ul className="gl-item-group">
                                            {listItems.map(item => (
                                                <li key={item.id}
                                                    className={`gl-item ${item.purchased ? 'gl-item-purchased' : ''}`}>
                                                    <div className="gl-item-info">
                                                        <input
                                                            type="checkbox"
                                                            checked={item.purchased}
                                                            onChange={() => handleToggleItemPurchased(item.id, item.purchased, list.id)}
                                                            id={`item-purchased-${item.id}`}
                                                            className="gl-item-checkbox"
                                                        />
                                                        <label htmlFor={`item-purchased-${item.id}`}
                                                               className="gl-item-name">
                                                            {item.name}
                                                        </label>
                                                        <span className="gl-item-details">
                                                            (Qty: {item.quantity})
                                                            {item.category && ` - ${item.category}`}
                                                            {item.description && ` - ${item.description}`}
                                                        </span>
                                                    </div>
                                                    <button
                                                        onClick={() => handleDeleteItem(item.id, list.id, item.name)}
                                                        className="gl-button gl-button-danger gl-button-small"
                                                        title="Delete Item"
                                                    >
                                                        &times;
                                                    </button>
                                                </li>
                                            ))}
                                        </ul>
                                        {allItemsPurchasedInView && (
                                            <button onClick={() => openPriceExpiryModal(list.id)}
                                                    className="gl-button gl-button-success gl-move-fridge-btn">
                                                Move Purchased to Fridge
                                            </button>
                                        )}
                                    </>
                                )}
                            </div>
                        )}
                    </li>
                ))}
            </ul>

            {isPriceExpiryModalOpen && (
                <div className="gl-modal-overlay">
                    <div className="gl-modal">
                        <h2 className="gl-modal-title">Add Details for Fridge Items</h2>
                        <p className="gl-modal-instructions">Enter price and expiration for items being moved to the
                            fridge. Leave blank if not applicable.</p>
                        {itemsForModal.map((item, index) => (
                            <div key={index} className="gl-modal-item-form">
                                <h4 className="gl-modal-item-name">{item.name} (Qty: {item.quantity},
                                    Cat: {item.category || 'N/A'})</h4>
                                <div className="gl-form-group">
                                    <label htmlFor={`price-${index}`}>Price (per unit or total):</label>
                                    <input
                                        id={`price-${index}`}
                                        type="number"
                                        step="0.01"
                                        min="0"
                                        value={item.price}
                                        onChange={(e) => handleModalItemChange(index, 'price', e.target.value)}
                                        placeholder="e.g., 2.99"
                                    />
                                </div>
                                <div className="gl-form-group">
                                    <label htmlFor={`expiry-${index}`}>Expiration Date:</label>
                                    <input
                                        id={`expiry-${index}`}
                                        type="date"
                                        value={item.expirationDate}
                                        onChange={(e) => handleModalItemChange(index, 'expirationDate', e.target.value)}
                                    />
                                </div>
                            </div>
                        ))}
                        <div className="gl-modal-actions">
                            <button onClick={handleSubmitFridgeDetails} className="gl-button gl-button-primary">Submit
                                to Fridge
                            </button>
                            <button onClick={() => setIsPriceExpiryModalOpen(false)}
                                    className="gl-button gl-button-secondary">Cancel
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}


export default GroceryLists;