package com.example.smartfridge.repositories;

import com.example.smartfridge.entities.Fridge;
import com.example.smartfridge.entities.ItemRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<ItemRecord, Long> {
    List<ItemRecord> findAllByFridgeId(Fridge fridgeId);
}
