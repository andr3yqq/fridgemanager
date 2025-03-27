package com.example.smartfridge.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class FridgeInvitesDto {
    private Long id;
    private Long userId;
    private String status;
    private Long invitedId;
    private Long fridgeId;
}
