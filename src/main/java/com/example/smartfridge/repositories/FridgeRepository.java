package com.example.smartfridge.repositories;

import com.example.smartfridge.entities.ItemRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FridgeRepository extends JpaRepository<ItemRecord, Long> {

}
