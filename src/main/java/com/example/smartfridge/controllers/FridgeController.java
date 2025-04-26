package com.example.smartfridge.controllers;


import com.example.smartfridge.dtos.*;
import com.example.smartfridge.services.FridgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
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
        return ResponseEntity.ok(fridgeService.checkCurrentUserFridge());
    }

    @PostMapping("/fridge")
    public ResponseEntity<FridgeDto> createFridge(@RequestBody String fridgeName) {
        FridgeDto createdFridge = fridgeService.createFridge(fridgeName);
        return ResponseEntity.created(URI.create("/fridge/" + createdFridge.getId())).body(createdFridge);
    }

    @DeleteMapping("/fridge")
    public ResponseEntity<FridgeDto> deleteFridge() {
        FridgeDto deletedFridge = fridgeService.deleteFridge();
        return ResponseEntity.ok(deletedFridge);
    }

    @GetMapping("/invites")
    public ResponseEntity<List<InvitesResponseDto>> getInvitesForUser() {
        List<FridgeInvitesDto> invites = fridgeService.getInvitesForUser();

        List<InvitesResponseDto> invitesDto = new ArrayList<>();
        invites.forEach(invite -> {
            invitesDto.add(new InvitesResponseDto(invite.getId(),fridgeService.getInviteUsername(invite.getId()),fridgeService.getInviteFridgeName(invite.getId())));
        });
        return ResponseEntity.ok(invitesDto);
    }

    @PostMapping("/invite")
    public ResponseEntity<FridgeInvitesDto> inviteUser(@RequestBody String username) {
        FridgeInvitesDto invite = fridgeService.inviteUser(username);
        System.out.println(invite);
        return ResponseEntity.ok(invite);
    }

    @PostMapping("/fridge/join")
    public ResponseEntity<UserDto> joinFridge(@RequestBody Long inviteId) {
        UserDto user = fridgeService.joinFridge(inviteId);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/fridge/leave")
    public ResponseEntity<UserDto> leaveFridge() {
        UserDto user = fridgeService.leaveFridge();
        return ResponseEntity.ok(user);
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
