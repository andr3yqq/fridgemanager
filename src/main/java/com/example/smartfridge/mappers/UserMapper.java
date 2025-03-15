package com.example.smartfridge.mappers;

import com.example.smartfridge.dtos.UserDto;
import com.example.smartfridge.entities.Fridge;
import com.example.smartfridge.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "fridgeId.id", target = "fridgeId")
    UserDto toUserDto(User user);
    @Mapping(source = "fridgeId", target = "fridgeId", qualifiedByName = "fridgeIdToFridge")
    User toUser(UserDto userDto);
    @Mapping(source = "fridgeId", target = "fridgeId", qualifiedByName = "fridgeIdToFridge")
    User updateEntityFromDto(UserDto userDto, @MappingTarget User user);

    @Mapping(source = "fridgeId", target = "fridgeId", qualifiedByName = "fridgeIdToFridge")
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
}
