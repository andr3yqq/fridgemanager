package com.example.smartfridge.services;

import com.example.smartfridge.dtos.UserDto;
import com.example.smartfridge.entities.User;
import com.example.smartfridge.exceptions.UserAlreadyExistsException;
import com.example.smartfridge.mappers.UserMapper;
import com.example.smartfridge.repositories.UserRepository;
import com.example.smartfridge.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserUtils userUtils;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserDetails loadUserByUsername(String username) {
        return userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public User authenticate(String username, String rawPassword) {
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        return user;
    }

    @Transactional
    public User changePassword(String oldPassword, String newPassword) {
        User user = userUtils.getUserFromAuthentication();
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

    @Transactional
    public User registerUser(UserDto userDto) {
        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new UserAlreadyExistsException("Username is already taken.");
        }
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new UserAlreadyExistsException("Email is already taken.");
        }

        User newUser = userMapper.toUser(userDto);
        newUser.setPassword(passwordEncoder.encode(userDto.getPassword()));

        try {
            return userRepository.save(newUser);
        } catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistsException("Username or email is already taken.");
        }
    }

}
