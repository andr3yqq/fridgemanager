package com.example.smartfridge.mappers;

import com.example.smartfridge.dtos.UserLogRecordDto;
import com.example.smartfridge.entities.UserLogRecord;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserLogMapper {
     UserLogRecord toUserLogRecord(UserLogRecordDto userLogRecordDto);
     UserLogRecordDto toUserLogRecordDto(UserLogRecord userLogRecord);

     List<UserLogRecordDto> toUserLogRecordDtoList(List<UserLogRecord> userLogRecords);
}
