import React, {useCallback, useEffect, useState} from 'react';
import {useAppContext} from "../context/AppContext";
import defaultHeaders from "./defaultHeaders";

const API_BASE_URL = 'http://localhost:8080/api/grocery';

function GroceryLists() {
    const [lists, setLists] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
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
    }, []);

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
        try {
            const response = await fetch(`${API_BASE_URL}/${listId}`, {
                method: 'DELETE',
                headers: defaultHeaders(),
            });
            if (!response.ok) {
                throw new Error(`Failed to delete list: ${response.status}`);
            }
            setLists(prevLists => prevLists.filter(list => list.id !== listId));
        } catch (e) {
            console.error("Error deleting list:", e);
            setError(`Failed to delete list: ${e.message}`);
        }
    };

    const handleToggleAddItemForm = (listId) => {
        if (addingToListId === listId) {
            setAddingToListId(null);
        } else {
            setAddingToListId(listId);
            setNewItem(initialNewItemState);
            setError(null);
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
        const newItemDto = {
            name: newItem.name,
            description: newItem.description || null,
            quantity: newItem.quantity || null,
            category: newItem.category || null,
            purchased: false,
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

            console.log("Item added successfully!");
            setNewItem(initialNewItemState);
            setAddingToListId(null);
            // Optionally: show a success message
            // Note: This component doesn't display items, so no list update needed here.
            // A separate component showing list details would fetch updated items.

        } catch (e) {
            console.error("Error adding item:", e);
            setError(`Failed to add item: ${e.message}`);
        }
    };


    if (loading) {
        return <div>Loading grocery lists...</div>;
    }

    if (error) {
        return <div style={{color: 'red'}}>Error: {error}</div>;
    }

    return (
        <div>
            <h2>My Grocery Lists</h2>

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
                                    {/* Add link/button to view items if you have a separate details page */}
                                    {/* Example: <Link to={`/lists/${list.id}`}>View Items</Link> */}
                                </div>
                                <div>
                                    <button
                                        onClick={() => handleToggleAddItemForm(list.id)}
                                        style={{marginLeft: '15px', cursor: 'pointer'}}
                                        aria-expanded={addingToListId === list.id}
                                    >
                                        {addingToListId === list.id ? 'Cancel Add' : 'Add Item'}
                                    </button>
                                    {/* You could add an Edit List button here too */}
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
                                            id={`itemName-${list.id}`}
                                            type="text"
                                            name="description"
                                            value={newItem.description}
                                            onChange={handleNewItemChange}
                                            placeholder="e.g., Milk lidl 100ml"
                                            required
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
        </div>
    );
}

export default GroceryLists;