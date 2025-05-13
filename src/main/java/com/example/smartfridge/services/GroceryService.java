package com.example.smartfridge.services;

import com.example.smartfridge.dtos.FridgeDto;
import com.example.smartfridge.dtos.GroceryItemDto;
import com.example.smartfridge.dtos.GroceryListDto;
import com.example.smartfridge.dtos.ItemRecordDto;
import com.example.smartfridge.entities.GroceryItem;
import com.example.smartfridge.entities.GroceryList;
import com.example.smartfridge.entities.ItemRecord;
import com.example.smartfridge.entities.User;
import com.example.smartfridge.mappers.FridgeMapper;
import com.example.smartfridge.mappers.GroceryItemMapper;
import com.example.smartfridge.mappers.GroceryListMapper;
import com.example.smartfridge.repositories.GroceryItemRepository;
import com.example.smartfridge.repositories.GroceryListRepository;
import com.example.smartfridge.repositories.ItemRepository;
import com.example.smartfridge.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GroceryService {
    private final ItemRepository itemRecordRepository;
    private final GroceryItemRepository groceryItemRepository;
    private final GroceryListRepository groceryListRepository;
    private final UserRepository userRepository;
    private final FridgeMapper fridgeMapper;
    private final GroceryListMapper groceryListMapper;
    private final GroceryItemMapper groceryItemMapper;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return userRepository.findUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public FridgeDto checkCurrentUserFridge() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return fridgeMapper.toFridgeDto(user.getFridgeId());
    }

    public List<GroceryListDto> allLists() {
        Long userFridgeId = fridgeMapper.toFridge(checkCurrentUserFridge()).getId();
        if (userFridgeId != 0) {
            return groceryListMapper.toGroceryListDtoList(groceryListRepository.findAllByFridgeId(userFridgeId));
        }
        return null;
    }

    public GroceryListDto getListById(Long id) {
        Long userFridgeId = fridgeMapper.toFridge(checkCurrentUserFridge()).getId();
        GroceryList groceryList = groceryListRepository.findById(id).orElse(null);
        if (groceryList != null && Objects.equals(groceryList.getFridge().getId(), userFridgeId)) {
            return groceryListMapper.toGroceryListDto(groceryList);
        }
        return null;
    }

    public List<GroceryItemDto> getItemsByListId(Long id) {
        Long userFridgeId = fridgeMapper.toFridge(checkCurrentUserFridge()).getId();
        GroceryList groceryList = groceryListRepository.findById(id).orElse(null);
        if (groceryList != null && Objects.equals(groceryList.getFridge().getId(), userFridgeId)) {
            return groceryItemMapper.toGroceryItemDtoList(groceryList.getItems());
        }
        return null;
    }

    public GroceryListDto createList(String groceryListName, String description) {
        User user = getCurrentUser();
        if (user.getFridgeId().getId() != 0) {
            GroceryList groceryList = new GroceryList();
            groceryList.setName(groceryListName);
            groceryList.setDescription(description);
            groceryList.setFridge(user.getFridgeId());
            groceryList.setUser(user);
            groceryListRepository.save(groceryList);
            return groceryListMapper.toGroceryListDto(groceryList);
        }
        return null;
    }

    public GroceryListDto updateList(Long id, String groceryListName, String description) {
        GroceryList groceryList = groceryListRepository.findById(id).orElse(null);
        if (groceryList != null) {
            groceryList.setName(groceryListName);
            groceryList.setDescription(description);
            groceryListRepository.save(groceryList);
            return groceryListMapper.toGroceryListDto(groceryList);
        }
        return null;
    }

    public GroceryListDto deleteList(Long id) {
        User user = getCurrentUser();
        User fridgeOwner = userRepository.findById(user.getFridgeId().getId()).orElse(null);
        GroceryList groceryList = groceryListRepository.findById(id).orElse(null);
        if (groceryList != null && (groceryList.getUser().getId().equals(user.getId())
                || groceryList.getUser().getId().equals(fridgeOwner.getId()))) {
            groceryListRepository.delete(groceryList);
            return groceryListMapper.toGroceryListDto(groceryList);
        }
        return null;
    }

    public GroceryItemDto addItemToList(GroceryItemDto groceryItemDto, Long listId) {
        GroceryItem groceryItem = groceryItemMapper.toGroceryItem(groceryItemDto);
        User user = getCurrentUser();
        GroceryList groceryList = groceryListRepository.findById(listId).orElse(null);
        if (groceryList != null && Objects.equals(user.getFridgeId().getId(), groceryList.getFridge().getId())) {
            groceryItem.setGroceryList(groceryList);
            groceryItemRepository.save(groceryItem);
            return groceryItemMapper.toGroceryItemDto(groceryItem);
        }
        return null;
    }

    public GroceryItemDto deleteItem(Long id) {
        GroceryItem groceryItem = groceryItemRepository.findById(id).orElse(null);
        User user = getCurrentUser();
        User fridgeOwner = userRepository.findById(user.getFridgeId().getId()).orElse(null);
        if (groceryItem != null && (groceryItem.getGroceryList().getUser().getId().equals(user.getId())
                || groceryItem.getGroceryList().getUser().getId().equals(fridgeOwner.getId()))) {
            groceryItemRepository.delete(groceryItem);
            return groceryItemMapper.toGroceryItemDto(groceryItem);
        }
        return null;
    }

    public GroceryItemDto updateItem(GroceryItemDto groceryItemDto) {
        User user = getCurrentUser();
        User fridgeOwner = userRepository.findById(user.getFridgeId().getId()).orElse(null);
        GroceryItem groceryItem = groceryItemRepository.findById(groceryItemDto.getId()).orElse(null);
        if (groceryItem != null && (groceryItem.getGroceryList().getUser().getId().equals(user.getId())
                || groceryItem.getGroceryList().getUser().getId().equals(fridgeOwner.getId()))) {
            groceryItem = groceryItemMapper.toGroceryItem(groceryItemDto);
            groceryItemRepository.save(groceryItem);
            return groceryItemMapper.toGroceryItemDto(groceryItem);
        }
        return null;
    }

    public GroceryItemDto toggleItemPurchased(Long itemId, boolean purchased) {
        User user = getCurrentUser();
        GroceryItem groceryItem = groceryItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Grocery item not found with id: " + itemId));

        if (groceryItem.getGroceryList() == null ||
                !Objects.equals(groceryItem.getGroceryList().getFridge().getId(), user.getFridgeId().getId())) {
            throw new SecurityException("User does not have permission to modify this item.");
        }

        groceryItem.setPurchased(purchased);
        groceryItemRepository.save(groceryItem);
        return groceryItemMapper.toGroceryItemDto(groceryItem);
    }

    @Transactional
    public void movePurchasedItemsToFridge(Long listId, List<ItemRecordDto> purchasedItems) {
        User user = getCurrentUser();
        GroceryList groceryList = groceryListRepository.findById(listId)
                .orElseThrow(() -> new RuntimeException("Grocery list not found with id: " + listId));

        if (!Objects.equals(groceryList.getFridge().getId(), user.getFridgeId().getId())) {
            throw new SecurityException("User does not have permission to modify this list.");
        }

        List<ItemRecord> fridgeItemsToAdd = new ArrayList<>();
        for (ItemRecordDto purchasedItem : purchasedItems) {
            toggleItemPurchased(purchasedItem.getId(), false);
            ItemRecord fridgeItem = new ItemRecord();
            fridgeItem.setName(purchasedItem.getName());
            fridgeItem.setDescription(purchasedItem.getDescription());
            fridgeItem.setQuantity(purchasedItem.getQuantity());
            fridgeItem.setCategory(purchasedItem.getCategory());
            fridgeItem.setPrice(purchasedItem.getPrice());
            fridgeItem.setExpirationDate(purchasedItem.getExpirationDate());
            fridgeItem.setBuyingDate(LocalDate.now());
            fridgeItem.setFridgeId(groceryList.getFridge());
            fridgeItemsToAdd.add(fridgeItem);
        }

        itemRecordRepository.saveAll(fridgeItemsToAdd);
    }

}
