package com.example.smartfridge.controllers;

import com.example.smartfridge.dtos.UserLogRecordDto;
import com.example.smartfridge.services.LogsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api")
public class UserLogController {
    private final LogsService logsService;

    @GetMapping("/logs")
    public ResponseEntity<List<UserLogRecordDto>> allFridgeLogs() {
        return ResponseEntity.ok(logsService.getLogsByFridge());
    }
    //public ResponseEntity<List<UserLogRecordDto>> allUserLogs() { return ResponseEntity.ok(logsService.allUserLogs()); }

    @PostMapping("/logs")
    public ResponseEntity<UserLogRecordDto> createUserLog(@RequestBody UserLogRecordDto userLogRecordDto) {
        UserLogRecordDto createdLog = logsService.createItem(userLogRecordDto);
        return ResponseEntity.ok(createdLog);
    }
}
