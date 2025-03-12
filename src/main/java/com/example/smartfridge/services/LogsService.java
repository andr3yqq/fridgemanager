package com.example.smartfridge.services;

import com.example.smartfridge.dtos.UserLogRecordDto;
import com.example.smartfridge.entities.UserLogRecord;
import com.example.smartfridge.mappers.UserLogMapper;
import com.example.smartfridge.repositories.UserLogsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LogsService {
    private final UserLogsRepository userLogsRepository;
    private final UserLogMapper userLogMapper;

    public List<UserLogRecordDto> allUserLogs() { return userLogMapper.toUserLogRecordDtoList(userLogsRepository.findAll()); }

    public UserLogRecordDto getItemById(Long id) {
        UserLogRecord userLogRecord = userLogsRepository.findById(id).orElse(null);
        return userLogMapper.toUserLogRecordDto(userLogRecord);
    }

    public UserLogRecordDto createItem(UserLogRecordDto userLogRecordDto) {
        UserLogRecord userLogRecord = userLogMapper.toUserLogRecord(userLogRecordDto);
        userLogsRepository.save(userLogRecord);
        return userLogMapper.toUserLogRecordDto(userLogRecord);

    }
}
