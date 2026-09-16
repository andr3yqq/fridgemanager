package com.example.smartfridge.controllers;


import com.example.smartfridge.dtos.*;
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

    @GetMapping("/fridge")
    public ResponseEntity<FridgeDto> allFridgeInfo() {
        return ResponseEntity.ok(fridgeService.getCurrentUserFridgeDto());
    }

    @PostMapping("/fridge")
    public ResponseEntity<FridgeDto> createFridge(@RequestBody String fridgeName) {
        FridgeDto createdFridge = fridgeService.createFridge(fridgeName);
        return ResponseEntity.created(URI.create("/fridge/" + createdFridge.getId())).body(createdFridge);
    }

    @DeleteMapping("/fridge")
    public ResponseEntity<Void> deleteFridge() {
        fridgeService.deleteFridge();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/invites")
    public ResponseEntity<List<InvitesResponseDto>> getInvitesForUser() {
        return ResponseEntity.ok(fridgeService.getInvitesForUser());
    }

    @PostMapping("/invite")
    public ResponseEntity<FridgeInvitesDto> inviteUser(@RequestBody String username) {
        return ResponseEntity.ok(fridgeService.inviteUser(username));
    }

    @PostMapping("/fridge/join")
    public ResponseEntity<UserDto> joinFridge(@RequestBody Long inviteId) {
        return ResponseEntity.ok(fridgeService.joinFridge(inviteId));
    }

    @PostMapping("/fridge/leave")
    public ResponseEntity<UserDto> leaveFridge() {
        return ResponseEntity.ok(fridgeService.leaveFridge());
    }

    @GetMapping("/items/{id}")
    public ResponseEntity<ItemRecordDto> getItem(@PathVariable long id) {
        return ResponseEntity.ok(fridgeService.getItemById(id));
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
        return ResponseEntity.ok(fridgeService.deleteItem(id));
    }
}
