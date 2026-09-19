package com.example.smartfridge.services;

import com.example.smartfridge.dtos.GroceryItemDto;
import com.example.smartfridge.dtos.GroceryListDto;
import com.example.smartfridge.dtos.ItemRecordDto;
import com.example.smartfridge.entities.*;
import com.example.smartfridge.exceptions.*;
import com.example.smartfridge.mappers.GroceryItemMapper;
import com.example.smartfridge.mappers.GroceryListMapper;
import com.example.smartfridge.mappers.ItemMapper;
import com.example.smartfridge.repositories.GroceryItemRepository;
import com.example.smartfridge.repositories.GroceryListRepository;
import com.example.smartfridge.repositories.ItemRepository;
import com.example.smartfridge.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ItemMapper itemRecordMapper;
    private final GroceryListMapper groceryListMapper;
    private final GroceryItemMapper groceryItemMapper;
    private final UserUtils userUtils;

    private void validateUserFridgeMembership(Long fridgeId) {
        Fridge userFridge = userUtils.getCurrentUserFridge();
        if (userFridge == null) {
            throw new UserDoesNotHaveFridgeException("User does not have a fridge");
        }
        Long userFridgeId = userFridge.getId();
        if (!Objects.equals(fridgeId, userFridgeId)) {
            throw new UserDoesNotHaveAccessToFridgeException("User does not have access to fridge");
        }
    }

    private GroceryList validateListOwnership(Long listId) {
        User user = userUtils.getUserFromAuthentication();
        GroceryList groceryList = groceryListRepository.findById(listId).orElseThrow(() -> new GroceryListNotFoundException("Grocery list not found"));
        boolean isCreator = Objects.equals(user.getId(), groceryList.getUser().getId());
        boolean isFridgeOwner = Objects.equals(user.getId(), groceryList.getFridge().getOwner().getId());
        if (!(isCreator || isFridgeOwner)) {
            throw new UserIsNotOwnerOfGroceryListException("User is not the owner of the grocery list");
        }
        return groceryList;
    }

    public List<GroceryListDto> allLists() {
        Fridge userFridge = userUtils.getCurrentUserFridge();
        if (userFridge == null) {
            throw new UserDoesNotHaveFridgeException("User does not have a fridge");
        }
        return groceryListMapper.toGroceryListDtoList(groceryListRepository.findAllByFridgeId(userFridge.getId()));
    }

    @Transactional(readOnly = true)
    public GroceryListDto getListById(Long id) {
        GroceryList groceryList = groceryListRepository.findById(id).orElseThrow(() -> new GroceryListNotFoundException("Grocery list not found"));
        validateUserFridgeMembership(groceryList.getFridge().getId());
        return groceryListMapper.toGroceryListDto(groceryList);
    }

    @Transactional(readOnly = true)
    public List<GroceryItemDto> getItemsByListId(Long id) {
        GroceryList groceryList = groceryListRepository.findById(id).orElseThrow(() -> new GroceryListNotFoundException("Grocery list not found"));
        validateUserFridgeMembership(groceryList.getFridge().getId());
        return groceryItemMapper.toGroceryItemDtoList(groceryList.getItems());
    }

    @Transactional
    public GroceryListDto createList(String groceryListName, String description) {
        User user = userUtils.getUserFromAuthentication();
        if (user.getFridge() == null) {
            throw new UserDoesNotHaveFridgeException("User does not have a fridge");
        }
        GroceryList groceryList = new GroceryList();
        groceryList.setName(groceryListName);
        groceryList.setDescription(description);
        groceryList.setFridge(user.getFridge());
        groceryList.setUser(user);
        return groceryListMapper.toGroceryListDto(groceryListRepository.save(groceryList));
    }

    @Transactional
    public GroceryListDto updateList(Long id, String groceryListName, String description) {
        GroceryList groceryList = validateListOwnership(id);
        groceryList.setName(groceryListName);
        groceryList.setDescription(description);
        return groceryListMapper.toGroceryListDto(groceryListRepository.save(groceryList));
    }

    public void deleteList(Long id) {
        GroceryList groceryList = validateListOwnership(id);
        groceryListRepository.delete(groceryList);
    }

    @Transactional
    public GroceryItemDto addItemToList(GroceryItemDto groceryItemDto, Long listId) {
        GroceryItem groceryItem = groceryItemMapper.toGroceryItem(groceryItemDto);
        GroceryList groceryList = groceryListRepository.findById(listId).orElseThrow(() -> new GroceryListNotFoundException("Grocery list not found"));
        validateUserFridgeMembership(groceryList.getFridge().getId());
        groceryItem.setGroceryList(groceryList);
        return groceryItemMapper.toGroceryItemDto(groceryItemRepository.save(groceryItem));
    }

    public void deleteItem(Long id) {
        GroceryItem groceryItem = groceryItemRepository.findById(id).orElseThrow(() -> new GroceryItemNotFoundException("Grocery item not found"));
        validateListOwnership(groceryItem.getGroceryList().getId());
        groceryItemRepository.delete(groceryItem);
    }

    @Transactional
    public void updateItem(GroceryItemDto groceryItemDto) {
        GroceryItem groceryItem = groceryItemRepository.findById(groceryItemDto.getId()).orElseThrow(() -> new GroceryItemNotFoundException("Grocery item not found"));
        validateListOwnership(groceryItem.getGroceryList().getId());
        groceryItemMapper.updateGroceryItemFromDto(groceryItemDto, groceryItem);
        groceryItemRepository.save(groceryItem);
    }

    @Transactional
    public GroceryItemDto toggleItemPurchased(Long itemId, boolean purchased) {
        GroceryItem groceryItem = groceryItemRepository.findById(itemId)
                .orElseThrow(() -> new GroceryItemNotFoundException("Grocery item not found with id: " + itemId));
        validateUserFridgeMembership(groceryItem.getGroceryList().getFridge().getId());
        groceryItem.setPurchased(purchased);
        return groceryItemMapper.toGroceryItemDto(groceryItemRepository.save(groceryItem));
    }

    //copies purchased items to fridge and marks them as not purchased for future list use
    @Transactional
    public List<ItemRecordDto> copyPurchasedItemsToFridge(Long listId, List<ItemRecordDto> purchasedItems) {
        GroceryList groceryList = groceryListRepository.findById(listId)
                .orElseThrow(() -> new GroceryListNotFoundException("Grocery list not found with id: " + listId));
        validateUserFridgeMembership(groceryList.getFridge().getId());
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
            fridgeItem.setFridge(groceryList.getFridge());
            fridgeItemsToAdd.add(fridgeItem);
        }
        return itemRecordMapper.toItemRecordDtoList(itemRecordRepository.saveAll(fridgeItemsToAdd));
    }

}
