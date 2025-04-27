package com.example.smartfridge.mappers;

import com.example.smartfridge.dtos.GroceryItemDto;
import com.example.smartfridge.entities.GroceryItem;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GroceryItemMapper {
    GroceryItemDto toGroceryItemDto(GroceryItem groceryItem);

    List<GroceryItemDto> toGroceryItemDtoList(List<GroceryItem> groceryItems);

    GroceryItem toGroceryItem(GroceryItemDto groceryItemDto);
}
