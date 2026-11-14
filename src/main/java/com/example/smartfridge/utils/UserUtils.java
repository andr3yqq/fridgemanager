package com.example.smartfridge.utils;

import com.example.smartfridge.entities.Fridge;
import com.example.smartfridge.entities.User;
import com.example.smartfridge.exceptions.UserDoesNotHaveFridgeException;
import com.example.smartfridge.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserUtils {

    private final UserRepository userRepository;

    public User getUserFromAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public Fridge getCurrentUserFridge() {
        User user = getUserFromAuthentication();
        Fridge fridge = user.getFridge();
        if (fridge == null) {
            throw new UserDoesNotHaveFridgeException("User does not have a fridge");
        }
        return fridge;
    }

}
