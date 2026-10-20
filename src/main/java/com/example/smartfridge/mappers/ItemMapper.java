package com.example.smartfridge.mappers;

import com.example.smartfridge.dtos.ItemRecordDto;
import com.example.smartfridge.entities.Fridge;
import com.example.smartfridge.entities.ItemRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    @Mapping(source = "fridge.id", target = "fridgeId")
    ItemRecordDto toItemRecordDto(ItemRecord itemRecord);

    @Mapping(source = "fridgeId", target = "fridge", qualifiedByName = "fridgeIdToFridge")
    ItemRecord toItemRecord(ItemRecordDto itemRecordDto);

    List<ItemRecordDto> toItemRecordDtoList(List<ItemRecord> itemRecords);

    @Named("fridgeIdToFridge")
    default Fridge fridgeIdToFridge(Long id) {
        if (id == null) {
            return null;
        }
        Fridge fridge = new Fridge();
        fridge.setId(id);
        return fridge;
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fridge", ignore = true)
    void updateItemRecordFromDto(ItemRecordDto itemRecordDto, @MappingTarget ItemRecord itemRecord);

}
