package com.example.smartfridge.services;

import com.example.smartfridge.dtos.ItemRecordDto;
import com.example.smartfridge.entities.ItemRecord;
import com.example.smartfridge.mappers.ItemMapper;
import com.example.smartfridge.repositories.FridgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FridgeService {
    private final FridgeRepository fridgeRepository;
    private final ItemMapper itemMapper;

    public List<ItemRecordDto> allItems() {
        return itemMapper.toItemRecordDtoList(fridgeRepository.findAll());
    }

    public ItemRecordDto getItemById(Long id) {
        ItemRecord itemRecord = fridgeRepository.findById(id).orElse(null);
        return itemMapper.toItemRecordDto(itemRecord);
    }

    public ItemRecordDto createItem(ItemRecordDto itemRecordDto) {
        ItemRecord itemRecord = itemMapper.toItemRecord(itemRecordDto);
        fridgeRepository.save(itemRecord);
        return itemMapper.toItemRecordDto(itemRecord);

    }



}
