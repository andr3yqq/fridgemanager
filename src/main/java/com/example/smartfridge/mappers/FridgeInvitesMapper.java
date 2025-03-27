package com.example.smartfridge.mappers;

import com.example.smartfridge.dtos.FridgeInvitesDto;
import com.example.smartfridge.entities.Fridge;
import com.example.smartfridge.entities.FridgeInvites;
import com.example.smartfridge.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface FridgeInvitesMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "invitedUser.id", target = "invitedId")
    @Mapping(source = "fridgeId.id", target = "fridgeId")
    FridgeInvitesDto toDto(FridgeInvites fridgeInvites);

    @Mapping(source = "userId", target = "user", qualifiedByName = "userIdToUser")
    @Mapping(source = "invitedId", target = "invitedUser", qualifiedByName = "userIdToUser")
    @Mapping(source = "fridgeId", target = "fridgeId", qualifiedByName = "fridgeIdToFridge")
    FridgeInvites toEntity(FridgeInvitesDto fridgeInvitesDto);

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