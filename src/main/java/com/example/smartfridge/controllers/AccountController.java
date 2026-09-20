package com.example.smartfridge.controllers;

import com.example.smartfridge.dtos.ChangePasswordRequestDto;
import com.example.smartfridge.dtos.UserResponseDto;
import com.example.smartfridge.services.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/me")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public UserResponseDto me() {
        return accountService.getCurrentUser();
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequestDto request) {
        accountService.changePassword(request);
        return ResponseEntity.noContent().build();
    }
}