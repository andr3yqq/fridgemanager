package com.example.smartfridge.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserLogRecordDto {
    private Long id;

    private LocalDate timestamp;

    private String action;

    private String itemName;

    private String details;
}
