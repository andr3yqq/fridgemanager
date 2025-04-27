package com.example.smartfridge.repositories;

import com.example.smartfridge.entities.GroceryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroceryItemRepository extends JpaRepository<GroceryItem, Long> {
    List<GroceryItem> findByGroceryListId(Long groceryListId);
}
