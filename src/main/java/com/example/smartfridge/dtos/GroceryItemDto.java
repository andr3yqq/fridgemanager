package com.example.smartfridge.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class GroceryItemDto {
    private Long id;
    private String name;
    private String description;
    private Integer quantity;
    private String category;
    private boolean purchased;
}
