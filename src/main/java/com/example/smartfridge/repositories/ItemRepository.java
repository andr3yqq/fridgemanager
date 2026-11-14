package com.example.smartfridge.repositories;

import com.example.smartfridge.entities.Fridge;
import com.example.smartfridge.entities.ItemRecord;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@JaversSpringDataAuditable
public interface ItemRepository extends JpaRepository<ItemRecord, Long> {
    Page<ItemRecord> findAllByFridge(Fridge fridge, Pageable pageable);
}
