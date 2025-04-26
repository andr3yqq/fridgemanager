package com.example.smartfridge.mappers;

import com.example.smartfridge.dtos.UserLogRecordDto;
import com.example.smartfridge.entities.Fridge;
import com.example.smartfridge.entities.User;
import com.example.smartfridge.entities.UserLogRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserLogMapper {
     @Mapping(source = "user.id",target = "userId")
     @Mapping(source = "fridgeId.id", target = "fridgeId")
     @Mapping(source = "user.username", target = "username")
     UserLogRecordDto toUserLogRecordDto(UserLogRecord userLogRecord);
     @Mapping(source = "userId",target = "user",qualifiedByName = "userIdToUser")
     @Mapping(source = "fridgeId", target = "fridgeId", qualifiedByName = "fridgeIdToFridge")
     UserLogRecord toUserLogRecord(UserLogRecordDto userLogRecordDto);

     List<UserLogRecordDto> toUserLogRecordDtoList(List<UserLogRecord> userLogRecords);

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
