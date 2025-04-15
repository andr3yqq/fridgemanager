package com.example.smartfridge.services;

import com.example.smartfridge.dtos.FridgeDto;
import com.example.smartfridge.dtos.FridgeInvitesDto;
import com.example.smartfridge.dtos.ItemRecordDto;
import com.example.smartfridge.dtos.UserDto;
import com.example.smartfridge.entities.Fridge;
import com.example.smartfridge.entities.FridgeInvites;
import com.example.smartfridge.entities.ItemRecord;
import com.example.smartfridge.mappers.FridgeInvitesMapper;
import com.example.smartfridge.mappers.FridgeMapper;
import com.example.smartfridge.mappers.ItemMapper;
import com.example.smartfridge.mappers.UserMapper;
import com.example.smartfridge.repositories.FridgeInvitesRepository;
import com.example.smartfridge.repositories.FridgeRepository;
import com.example.smartfridge.repositories.ItemRepository;
import com.example.smartfridge.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.smartfridge.entities.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FridgeService {
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final FridgeMapper fridgeMapper;
    private final FridgeInvitesMapper fridgeInvitesMapper;
    private final UserRepository userRepository;
    private final FridgeRepository fridgeRepository;
    private final FridgeInvitesRepository fridgeInvitesRepository;
    private final UserMapper userMapper;

    public List<ItemRecordDto> allItems() {
        return itemMapper.toItemRecordDtoList(itemRepository.findAll());
    }

    public FridgeDto checkCurrentUserFridge() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return fridgeMapper.toFridgeDto(user.getFridgeId());
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return userRepository.findUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<ItemRecordDto> allItemsByFridge() {
        Fridge fridgeId = fridgeMapper.toFridge(checkCurrentUserFridge());
        return itemMapper.toItemRecordDtoList(itemRepository.findAllByFridgeId(fridgeId));
    }

    public ItemRecordDto getItemById(Long id) {

        ItemRecord itemRecord = itemRepository.findById(id).orElse(null);
        return itemMapper.toItemRecordDto(itemRecord);
    }

    public ItemRecordDto createItem(ItemRecordDto itemRecordDto) {
        ItemRecord itemRecord = itemMapper.toItemRecord(itemRecordDto);
        Fridge fridgeId = fridgeMapper.toFridge(checkCurrentUserFridge());
        itemRecord.setFridgeId(fridgeId);
        itemRepository.save(itemRecord);
        return itemMapper.toItemRecordDto(itemRecord);

    }

    public ItemRecordDto updateItem(ItemRecordDto itemRecordDto) {
        Fridge fridgeId = fridgeMapper.toFridge(checkCurrentUserFridge());
        ItemRecord itemRecord = itemRepository.findById(itemRecordDto.getId()).orElse(null);
        if (itemRecord != null && fridgeId.equals(itemRecord.getFridgeId())) {
            itemRecord = itemMapper.toItemRecord(itemRecordDto);
            itemRepository.save(itemRecord);
            return itemMapper.toItemRecordDto(itemRecord);
        }
        return null;
    }

    public ItemRecordDto deleteItem(Long id) {
        Fridge fridgeId = fridgeMapper.toFridge(checkCurrentUserFridge());
        ItemRecord itemRecord = itemRepository.findById(id).orElse(null);
        if (itemRecord != null && fridgeId.equals(itemRecord.getFridgeId())) {
            itemRepository.delete(itemRecord);
            return itemMapper.toItemRecordDto(itemRecord);
        }
        return null;
    }

    public FridgeDto createFridge(String fridgeName)
    {
        User user = getCurrentUser();

        if (user.getFridgeId().getId() == 0)
        {
            Fridge fridge = new Fridge();
            fridge.setName(fridgeName);
            fridge.setOwner(user);
            fridgeRepository.save(fridge);
            user.setFridgeId(fridge);
            userRepository.save(user);
            return fridgeMapper.toFridgeDto(fridge);
        }
        return null;
    }

    public FridgeDto deleteFridge() {
        Fridge fridge = fridgeMapper.toFridge(checkCurrentUserFridge());
        User user = getCurrentUser();
        if (fridge != null && user.getId().equals(fridge.getOwner().getId())) {
            fridgeRepository.delete(fridge);
            Fridge basicFridge = new Fridge();
            basicFridge.setId(0L);
            user.setFridgeId(basicFridge);
            userRepository.save(user);
            return fridgeMapper.toFridgeDto(fridge);
        }
        return null;
    }

    public FridgeInvitesDto inviteUser(String username) {
        System.out.println(username);
        User currentUser = getCurrentUser();
        User invitedUser = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (currentUser.getFridgeId().getId() != 0 && invitedUser.getFridgeId().getId() == 0)
        {
            FridgeInvites invite = new FridgeInvites();
            invite.setFridgeId(currentUser.getFridgeId());
            invite.setUser(currentUser);
            invite.setInvitedUser(invitedUser);
            invite.setStatus("invited");
            fridgeInvitesRepository.save(invite);
            return fridgeInvitesMapper.toDto(invite);
        }
        return null;
    }

    public List<FridgeInvitesDto> getInvitesForUser() {
        User currentUser = getCurrentUser();
        return fridgeInvitesMapper.toDtoList(fridgeInvitesRepository.findAllByInvitedUser(currentUser)
                .stream().filter(e -> e.getStatus().equals("invited")).collect(Collectors.toList()));
    }

    public UserDto leaveFridge() {
        User user = getCurrentUser();
        if (user.getFridgeId().getId() != 0) {
            Fridge fridge = fridgeRepository.findById(user.getFridgeId().getId()).orElse(null);
            if (fridge != null && !fridge.getOwner().getId().equals(user.getId())) {
                Fridge basicFridge = new Fridge();
                basicFridge.setId(0L);
                user.setFridgeId(basicFridge);
                userRepository.save(user);
                return userMapper.toUserDto(user);
            }
            return null;
        }
        return null;
    }

    public UserDto joinFridge(Long inviteId) {
        User user = getCurrentUser();
        FridgeInvites invite = fridgeInvitesRepository.findById(inviteId).orElse(null);
        if (invite != null && user.getFridgeId().getId() == 0) {
            Fridge newFridge = new Fridge();
            newFridge.setId(invite.getFridgeId().getId());
            user.setFridgeId(newFridge);
            invite.setStatus("accepted");
            fridgeInvitesRepository.save(invite);
            userRepository.save(user);
            return userMapper.toUserDto(user);
        }
        return null;
    }

    public String getInviteUsername(Long id) {
        FridgeInvites invite = fridgeInvitesRepository.findById(id).orElse(null);
        if (invite != null) {
            return invite.getInvitedUser().getUsername();
        }
        return null;
    }

    public String getInviteFridgeName(Long id) {
        FridgeInvites invite = fridgeInvitesRepository.findById(id).orElse(null);
        if (invite != null) {
            return invite.getFridgeId().getName();
        }
        return null;
    }



}
