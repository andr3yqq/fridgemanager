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

public class ItemRecordDto {
    private Long id;
    private String name;
    private String description;
    private Integer quantity;
    private String category;
    private Double price;
    private LocalDate expirationDate;
    private LocalDate buyingDate;
    private Long fridgeId;
}
