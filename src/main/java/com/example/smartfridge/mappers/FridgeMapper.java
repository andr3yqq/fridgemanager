package com.example.smartfridge.mappers;

import com.example.smartfridge.dtos.FridgeDto;
import com.example.smartfridge.entities.Fridge;
import com.example.smartfridge.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FridgeMapper {

    @Mapping(source = "owner.id", target = "ownerId")
    FridgeDto toFridgeDto(Fridge fridge);
    @Mapping(source = "ownerId", target = "owner", qualifiedByName = "ownerIdToUser")
    Fridge toFridge(FridgeDto fridgeDto);
    @Mapping(source = "ownerId", target = "owner", qualifiedByName = "ownerIdToUser")
    Fridge updateEntityFromDto(FridgeDto fridgeDto, @MappingTarget Fridge fridge);

    List<FridgeDto> toFridgeDtoList(List<Fridge> fridges);

    @Named("ownerIdToUser")
    default User ownerIdToUser(Long id) {
        if (id == null) {
            return null;
        }
        User user = new User();
        user.setId(id);
        return user;
    }
}
