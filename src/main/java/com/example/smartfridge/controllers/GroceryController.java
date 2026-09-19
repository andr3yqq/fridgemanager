package com.example.smartfridge.controllers;

import com.example.smartfridge.dtos.GroceryItemDto;
import com.example.smartfridge.dtos.GroceryListDto;
import com.example.smartfridge.dtos.ItemRecordDto;
import com.example.smartfridge.services.GroceryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteList(@PathVariable Long listId) {
        groceryService.deleteList(listId);
        return ResponseEntity.noContent().build();
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
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteItemFromList(@PathVariable Long id) {
        groceryService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{listId}")
    public ResponseEntity<GroceryListDto> updateList(@RequestBody GroceryListDto groceryListDto) {
        return ResponseEntity.ok(groceryService.updateList(groceryListDto.getId(), groceryListDto.getName(), groceryListDto.getDescription()));
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<Void> updateItem(@RequestBody GroceryItemDto groceryItemDto) {
        groceryService.updateItem(groceryItemDto);
        return ResponseEntity.ok().build();
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
