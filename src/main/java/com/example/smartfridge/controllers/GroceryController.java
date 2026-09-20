package com.example.smartfridge.controllers;

import com.example.smartfridge.dtos.GroceryItemDto;
import com.example.smartfridge.dtos.GroceryListCreateDto;
import com.example.smartfridge.dtos.GroceryListDto;
import com.example.smartfridge.dtos.ItemRecordDto;
import com.example.smartfridge.services.GroceryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
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

    @PostMapping
    public ResponseEntity<GroceryListDto> createList(@RequestBody GroceryListCreateDto dto) {
        GroceryListDto createdList = groceryService.createList(dto.getName(), dto.getDescription());
        return ResponseEntity.created(URI.create("/api/grocery/" + createdList.getId().toString())).body(createdList);
    }

    @DeleteMapping("/{listId}")
    public ResponseEntity<Void> deleteList(@PathVariable Long listId) {
        groceryService.deleteList(listId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{listId}/items")
    public ResponseEntity<List<GroceryItemDto>> getItemsByListId(@PathVariable Long listId) {
        List<GroceryItemDto> items = groceryService.getItemsByListId(listId);
        return ResponseEntity.ok(items);
    }

    @PostMapping("/{listId}/items")
    public ResponseEntity<GroceryItemDto> addItemToList(@RequestBody GroceryItemDto groceryItemDto, @PathVariable Long listId) {
        return ResponseEntity.ok(groceryService.addItemToList(groceryItemDto, listId));
    }

    @DeleteMapping("/{listId}/items/{id}")
    public ResponseEntity<Void> deleteItemFromList(@PathVariable Long id) {
        groceryService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{listId}")
    public ResponseEntity<GroceryListDto> updateList(@RequestBody GroceryListDto groceryListDto, @PathVariable Long listId) {
        return ResponseEntity.ok(groceryService.updateList(listId, groceryListDto.getName(), groceryListDto.getDescription()));
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<GroceryItemDto> updateItem(@PathVariable Long id, @RequestBody GroceryItemDto groceryItemDto) {
        return ResponseEntity.ok(groceryService.updateItem(id, groceryItemDto));
    }

    @PatchMapping("/items/{itemId}/purchase")
    public ResponseEntity<GroceryItemDto> togglePurchaseItem(@PathVariable Long itemId, @RequestParam boolean purchased) {
        return ResponseEntity.ok(groceryService.toggleItemPurchased(itemId, purchased));
    }

    @PostMapping("/{listId}/move-to-fridge")
    public ResponseEntity<List<ItemRecordDto>> movePurchasedToFridge(@PathVariable Long listId, @RequestBody List<ItemRecordDto> items) {
        return ResponseEntity.ok(groceryService.copyPurchasedItemsToFridge(listId, items));
    }
}
