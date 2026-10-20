package com.example.smartfridge.services;

import com.example.smartfridge.dtos.*;
import com.example.smartfridge.entities.Fridge;
import com.example.smartfridge.entities.FridgeInvites;
import com.example.smartfridge.entities.ItemRecord;
import com.example.smartfridge.entities.User;
import com.example.smartfridge.enums.AuditAction;
import com.example.smartfridge.enums.AuditEntityType;
import com.example.smartfridge.enums.FridgeInviteStatus;
import com.example.smartfridge.exceptions.*;
import com.example.smartfridge.mappers.FridgeInvitesMapper;
import com.example.smartfridge.mappers.FridgeMapper;
import com.example.smartfridge.mappers.ItemMapper;
import com.example.smartfridge.mappers.UserMapper;
import com.example.smartfridge.repositories.FridgeInvitesRepository;
import com.example.smartfridge.repositories.FridgeRepository;
import com.example.smartfridge.repositories.ItemRepository;
import com.example.smartfridge.repositories.UserRepository;
import com.example.smartfridge.utils.DetailsKeys;
import com.example.smartfridge.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.javers.core.Javers;
import org.javers.core.commit.CommitMetadata;
import org.javers.core.diff.changetype.ValueChange;
import org.javers.repository.jql.QueryBuilder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
    private final UserUtils userUtils;
    private final FridgeActivityService fridgeActivityService;
    private final Javers javers;

    @Transactional(readOnly = true)
    public FridgeDto getCurrentUserFridgeDto() {
        return fridgeMapper.toFridgeDto(userUtils.getCurrentUserFridge());
    }

    @Transactional(readOnly = true)
    public List<ItemRecordDto> allItemsByFridge() {
        return itemMapper.toItemRecordDtoList(itemRepository.findAllByFridge(userUtils.getCurrentUserFridge()));
    }

    @Transactional(readOnly = true)
    public ItemRecordDto getItemById(Long id) {
        ItemRecord itemRecord = getItemRecordById(id);
        return itemMapper.toItemRecordDto(itemRecord);
    }

    @Transactional
    public ItemRecordDto createItem(ItemRecordDto itemRecordDto) {
        itemRecordDto.setId(null);
        ItemRecord itemRecord = itemMapper.toItemRecord(itemRecordDto);
        Fridge fridge = userUtils.getCurrentUserFridge();
        itemRecord.setFridge(fridge);
        ItemRecord saved = itemRepository.save(itemRecord);
        return itemMapper.toItemRecordDto(saved);

    }

    @Transactional
    public ItemRecordDto updateItem(Long id, ItemRecordDto itemRecordDto) {
        ItemRecord itemRecord = getItemRecordById(id);
        itemMapper.updateItemRecordFromDto(itemRecordDto, itemRecord);
        return itemMapper.toItemRecordDto(itemRepository.save(itemRecord));
    }

    @Transactional(readOnly = true)
    public List<ItemChangeDto> getItemHistory(Long id, int offset, int limit) {
        getItemRecordById(id);
        return javers.findChanges(QueryBuilder.byInstanceId(id, ItemRecord.class)
                        .skip(Math.max(0, offset))
                        .limit(Math.clamp(limit, 1, 100))
                        .build()).stream()
                .filter(ValueChange.class::isInstance)
                .map(ValueChange.class::cast)
                .map(change -> {
                    Optional<CommitMetadata> metadata = change.getCommitMetadata();
                    return new ItemChangeDto(
                            metadata.map(CommitMetadata::getAuthor).orElse("unknown"),
                            metadata.map(commit -> commit.getCommitDate().toString()).orElse(""),
                            change.getPropertyName(),
                            Objects.toString(change.getLeft(), null),
                            Objects.toString(change.getRight(), null));
                })
                .toList();
    }

    @Transactional
    public ItemRecordDto deleteItem(Long id) {
        ItemRecord itemRecord = getItemRecordById(id);
        itemRepository.delete(itemRecord);
        return itemMapper.toItemRecordDto(itemRecord);
    }

    @Transactional
    public FridgeDto createFridge(String fridgeName) {
        User user = userUtils.getUserFromAuthentication();

        if (user.getFridge() != null) {
            throw new UserAlreadyHasFridgeException("User already has a fridge");
        }
        Fridge fridge = new Fridge();
        fridge.setName(fridgeName);
        fridge.setOwner(user);
        fridgeRepository.save(fridge);
        user.setFridge(fridge);
        userRepository.save(user);
        return fridgeMapper.toFridgeDto(fridge);
    }

    @Transactional
    public void deleteFridge() {
        User user = userUtils.getUserFromAuthentication();
        Fridge fridge = userUtils.getCurrentUserFridge();
        if (!user.getId().equals(fridge.getOwner().getId())) {
            throw new UserIsNotOwnerOfFridgeException("User is not the owner of the fridge");
        }
        List<User> members = fridge.getUsers();
        members.forEach(member -> member.setFridge(null));
        userRepository.saveAll(members);
        fridgeInvitesRepository.deleteByFridge(fridge);
        fridgeRepository.delete(fridge);
    }

    @Transactional
    public FridgeInvitesDto inviteUser(String username) {
        User currentUser = userUtils.getUserFromAuthentication();
        User invitedUser = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (Objects.equals(currentUser.getId(), invitedUser.getId())) {
            throw new InviteConflictException("User cannot invite themselves");
        }
        if (currentUser.getFridge() == null) {
            throw new UserDoesNotHaveFridgeException("User does not have a fridge");
        }
        if (invitedUser.getFridge() != null) {
            throw new UserAlreadyHasFridgeException("User already has a fridge");
        }
        if (fridgeInvitesRepository.existsByFridgeAndUserAndInvitedUser(currentUser.getFridge(), currentUser, invitedUser)) {
            throw new InviteConflictException("User already has an invite");
        }
        FridgeInvites invite = new FridgeInvites();
        invite.setFridge(currentUser.getFridge());
        invite.setUser(currentUser);
        invite.setInvitedUser(invitedUser);
        invite.setStatus(FridgeInviteStatus.INVITED);
        fridgeInvitesRepository.save(invite);
        fridgeActivityService.recordUserLog(currentUser, AuditAction.USER_INVITED, AuditEntityType.USER, invitedUser.getId(),
                invitedUser.getUsername(), Map.of(DetailsKeys.INVITED_BY, currentUser.getUsername()));
        return fridgeInvitesMapper.toDto(invite);
    }

    @Transactional(readOnly = true)
    public List<InvitesResponseDto> getInvitesForUser() {
        User currentUser = userUtils.getUserFromAuthentication();
        List<FridgeInvites> invites = fridgeInvitesRepository.findAllByInvitedUser(currentUser);

        return invites.stream()
                .filter(invite -> invite.getStatus().equals(FridgeInviteStatus.INVITED))
                .map(invite -> new InvitesResponseDto(invite.getId(), invite.getUser().getUsername(), invite.getFridge().getName()))
                .toList();
    }

    @Transactional
    public UserDto leaveFridge() {
        User user = userUtils.getUserFromAuthentication();
        Fridge fridge = userUtils.getCurrentUserFridge();
        if (fridge.getOwner().getId().equals(user.getId())) {
            throw new UserIsOwnerOfThisFridgeException("User is the owner of the fridge");
        }
        user.setFridge(null);
        userRepository.save(user);
        fridgeActivityService.recordUserLog(user, AuditAction.USER_LEFT, AuditEntityType.USER, fridge, user.getId(),
                user.getUsername(), Map.of());
        return userMapper.toUserDto(user);
    }

    @Transactional
    public UserDto joinFridge(Long inviteId) {
        User user = userUtils.getUserFromAuthentication();
        FridgeInvites invite = fridgeInvitesRepository.findById(inviteId).orElseThrow(() -> new InviteNotOwnedException("Invite not found"));
        if (user.getFridge() != null) {
            throw new UserAlreadyHasFridgeException("User already has a fridge");
        }
        if (!user.getId().equals(invite.getInvitedUser().getId())) {
            throw new InviteNotOwnedException("User is not invited to the fridge");
        }
        if (!invite.getStatus().equals(FridgeInviteStatus.INVITED)) {
            throw new InviteAlreadyProcessedException("Invite is no longer valid");
        }
        Fridge newFridge = fridgeRepository.findById(invite.getFridge().getId())
                .orElseThrow(() -> new FridgeNotFoundException("Fridge not found"));
        user.setFridge(newFridge);
        invite.setStatus(FridgeInviteStatus.ACCEPTED);
        fridgeInvitesRepository.save(invite);
        userRepository.save(user);
        fridgeActivityService.recordUserLog(user, AuditAction.USER_JOINED, AuditEntityType.USER, user.getId(),
                user.getUsername(), Map.of(DetailsKeys.INVITED_BY, invite.getUser().getUsername()));
        return userMapper.toUserDto(user);
    }

    public ItemRecord getItemRecordById(Long itemId) {
        Long fridgeId = userUtils.getCurrentUserFridge().getId();
        ItemRecord itemRecord = itemRepository.findById(itemId).orElseThrow(() -> new ItemNotFoundException("Item not found"));
        if (!fridgeId.equals(itemRecord.getFridge().getId())) {
            throw new ItemNotFoundException("Item not found");
        }
        return itemRecord;
    }

}
