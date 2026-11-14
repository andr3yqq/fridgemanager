package com.example.smartfridge.mappers;

import com.example.smartfridge.dtos.AuthResponseDto;
import com.example.smartfridge.dtos.UserDto;
import com.example.smartfridge.dtos.UserResponseDto;
import com.example.smartfridge.entities.Fridge;
import com.example.smartfridge.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "fridge.id", target = "fridgeId")
    UserDto toUserDto(User user);

    @Mapping(source = "fridgeId", target = "fridge", qualifiedByName = "fridgeIdToFridge")
    User toUser(UserDto userDto);

    @Mapping(source = "fridgeId", target = "fridge", qualifiedByName = "fridgeIdToFridge")
    User updateEntityFromDto(UserDto userDto, @MappingTarget User user);

    @Mapping(source = "fridge", target = "fridgeId", qualifiedByName = "fridgeToFridgeId")
    List<UserDto> toUserDtoList(List<User> users);

    @Named("fridgeIdToFridge")
    default Fridge fridgeIdToFridge(Long id) {
        if (id == null) {
            return null;
        }
        Fridge fridge = new Fridge();
        fridge.setId(id);
        return fridge;
    }

    @Named("fridgeToFridgeId")
    default Long fridgeToFridgeId(Fridge fridge) {
        if (fridge == null) {
            return null;
        }
        return fridge.getId();
    }

    @Mapping(source = "fridge", target = "fridgeId", qualifiedByName = "fridgeToFridgeId")
    UserResponseDto toUserResponseDto(User user);

    @Mapping(source = "fridge", target = "fridgeId", qualifiedByName = "fridgeToFridgeId")
    default AuthResponseDto toAuthResponseDto(User user, String token) {
        return new AuthResponseDto(toUserResponseDto(user), token);
    }
}
