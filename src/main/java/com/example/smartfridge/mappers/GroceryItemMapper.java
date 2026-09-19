package com.example.smartfridge.mappers;

import com.example.smartfridge.dtos.GroceryItemDto;
import com.example.smartfridge.entities.GroceryItem;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GroceryItemMapper {
    GroceryItemDto toGroceryItemDto(GroceryItem groceryItem);

    List<GroceryItemDto> toGroceryItemDtoList(List<GroceryItem> groceryItems);

    GroceryItem toGroceryItem(GroceryItemDto groceryItemDto);

    void updateGroceryItemFromDto(GroceryItemDto groceryItemDto, @MappingTarget GroceryItem groceryItem);
}
