package com.example.smartfridge.services;

import com.example.smartfridge.dtos.UserLogRecordDto;
import com.example.smartfridge.entities.Fridge;
import com.example.smartfridge.entities.User;
import com.example.smartfridge.entities.UserLogRecord;
import com.example.smartfridge.mappers.UserLogMapper;
import com.example.smartfridge.repositories.FridgeRepository;
import com.example.smartfridge.repositories.UserLogsRepository;
import com.example.smartfridge.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LogsService {
    private final UserRepository userRepository;
    private final FridgeRepository fridgeRepository;
    private final UserLogsRepository userLogsRepository;
    private final UserLogMapper userLogMapper;

    private User checkCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return userRepository.findUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<UserLogRecordDto> allUserLogs() {
        User user = checkCurrentUser();
        return userLogMapper.toUserLogRecordDtoList(userLogsRepository.findAllByUser(user));
    }

    public UserLogRecordDto getItemById(Long id) {
        UserLogRecord userLogRecord = userLogsRepository.findById(id).orElse(null);
        return userLogMapper.toUserLogRecordDto(userLogRecord);
    }

    public UserLogRecordDto createItem(UserLogRecordDto userLogRecordDto) {
        User user = checkCurrentUser();
        UserLogRecord userLogRecord = userLogMapper.toUserLogRecord(userLogRecordDto);
        Fridge fridge = fridgeRepository.findById(user.getFridgeId().getId()).orElse(null);
        userLogRecord.setUser(user);
        userLogRecord.setFridgeId(fridge);
        userLogsRepository.save(userLogRecord);
        return userLogMapper.toUserLogRecordDto(userLogRecord);
    }

    public List<UserLogRecordDto> getLogsByFridge() {
        User user = checkCurrentUser();
        Fridge fridge = fridgeRepository.findById(user.getFridgeId().getId()).orElse(null);
        List<UserLogRecord> userLogs = userLogsRepository.findAllByFridgeId(fridge);
        userLogs.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        if (fridge != null) {
            return userLogMapper.toUserLogRecordDtoList(userLogs);
        }
        return null;
    }
}
