package com.example.smartfridge.services;

import com.example.smartfridge.dtos.*;
import com.example.smartfridge.entities.Fridge;
import com.example.smartfridge.entities.FridgeInvites;
import com.example.smartfridge.entities.ItemRecord;
import com.example.smartfridge.entities.User;
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
import com.example.smartfridge.utils.UserUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FridgeServiceTests {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private FridgeMapper fridgeMapper;

    @Mock
    private FridgeInvitesMapper fridgeInvitesMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FridgeRepository fridgeRepository;

    @Mock
    private FridgeInvitesRepository fridgeInvitesRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserUtils userUtils;

    @InjectMocks
    private FridgeService fridgeService;

    private User user;
    private User otherUser;
    private Fridge fridge;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("current");

        otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("other");

        fridge = new Fridge();
        fridge.setId(10L);
        fridge.setName("Home");
        fridge.setOwner(user);
        fridge.setUsers(new ArrayList<>());
        user.setFridge(fridge);
    }

    @Test
    void getCurrentUserFridgeDtoMapsCurrentFridge() {
        FridgeDto expected = new FridgeDto();
        when(userUtils.getCurrentUserFridge()).thenReturn(fridge);
        when(fridgeMapper.toFridgeDto(fridge)).thenReturn(expected);

        assertSame(expected, fridgeService.getCurrentUserFridgeDto());
    }

    @Test
    void allItemsByFridgeLoadsAndMapsCurrentFridgeItems() {
        List<ItemRecord> items = List.of(new ItemRecord());
        List<ItemRecordDto> expected = List.of(new ItemRecordDto());
        when(userUtils.getCurrentUserFridge()).thenReturn(fridge);
        when(itemRepository.findAllByFridge(fridge)).thenReturn(items);
        when(itemMapper.toItemRecordDtoList(items)).thenReturn(expected);

        assertSame(expected, fridgeService.allItemsByFridge());
    }

    @Test
    void getItemByIdReturnsItemFromCurrentFridge() {
        ItemRecord item = item(fridge);
        ItemRecordDto expected = new ItemRecordDto();
        givenCurrentFridgeAndItem(item);
        when(itemMapper.toItemRecordDto(item)).thenReturn(expected);

        assertSame(expected, fridgeService.getItemById(item.getId()));
    }

    @Test
    void getItemByIdThrowsWhenItemBelongsToAnotherFridge() {
        Fridge otherFridge = new Fridge();
        otherFridge.setId(11L);
        ItemRecord item = item(otherFridge);
        givenCurrentFridgeAndItem(item);

        assertThrows(ItemNotFoundException.class, () -> fridgeService.getItemById(item.getId()));
    }

    @Test
    void createItemClearsIdAssociatesCurrentFridgeAndMapsSavedItem() {
        ItemRecordDto input = new ItemRecordDto(99L, "Milk", "Whole", 2, "Dairy", 2.5,
                LocalDate.of(2026, 9, 25), LocalDate.of(2026, 9, 19), null);
        ItemRecord item = new ItemRecord();
        ItemRecordDto expected = new ItemRecordDto();
        when(itemMapper.toItemRecord(input)).thenReturn(item);
        when(userUtils.getCurrentUserFridge()).thenReturn(fridge);
        when(itemMapper.toItemRecordDto(item)).thenReturn(expected);

        assertSame(expected, fridgeService.createItem(input));

        assertNull(input.getId());
        assertSame(fridge, item.getFridge());
        verify(itemRepository).save(item);
    }

    @Test
    void updateItemCopiesEditableFieldsAndSavesItem() {
        ItemRecord item = item(fridge);
        ItemRecordDto update = new ItemRecordDto(30L, "Bread", "Sourdough", 1, "Bakery", 3.0,
                LocalDate.of(2026, 9, 26), LocalDate.of(2026, 9, 19), fridge.getId());
        ItemRecordDto expected = new ItemRecordDto();
        givenCurrentFridgeAndItem(item);
        when(itemMapper.toItemRecordDto(item)).thenReturn(expected);

        assertSame(expected, fridgeService.updateItem(item.getId(), update));

        assertEquals("Bread", item.getName());
        assertEquals("Sourdough", item.getDescription());
        assertEquals(1, item.getQuantity());
        assertEquals("Bakery", item.getCategory());
        assertEquals(3.0, item.getPrice());
        assertEquals(update.getBuyingDate(), item.getBuyingDate());
        assertEquals(update.getExpirationDate(), item.getExpirationDate());
        verify(itemRepository).save(item);
    }

    @Test
    void deleteItemDeletesAndReturnsItemDto() {
        ItemRecord item = item(fridge);
        ItemRecordDto expected = new ItemRecordDto();
        givenCurrentFridgeAndItem(item);
        when(itemMapper.toItemRecordDto(item)).thenReturn(expected);

        assertSame(expected, fridgeService.deleteItem(item.getId()));

        verify(itemRepository).delete(item);
    }

    @Test
    void getItemRecordByIdThrowsWhenItemDoesNotExist() {
        when(userUtils.getCurrentUserFridge()).thenReturn(fridge);
        when(itemRepository.findById(30L)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> fridgeService.getItemRecordById(30L));
    }

    @Test
    void createFridgeAssignsOwnerAndSavesUser() {
        FridgeDto expected = new FridgeDto();
        user.setFridge(null);
        when(userUtils.getUserFromAuthentication()).thenReturn(user);
        when(fridgeMapper.toFridgeDto(any(Fridge.class))).thenReturn(expected);

        assertSame(expected, fridgeService.createFridge("New fridge"));

        ArgumentCaptor<Fridge> fridgeCaptor = ArgumentCaptor.forClass(Fridge.class);
        verify(fridgeRepository).save(fridgeCaptor.capture());
        Fridge savedFridge = fridgeCaptor.getValue();
        assertEquals("New fridge", savedFridge.getName());
        assertSame(user, savedFridge.getOwner());
        assertSame(savedFridge, user.getFridge());
        verify(userRepository).save(user);
    }

    @Test
    void createFridgeThrowsWhenUserAlreadyHasFridge() {
        when(userUtils.getUserFromAuthentication()).thenReturn(user);

        assertThrows(UserAlreadyHasFridgeException.class, () -> fridgeService.createFridge("Another"));
        verify(fridgeRepository, never()).save(any());
    }

    @Test
    void deleteFridgeClearsMembersAndDeletesOwnedFridge() {
        User member = new User();
        member.setId(3L);
        member.setFridge(fridge);
        fridge.setUsers(new ArrayList<>(List.of(user, member)));
        when(userUtils.getUserFromAuthentication()).thenReturn(user);
        when(userUtils.getCurrentUserFridge()).thenReturn(fridge);

        fridgeService.deleteFridge();

        assertNull(user.getFridge());
        assertNull(member.getFridge());
        verify(userRepository).saveAll(fridge.getUsers());
        verify(fridgeRepository).delete(fridge);
    }

    @Test
    void deleteFridgeThrowsWhenCurrentUserIsNotOwner() {
        fridge.setOwner(otherUser);
        when(userUtils.getUserFromAuthentication()).thenReturn(user);
        when(userUtils.getCurrentUserFridge()).thenReturn(fridge);

        assertThrows(UserIsNotOwnerOfFridgeException.class, () -> fridgeService.deleteFridge());
        verify(fridgeRepository, never()).delete(any(Fridge.class));
    }

    @Test
    void inviteUserCreatesInviteForUserWithoutFridge() {
        user.setFridge(fridge);
        otherUser.setFridge(null);
        FridgeInvitesDto expected = new FridgeInvitesDto();
        when(userUtils.getUserFromAuthentication()).thenReturn(user);
        when(userRepository.findUserByUsername(otherUser.getUsername())).thenReturn(Optional.of(otherUser));
        when(fridgeInvitesRepository.existsByFridgeAndUserAndInvitedUser(fridge, user, otherUser)).thenReturn(false);
        when(fridgeInvitesMapper.toDto(any(FridgeInvites.class))).thenReturn(expected);

        assertSame(expected, fridgeService.inviteUser(otherUser.getUsername()));

        ArgumentCaptor<FridgeInvites> captor = ArgumentCaptor.forClass(FridgeInvites.class);
        verify(fridgeInvitesRepository).save(captor.capture());
        FridgeInvites invite = captor.getValue();
        assertSame(fridge, invite.getFridge());
        assertSame(user, invite.getUser());
        assertSame(otherUser, invite.getInvitedUser());
        assertEquals(FridgeInviteStatus.INVITED, invite.getStatus());
    }

    @Test
    void inviteUserThrowsWhenInvitedUserDoesNotExist() {
        when(userUtils.getUserFromAuthentication()).thenReturn(user);
        when(userRepository.findUserByUsername("missing")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> fridgeService.inviteUser("missing"));
    }

    @Test
    void inviteUserThrowsWhenInvitingSelf() {
        when(userUtils.getUserFromAuthentication()).thenReturn(user);
        when(userRepository.findUserByUsername(user.getUsername())).thenReturn(Optional.of(user));

        assertThrows(InviteConflictException.class, () -> fridgeService.inviteUser(user.getUsername()));
    }

    @Test
    void inviteUserThrowsWhenCurrentUserHasNoFridge() {
        user.setFridge(null);
        when(userUtils.getUserFromAuthentication()).thenReturn(user);
        when(userRepository.findUserByUsername(otherUser.getUsername())).thenReturn(Optional.of(otherUser));

        assertThrows(UserDoesNotHaveFridgeException.class, () -> fridgeService.inviteUser(otherUser.getUsername()));
    }

    @Test
    void inviteUserThrowsWhenInviteAlreadyExists() {
        when(userUtils.getUserFromAuthentication()).thenReturn(user);
        when(userRepository.findUserByUsername(otherUser.getUsername())).thenReturn(Optional.of(otherUser));
        when(fridgeInvitesRepository.existsByFridgeAndUserAndInvitedUser(fridge, user, otherUser)).thenReturn(true);

        assertThrows(InviteConflictException.class, () -> fridgeService.inviteUser(otherUser.getUsername()));
    }

    @Test
    void getInvitesForUserReturnsOnlyPendingInvites() {
        FridgeInvites pending = invite(40L, user, otherUser, fridge, FridgeInviteStatus.INVITED);
        FridgeInvites accepted = invite(41L, user, otherUser, fridge, FridgeInviteStatus.ACCEPTED);
        when(userUtils.getUserFromAuthentication()).thenReturn(otherUser);
        when(fridgeInvitesRepository.findAllByInvitedUser(otherUser)).thenReturn(List.of(pending, accepted));

        List<InvitesResponseDto> result = fridgeService.getInvitesForUser();

        assertEquals(1, result.size());
        assertEquals(40L, result.getFirst().getId());
        assertEquals(user.getUsername(), result.getFirst().getUsername());
        assertEquals(fridge.getName(), result.getFirst().getFridgeName());
    }

    @Test
    void leaveFridgeClearsFridgeForNonOwner() {
        UserDto expected = new UserDto();
        fridge.setOwner(otherUser);
        when(userUtils.getUserFromAuthentication()).thenReturn(user);
        when(userUtils.getCurrentUserFridge()).thenReturn(fridge);
        when(userMapper.toUserDto(user)).thenReturn(expected);

        assertSame(expected, fridgeService.leaveFridge());

        assertNull(user.getFridge());
        verify(userRepository).save(user);
    }

    @Test
    void leaveFridgeThrowsForOwner() {
        when(userUtils.getUserFromAuthentication()).thenReturn(user);
        when(userUtils.getCurrentUserFridge()).thenReturn(fridge);

        assertThrows(UserIsOwnerOfThisFridgeException.class, () -> fridgeService.leaveFridge());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void joinFridgeAssignsFridgeAndAcceptsInvite() {
        user.setFridge(null);
        FridgeInvites invite = invite(40L, user, user, fridge, FridgeInviteStatus.INVITED);
        UserDto expected = new UserDto();
        when(userUtils.getUserFromAuthentication()).thenReturn(user);
        when(fridgeInvitesRepository.findById(invite.getId())).thenReturn(Optional.of(invite));
        when(fridgeRepository.findById(fridge.getId())).thenReturn(Optional.of(fridge));
        when(userMapper.toUserDto(user)).thenReturn(expected);

        assertSame(expected, fridgeService.joinFridge(invite.getId()));

        assertSame(fridge, user.getFridge());
        assertEquals(FridgeInviteStatus.ACCEPTED, invite.getStatus());
        verify(fridgeInvitesRepository).save(invite);
        verify(userRepository).save(user);
    }

    @Test
    void joinFridgeThrowsWhenInviteDoesNotExist() {
        when(userUtils.getUserFromAuthentication()).thenReturn(user);
        when(fridgeInvitesRepository.findById(40L)).thenReturn(Optional.empty());

        assertThrows(InviteNotOwnedException.class, () -> fridgeService.joinFridge(40L));
    }

    @Test
    void joinFridgeThrowsWhenInviteBelongsToAnotherUser() {
        user.setFridge(null);
        FridgeInvites invite = invite(40L, otherUser, otherUser, fridge, FridgeInviteStatus.INVITED);
        when(userUtils.getUserFromAuthentication()).thenReturn(user);
        when(fridgeInvitesRepository.findById(invite.getId())).thenReturn(Optional.of(invite));

        assertThrows(InviteNotOwnedException.class, () -> fridgeService.joinFridge(invite.getId()));
    }

    @Test
    void joinFridgeThrowsWhenInviteWasAlreadyProcessed() {
        user.setFridge(null);
        FridgeInvites invite = invite(40L, user, user, fridge, FridgeInviteStatus.ACCEPTED);
        when(userUtils.getUserFromAuthentication()).thenReturn(user);
        when(fridgeInvitesRepository.findById(invite.getId())).thenReturn(Optional.of(invite));

        assertThrows(InviteAlreadyProcessedException.class, () -> fridgeService.joinFridge(invite.getId()));
    }

    @Test
    void joinFridgeThrowsWhenTargetFridgeDoesNotExist() {
        user.setFridge(null);
        FridgeInvites invite = invite(40L, user, user, fridge, FridgeInviteStatus.INVITED);
        when(userUtils.getUserFromAuthentication()).thenReturn(user);
        when(fridgeInvitesRepository.findById(invite.getId())).thenReturn(Optional.of(invite));
        when(fridgeRepository.findById(fridge.getId())).thenReturn(Optional.empty());

        assertThrows(FridgeNotFoundException.class, () -> fridgeService.joinFridge(invite.getId()));
    }

    private ItemRecord item(Fridge itemFridge) {
        ItemRecord item = new ItemRecord();
        item.setId(30L);
        item.setFridge(itemFridge);
        return item;
    }

    private void givenCurrentFridgeAndItem(ItemRecord item) {
        when(userUtils.getCurrentUserFridge()).thenReturn(fridge);
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
    }

    private FridgeInvites invite(Long id, User inviter, User invitedUser, Fridge invitedFridge,
                                 FridgeInviteStatus status) {
        FridgeInvites invite = new FridgeInvites();
        invite.setId(id);
        invite.setUser(inviter);
        invite.setInvitedUser(invitedUser);
        invite.setFridge(invitedFridge);
        invite.setStatus(status);
        return invite;
    }
}
