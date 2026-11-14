package com.example.smartfridge.repositories;

import com.example.smartfridge.entities.GroceryItem;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@JaversSpringDataAuditable
public interface GroceryItemRepository extends JpaRepository<GroceryItem, Long> {
}
