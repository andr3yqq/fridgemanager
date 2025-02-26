package com.example.smartfridge.mappers;

import com.example.smartfridge.dtos.ItemRecordDto;
import com.example.smartfridge.entities.ItemRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    ItemRecord toItemRecord(ItemRecordDto itemRecordDto);
    ItemRecordDto toItemRecordDto(ItemRecord itemRecord);

    List<ItemRecordDto> toItemRecordDtoList(List<ItemRecord> itemRecords);

    @Mapping(target = "id", ignore = true)
    void updateItemRecord(ItemRecordDto itemRecordDto, @MappingTarget ItemRecord itemRecord);

}
