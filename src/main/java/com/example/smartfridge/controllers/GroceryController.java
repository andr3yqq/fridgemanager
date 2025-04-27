package com.example.smartfridge.controllers;

import com.example.smartfridge.dtos.GroceryItemDto;
import com.example.smartfridge.dtos.GroceryListDto;
import com.example.smartfridge.services.GroceryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/grocery")
public class GroceryController {

    private final GroceryService groceryService;

    @GetMapping
    public ResponseEntity<List<GroceryListDto>> getListsByFridge() {
        return ResponseEntity.ok(groceryService.allLists());
    }

    @GetMapping("/{listId}")
    public ResponseEntity<GroceryListDto> getListById(@PathVariable Long listId) {
        GroceryListDto list = groceryService.getListById(listId);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/{name}")
    public ResponseEntity<GroceryListDto> createList(@PathVariable String name, @RequestBody String description) {
        GroceryListDto createdList = groceryService.createList(name, description);
        return ResponseEntity.created(URI.create(createdList.getId().toString())).body(createdList);
    }

    @DeleteMapping("/{listId}")
    public ResponseEntity<GroceryListDto> deleteList(@PathVariable Long listId) {
        GroceryListDto deletedList = groceryService.deleteList(listId);
        return ResponseEntity.ok(deletedList);
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<List<GroceryItemDto>> getItemsByListId(@PathVariable Long id) {
        List<GroceryItemDto> items = groceryService.getItemsByListId(id);
        return ResponseEntity.ok(items);
    }

    @PostMapping("/add/{id}")
    public ResponseEntity<GroceryItemDto> addItemToList(@RequestBody GroceryItemDto groceryItemDto, @PathVariable Long id) {
        return ResponseEntity.ok(groceryService.addItemToList(groceryItemDto, id));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<GroceryItemDto> deleteItemFromList(@PathVariable Long id) {
        return ResponseEntity.ok(groceryService.deleteItem(id));
    }

    @PutMapping("/{listId}")
    public ResponseEntity<GroceryListDto> updateList(@RequestBody GroceryListDto groceryListDto) {
        return ResponseEntity.ok(groceryService.updateList(groceryListDto.getId(), groceryListDto.getName(), groceryListDto.getDescription()));
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<GroceryItemDto> updateItem(@RequestBody GroceryItemDto groceryItemDto) {
        return ResponseEntity.ok(groceryService.updateItem(groceryItemDto));
    }

}
