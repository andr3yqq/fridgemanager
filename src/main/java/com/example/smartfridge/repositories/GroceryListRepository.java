package com.example.smartfridge.repositories;

import com.example.smartfridge.entities.GroceryList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroceryListRepository extends JpaRepository<GroceryList, Long> {
    List<GroceryList> findAllByFridgeId(Long fridgeId);
}
