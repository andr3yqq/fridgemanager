package com.example.smartfridge.dtos;

public record ItemChangeDto(
        String author,
        String changedAt,
        String field,
        String oldValue,
        String newValue) {
}
