package com.example.smartfridge.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserLogRecordDto {
    private Long id;

    private LocalDateTime timestamp;

    private String action;

    private String itemName;

    private String details;

    private Long userId;

    private Long fridgeId;

    private String username;
}
