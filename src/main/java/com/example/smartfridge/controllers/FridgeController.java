package com.example.smartfridge.controllers;


import com.example.smartfridge.dtos.ItemRecordDto;
import com.example.smartfridge.services.FridgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api")
public class FridgeController {

    private final FridgeService fridgeService;

    @GetMapping("/items")
    public ResponseEntity<List<ItemRecordDto>> allItemsByUser() {
        return ResponseEntity.ok(fridgeService.allItemsByFridge());
    }

    /*public ResponseEntity<List<ItemRecordDto>> allItems() {
        return ResponseEntity.ok(fridgeService.allItems());
    }*/

    @GetMapping("/items/{id}")
    public ResponseEntity<ItemRecordDto> getItem(@PathVariable long id) {
        ItemRecordDto itemRecordDto = fridgeService.getItemById(id);
        return ResponseEntity.ok(itemRecordDto);
    }

    @PostMapping("/items")
    public ResponseEntity<ItemRecordDto> createItem(@RequestBody ItemRecordDto itemRecordDto) {
        ItemRecordDto createdItem = fridgeService.createItem(itemRecordDto);
        return ResponseEntity.created(URI.create("/items/" + createdItem.getId())).body(createdItem);
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<ItemRecordDto> updateItem(@RequestBody ItemRecordDto itemRecordDto) {
        return ResponseEntity.ok(fridgeService.updateItem(itemRecordDto));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<ItemRecordDto> deleteItem(@PathVariable long id) {
        ItemRecordDto deletedItem = fridgeService.deleteItem(id);
        return ResponseEntity.ok(deletedItem);
    }
}
