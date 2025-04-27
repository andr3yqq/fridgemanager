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

public class GroceryListDto {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private Long fridgeId;
    private Long userId;
}
