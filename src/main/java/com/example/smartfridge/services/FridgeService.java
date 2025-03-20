package com.example.smartfridge.services;

import com.example.smartfridge.dtos.ItemRecordDto;
import com.example.smartfridge.entities.Fridge;
import com.example.smartfridge.entities.ItemRecord;
import com.example.smartfridge.mappers.ItemMapper;
import com.example.smartfridge.repositories.FridgeRepository;
import com.example.smartfridge.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.smartfridge.entities.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FridgeService {
    private final FridgeRepository fridgeRepository;
    private final ItemMapper itemMapper;
    private final UserRepository userRepository;

    public List<ItemRecordDto> allItems() {
        return itemMapper.toItemRecordDtoList(fridgeRepository.findAll());
    }

    private Fridge checkCurrentUserFridge() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getFridgeId();
    }

    public List<ItemRecordDto> allItemsByFridge() {
        Fridge fridgeId = checkCurrentUserFridge();
        return itemMapper.toItemRecordDtoList(fridgeRepository.findAllByFridgeId(fridgeId));
    }

    public ItemRecordDto getItemById(Long id) {

        ItemRecord itemRecord = fridgeRepository.findById(id).orElse(null);
        return itemMapper.toItemRecordDto(itemRecord);
    }

    public ItemRecordDto createItem(ItemRecordDto itemRecordDto) {
        ItemRecord itemRecord = itemMapper.toItemRecord(itemRecordDto);
        Fridge fridgeId = checkCurrentUserFridge();
        itemRecord.setFridgeId(fridgeId);
        fridgeRepository.save(itemRecord);
        return itemMapper.toItemRecordDto(itemRecord);

    }

    public ItemRecordDto updateItem(ItemRecordDto itemRecordDto) {
        Fridge fridgeId = checkCurrentUserFridge();
        ItemRecord itemRecord = fridgeRepository.findById(itemRecordDto.getId()).orElse(null);
        if (itemRecord != null && fridgeId.equals(itemRecord.getFridgeId())) {
            itemRecord = itemMapper.toItemRecord(itemRecordDto);
            fridgeRepository.save(itemRecord);
            return itemMapper.toItemRecordDto(itemRecord);
        }
        return null;
    }

    public ItemRecordDto deleteItem(Long id) {
        Fridge fridgeId = checkCurrentUserFridge();
        ItemRecord itemRecord = fridgeRepository.findById(id).orElse(null);
        if (itemRecord != null && fridgeId.equals(itemRecord.getFridgeId())) {
            fridgeRepository.delete(itemRecord);
            return itemMapper.toItemRecordDto(itemRecord);
        }
        return null;
    }





}
