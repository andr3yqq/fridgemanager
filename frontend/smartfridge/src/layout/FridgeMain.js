import Header from "./Header";
import Footer from "./Footer";
import React, {useEffect, useState} from "react";

function FridgeMain() {
    const [fridgeItems, setFridgeItems] = useState([
        /*{ id: 1, name: "Milk", quantity: 1, category: "Dairy", expiryDate: "2025-03-10" },
        { id: 2, name: "Eggs", quantity: 12, category: "Dairy", expiryDate: "2025-03-15" },
        { id: 3, name: "Chicken", quantity: 2, category: "Meat", expiryDate: "2025-03-05" },
        { id: 4, name: "Spinach", quantity: 1, category: "Vegetables", expiryDate: "2025-03-07" },
        { id: 5, name: "Yogurt", quantity: 4, category: "Dairy", expiryDate: "2025-03-12" },
        { id: 6, name: "Apples", quantity: 6, category: "Fruits", expiryDate: "2025-03-20" }*/
    ]);

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

    const handleAddItem = () => {
        if (newItem.name.trim() === "") return;
        setFridgeItems([
            ...fridgeItems,
            { ...newItem, id: fridgeItems.length + 1 }
        ]);
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
        setFridgeItems(fridgeItems.filter(item => item.id !== id));
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
    const getExpiryColor = (expiryDate) => {
        const days = getDaysUntilExpiry(expiryDate);
        if (days < 0) return "text-red-600";
        if (days < 3) return "text-orange-500";
        return "text-green-600";
    };

    // Filter items based on search input
    const filteredItems = fridgeItems.filter(item =>
        item.name.toLowerCase().includes(filter.toLowerCase()) ||
        item.category.toLowerCase().includes(filter.toLowerCase())
    );
    return (
        <div className="max-w-4xl mx-auto p-4">

            {/* Add new item form */}
            <div className="bg-gray-100 p-4 rounded-lg mb-6">
                <h2 className="text-lg font-semibold mb-2">Add New Item</h2>
                <div className="flex flex-wrap gap-2">
                    <input
                        type="text"
                        placeholder="Item name"
                        className="p-2 border rounded"
                        value={newItem.name}
                        onChange={(e) => setNewItem({...newItem, name: e.target.value})}
                    />
                    <input
                        type="text"
                        placeholder="Item description"
                        className="p-2 border rounded"
                        value={newItem.description}
                        onChange={(e) => setNewItem({...newItem, description: e.target.value})}
                    />
                    <input
                        type="number"
                        min="1"
                        className="p-2 border rounded w-20"
                        value={newItem.quantity}
                        onChange={(e) => setNewItem({...newItem, quantity: parseInt(e.target.value)})}
                    />
                    <select
                        className="p-2 border rounded"
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
                    <input
                        type="date"
                        className="p-2 border rounded"
                        value={newItem.expirationDate}
                        onChange={(e) => setNewItem({...newItem, expirationDate: e.target.value})}
                    />
                    <input
                        type="date"
                        className="p-2 border rounded"
                        value={newItem.buyingDate}
                        onChange={(e) => setNewItem({...newItem, buyingDate: e.target.value})}
                    />
                    <button
                        className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
                        onClick={handleAddItem}
                    >
                        Add
                    </button>
                </div>
            </div>

            {/* Search filter */}
            <div className="mb-4">
                <input
                    type="text"
                    placeholder="Search items or categories..."
                    className="p-2 border rounded w-full"
                    value={filter}
                    onChange={(e) => setFilter(e.target.value)}
                />
            </div>

            {/* Items list */}
            <div className="bg-white shadow rounded-lg">
                {filteredItems.length === 0 ? (
                    <p className="p-4 text-center text-gray-500">No items in your fridge.</p>
                ) : (
                    <table className="min-w-full divide-y divide-gray-200">
                        <thead className="bg-gray-50">
                        <tr>
                            <th className="p-4 text-left text-sm font-medium text-gray-500">Item</th>
                            <th className="p-4 text-left text-sm font-medium text-gray-500">Quantity</th>
                            <th className="p-4 text-left text-sm font-medium text-gray-500">Expires</th>
                            <th className="p-4 text-left text-sm font-medium text-gray-500">Action</th>
                        </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-200">
                        {filteredItems.map(item => (
                            <tr key={item.id}>
                                <td className="p-4">{item.name}</td>
                                <td className="p-4">{item.quantity}</td>
                                <td className={`p-4 ${getExpiryColor(item.expirationDate)}`}>
                                    {item.expirationDate}
                                    <span className="text-sm ml-2">
                      ({getDaysUntilExpiry(item.expirationDate)} days)
                    </span>
                                </td>
                                <td className="p-4">
                                    <button
                                        className="text-red-500 hover:text-red-700"
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
        </div>
    );
}

export default FridgeMain;