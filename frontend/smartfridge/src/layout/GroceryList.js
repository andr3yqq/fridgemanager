import React, {useCallback, useEffect, useState} from 'react';
import {useAppContext} from "../context/AppContext";
import defaultHeaders from "./defaultHeaders";
import {addActivityLog} from "./ActivityLogs";

const API_BASE_URL = 'http://localhost:8080/api/grocery';

const Modal = ({isOpen, onClose, title, children}) => {
    if (!isOpen) return null;

    return (
        <div style={{
            position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
            backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex',
            alignItems: 'center', justifyContent: 'center', zIndex: 1000
        }}>
            <div style={{
                backgroundColor: 'white', padding: '20px', borderRadius: '8px',
                minWidth: '300px', maxWidth: '600px', maxHeight: '90vh', overflowY: 'auto'
            }}>
                <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center'}}>
                    <h3>{title}</h3>
                    <button onClick={onClose} style={{
                        background: 'none',
                        border: 'none',
                        fontSize: '1.5rem',
                        cursor: 'pointer'
                    }}>&times;</button>
                </div>
                {children}
            </div>
        </div>
    );
}

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
        quantity: 0,
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

        } catch (e) {
            console.error("Error adding list:", e);
            setError(`Failed to add list: ${e.message}`);
        }
    };

    const handleDeleteList = async (listId) => {
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
        setError(null);
        setSuccessMessage('');
        const newItemDto = {
            name: newItem.name,
            description: newItem.description || null,
            quantity: newItem.quantity || 0,
            category: newItem.category || null,
            purchased: newItem.purchased || false,
        };

        console.log("Attempting to add item:", newItemDto, "to list:", listId);

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


    if (loading) {
        return <div>Loading grocery lists...</div>;
    }

    return (
        <div>
            <h2>My Grocery Lists</h2>

            {error && <div style={{
                color: 'red',
                marginBottom: '10px',
                padding: '10px',
                border: '1px solid red',
                borderRadius: '4px'
            }}>Error: {error}</div>}
            {successMessage && <div style={{
                color: 'green',
                marginBottom: '10px',
                padding: '10px',
                border: '1px solid green',
                borderRadius: '4px'
            }}>{successMessage}</div>}

            <form onSubmit={handleAddList}
                  style={{marginBottom: '20px', border: '1px solid #ccc', padding: '15px', borderRadius: '5px'}}>
                <h3>Add New List</h3>
                <div>
                    <label htmlFor="listName">Name: </label>
                    <input
                        id="listName"
                        type="text"
                        value={newListName}
                        onChange={(e) => setNewListName(e.target.value)}
                        placeholder="e.g., Weekly Groceries"
                        required
                        style={{marginRight: '10px'}}
                    />
                </div>
                <div style={{marginTop: '10px'}}>
                    <label htmlFor="listDesc">Description: </label>
                    <input
                        id="listDesc"
                        type="text"
                        value={newListDescription}
                        onChange={(e) => setNewListDescription(e.target.value)}
                        placeholder="Optional description"
                        style={{marginRight: '10px'}}
                    />
                </div>
                <button type="submit" style={{marginTop: '10px'}}>Add List</button>
            </form>

            {lists.length === 0 ? (
                <p>No grocery lists found. Add one above!</p>
            ) : (
                <ul>
                    {lists.map(list => (
                        <li key={list.id}
                            style={{marginBottom: '10px', padding: '5px', borderBottom: '1px solid #eee'}}>
                            <div style={{
                                display: 'flex',
                                justifyContent: 'space-between',
                                alignItems: 'center',
                                marginBottom: addingToListId === list.id ? '10px' : '0'
                            }}>
                                <div>
                                    <strong>{list.name}</strong>
                                    {list.description &&
                                        <span style={{marginLeft: '10px', color: '#555'}}> - {list.description}</span>}
                                </div>
                                <div>
                                    <button
                                        onClick={() => handleToggleViewItems(list.id)}
                                        style={{marginLeft: '10px', cursor: 'pointer'}}
                                        aria-expanded={viewingListId === list.id}
                                    >
                                        {viewingListId === list.id ? 'Hide Items' : 'View Items'}
                                    </button>
                                    <button
                                        onClick={() => handleToggleAddItemForm(list.id)}
                                        style={{marginLeft: '15px', cursor: 'pointer'}}
                                        aria-expanded={addingToListId === list.id}
                                    >
                                        {addingToListId === list.id ? 'Cancel Add' : 'Add Item'}
                                    </button>
                                    <button
                                        onClick={() => handleDeleteList(list.id)}
                                        style={{
                                            marginLeft: '10px',
                                            color: 'red',
                                            cursor: 'pointer',
                                            background: 'none',
                                            border: 'none',
                                            padding: '0 5px'
                                        }}
                                        title="Delete list"
                                    >
                                        Delete List
                                    </button>
                                </div>
                            </div>
                            {viewingListId === list.id && (
                                <div style={{marginTop: '15px', paddingLeft: '20px'}}>
                                    {itemsLoading && <p>Loading items...</p>}
                                    {itemsError && <p style={{color: 'red'}}>{itemsError}</p>}
                                    {!itemsLoading && !itemsError && listItems.length === 0 && (
                                        <p>No items in this list yet.</p>
                                    )}
                                    {!itemsLoading && !itemsError && listItems.length > 0 && (
                                        <>
                                            <ul style={{listStyleType: 'none', paddingLeft: 0}}>
                                                {listItems.map(item => (
                                                    <li key={item.id} style={{
                                                        padding: '8px',
                                                        borderBottom: '1px solid #f0f0f0',
                                                        display: 'flex',
                                                        alignItems: 'center',
                                                        textDecoration: item.purchased ? 'line-through' : 'none',
                                                        color: item.purchased ? '#888' : 'inherit'
                                                    }}>
                                                        <input
                                                            type="checkbox"
                                                            checked={item.purchased}
                                                            onChange={() => handleToggleItemPurchased(item.id, item.purchased)}
                                                            style={{marginRight: '10px', cursor: 'pointer'}}
                                                        />
                                                        <span>
                                                        <strong>{item.name}</strong>
                                                            {item.description && ` - ${item.description}`}
                                                            {item.quantity && ` (Qty: ${item.quantity})`}
                                                            {item.category && ` [${item.category}]`}
                                                    </span>
                                                    </li>
                                                ))}
                                            </ul>
                                            {allItemsPurchasedInView && (
                                                <button
                                                    onClick={() => openPriceExpiryModal(list.id)}
                                                    style={{
                                                        marginTop: '15px',
                                                        backgroundColor: '#28a745',
                                                        color: 'white',
                                                        padding: '8px 15px',
                                                        border: 'none',
                                                        borderRadius: '4px',
                                                        cursor: 'pointer'
                                                    }}
                                                >
                                                    Add All to Fridge
                                                </button>
                                            )}
                                        </>
                                    )}
                                </div>
                            )}
                            {addingToListId === list.id && (
                                <form onSubmit={(e) => handleAddItemSubmit(e, list.id)} style={{
                                    marginTop: '10px',
                                    padding: '10px',
                                    border: '1px dashed #ccc',
                                    borderRadius: '4px'
                                }}>
                                    <div style={{marginBottom: '5px'}}>
                                        <label htmlFor={`itemName-${list.id}`}>Item Name: </label>
                                        <input
                                            id={`itemName-${list.id}`}
                                            type="text"
                                            name="name"
                                            value={newItem.name}
                                            onChange={handleNewItemChange}
                                            placeholder="e.g., Milk"
                                            required
                                            style={{marginRight: '10px'}}
                                        />
                                    </div>
                                    <div style={{marginBottom: '5px'}}>
                                        <label htmlFor={`itemName-${list.id}`}>Item Description: </label>
                                        <input
                                            id={`itemDesc-${list.id}`}
                                            type="text"
                                            name="description"
                                            value={newItem.description}
                                            onChange={handleNewItemChange}
                                            placeholder="e.g., Milk lidl 100ml"
                                            style={{marginRight: '10px'}}
                                        />
                                    </div>
                                    <div style={{marginBottom: '5px'}}>
                                        <label htmlFor={`itemQty-${list.id}`}>Quantity: </label>
                                        <input
                                            id={`itemQty-${list.id}`}
                                            type="text"
                                            name="quantity"
                                            value={newItem.quantity}
                                            onChange={handleNewItemChange}
                                            placeholder="e.g., 1 Gallon (Optional)"
                                            style={{marginRight: '10px'}}
                                        />
                                    </div>
                                    <div style={{marginBottom: '5px'}}>
                                        <label htmlFor={`itemCat-${list.id}`}>Category: </label>
                                        <input
                                            id={`itemCat-${list.id}`}
                                            type="text"
                                            name="category"
                                            value={newItem.category}
                                            onChange={handleNewItemChange}
                                            placeholder="e.g., Dairy (Optional)"
                                            style={{marginRight: '10px'}}
                                        />
                                    </div>
                                    <button type="submit">Save Item</button>
                                    <button type="button" onClick={() => setAddingToListId(null)}
                                            style={{marginLeft: '10px'}}>Cancel
                                    </button>
                                </form>
                            )}

                        </li>
                    ))}
                </ul>
            )}
            <Modal isOpen={isPriceExpiryModalOpen} onClose={() => setIsPriceExpiryModalOpen(false)}
                   title="Enter Item Details for Fridge">
                {error &&
                    <div style={{color: 'red', marginBottom: '10px'}}>{error}</div>} {/* Error display inside modal */}
                <form onSubmit={(e) => {
                    e.preventDefault();
                    handleSubmitFridgeDetails();
                }}>
                    {itemsForModal.map((item, index) => (
                        <div key={item.groceryItemId}
                             style={{marginBottom: '15px', paddingBottom: '10px', borderBottom: '1px solid #eee'}}>
                            <strong>{item.name}</strong> (Qty: {item.quantity || 1})
                            <div style={{marginTop: '5px'}}>
                                <label htmlFor={`price-${item.groceryItemId}`}
                                       style={{marginRight: '5px'}}>Price:</label>
                                <input
                                    type="number"
                                    id={`price-${item.groceryItemId}`}
                                    value={item.price}
                                    onChange={(e) => handleModalItemChange(index, 'price', e.target.value)}
                                    placeholder="e.g., 2.99"
                                    step="0.01"
                                    style={{width: '80px', marginRight: '10px'}}
                                />
                                <label htmlFor={`expiry-${item.groceryItemId}`} style={{marginRight: '5px'}}>Expiry
                                    Date:</label>
                                <input
                                    type="date"
                                    id={`expiry-${item.groceryItemId}`}
                                    value={item.expirationDate}
                                    onChange={(e) => handleModalItemChange(index, 'expirationDate', e.target.value)}
                                    style={{width: '150px'}}
                                />
                            </div>
                        </div>
                    ))}
                    <button type="submit" style={{
                        marginTop: '10px',
                        padding: '10px 15px',
                        backgroundColor: '#007bff',
                        color: 'white',
                        border: 'none',
                        borderRadius: '4px',
                        cursor: 'pointer'
                    }}>
                        Confirm & Add to Fridge
                    </button>
                </form>
            </Modal>
        </div>
    );
}

export default GroceryLists;