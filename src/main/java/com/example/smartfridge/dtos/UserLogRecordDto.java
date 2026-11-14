package com.example.smartfridge.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserLogRecordDto {
    private Long id;

    private Instant timestamp;

    private String action;

    private String entityType;

    private Long entityId;

    private String entityName;

    private String field;

    private String oldValue;

    private String newValue;

    private Map<String, String> details;

    private Long userId;

    private Long fridgeId;

    private String username;
}
