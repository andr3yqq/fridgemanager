package com.example.smartfridge.mappers;

import com.example.smartfridge.dtos.GroceryListDto;
import com.example.smartfridge.entities.Fridge;
import com.example.smartfridge.entities.GroceryList;
import com.example.smartfridge.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GroceryListMapper {
    @Mapping(source = "fridge.id", target = "fridgeId")
    @Mapping(source = "user.id", target = "userId")
    GroceryListDto toGroceryListDto(GroceryList groceryList);

    List<GroceryListDto> toGroceryListDtoList(List<GroceryList> groceryLists);

    @Mapping(source = "fridgeId", target = "fridge", qualifiedByName = "fridgeIdToFridge")
    @Mapping(source = "userId", target = "user", qualifiedByName = "userIdToUser")
    GroceryList toGroceryList(GroceryListDto groceryListDto);

    @Named("userIdToUser")
    default User userIdToUser(Long id) {
        if (id == null) {
            return null;
        }
        User user = new User();
        user.setId(id);
        return user;
    }

    @Named("fridgeIdToFridge")
    default Fridge fridgeIdToFridge(Long id) {
        if (id == null) {
            return null;
        }
        Fridge fridge = new Fridge();
        fridge.setId(id);
        return fridge;
    }
}
