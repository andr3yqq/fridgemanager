package com.example.smartfridge.services;

import com.example.smartfridge.dtos.GroceryItemDto;
import com.example.smartfridge.dtos.GroceryListDto;
import com.example.smartfridge.dtos.ItemRecordDto;
import com.example.smartfridge.entities.*;
import com.example.smartfridge.exceptions.*;
import com.example.smartfridge.mappers.GroceryItemMapper;
import com.example.smartfridge.mappers.GroceryListMapper;
import com.example.smartfridge.mappers.ItemMapper;
import com.example.smartfridge.repositories.GroceryItemRepository;
import com.example.smartfridge.repositories.GroceryListRepository;
import com.example.smartfridge.repositories.ItemRepository;
import com.example.smartfridge.utils.UserUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroceryServiceTests {

    @Mock
    private ItemRepository itemRecordRepository;

    @Mock
    private GroceryItemRepository groceryItemRepository;

    @Mock
    private GroceryListRepository groceryListRepository;

    @Mock
    private ItemMapper itemRecordMapper;

    @Mock
    private GroceryListMapper groceryListMapper;

    @Mock
    private GroceryItemMapper groceryItemMapper;

    @Mock
    private UserUtils userUtils;

    @InjectMocks
    private GroceryService groceryService;

    private User user;
    private User otherUser;
    private Fridge fridge;
    private GroceryList groceryList;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        otherUser = new User();
        otherUser.setId(2L);

        fridge = new Fridge();
        fridge.setId(10L);
        fridge.setOwner(user);
        user.setFridge(fridge);

        groceryList = new GroceryList();
        groceryList.setId(20L);
        groceryList.setFridge(fridge);
        groceryList.setUser(user);
    }

    @Test
    void allListsReturnsListsForCurrentUsersFridge() {
        List<GroceryList> lists = List.of(groceryList);
        List<GroceryListDto> expected = List.of(new GroceryListDto());
        when(userUtils.getCurrentUserFridge()).thenReturn(fridge);
        when(groceryListRepository.findAllByFridgeId(fridge.getId())).thenReturn(lists);
        when(groceryListMapper.toGroceryListDtoList(lists)).thenReturn(expected);

        assertSame(expected, groceryService.allLists());
    }

    @Test
    void allListsThrowsWhenUserHasNoFridge() {
        when(userUtils.getCurrentUserFridge()).thenReturn(null);

        assertThrows(UserDoesNotHaveFridgeException.class, () -> groceryService.allLists());
        verifyNoListLookup();
    }

    @Test
    void getListByIdReturnsListWhenUserHasAccess() {
        GroceryListDto expected = new GroceryListDto();
        givenCurrentFridge();
        when(groceryListRepository.findById(groceryList.getId())).thenReturn(Optional.of(groceryList));
        when(groceryListMapper.toGroceryListDto(groceryList)).thenReturn(expected);

        assertSame(expected, groceryService.getListById(groceryList.getId()));
    }

    @Test
    void getListByIdThrowsWhenListDoesNotExist() {
        when(groceryListRepository.findById(20L)).thenReturn(Optional.empty());

        assertThrows(GroceryListNotFoundException.class, () -> groceryService.getListById(20L));
    }

    @Test
    void getListByIdThrowsWhenUserHasNoAccessToListFridge() {
        Fridge otherFridge = new Fridge();
        otherFridge.setId(11L);
        groceryList.setFridge(otherFridge);
        when(groceryListRepository.findById(groceryList.getId())).thenReturn(Optional.of(groceryList));
        when(userUtils.getCurrentUserFridge()).thenReturn(fridge);

        assertThrows(UserDoesNotHaveAccessToFridgeException.class,
                () -> groceryService.getListById(groceryList.getId()));
    }

    @Test
    void getItemsByListIdMapsItemsWhenUserHasAccess() {
        List<GroceryItem> items = List.of(new GroceryItem());
        List<GroceryItemDto> expected = List.of(new GroceryItemDto());
        groceryList.setItems(items);
        givenCurrentFridge();
        when(groceryListRepository.findById(groceryList.getId())).thenReturn(Optional.of(groceryList));
        when(groceryItemMapper.toGroceryItemDtoList(items)).thenReturn(expected);

        assertSame(expected, groceryService.getItemsByListId(groceryList.getId()));
    }

    @Test
    void createListSetsNameDescriptionFridgeAndCreator() {
        GroceryListDto expected = new GroceryListDto();
        when(userUtils.getUserFromAuthentication()).thenReturn(user);
        when(groceryListRepository.save(any(GroceryList.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(groceryListMapper.toGroceryListDto(any(GroceryList.class))).thenReturn(expected);

        assertSame(expected, groceryService.createList("Weekly shopping", "For this week"));

        ArgumentCaptor<GroceryList> captor = ArgumentCaptor.forClass(GroceryList.class);
        verify(groceryListRepository).save(captor.capture());
        GroceryList saved = captor.getValue();
        assertEquals("Weekly shopping", saved.getName());
        assertEquals("For this week", saved.getDescription());
        assertSame(fridge, saved.getFridge());
        assertSame(user, saved.getUser());
    }

    @Test
    void createListThrowsWhenUserHasNoFridge() {
        user.setFridge(null);
        when(userUtils.getUserFromAuthentication()).thenReturn(user);

        assertThrows(UserDoesNotHaveFridgeException.class,
                () -> groceryService.createList("Shopping", "Description"));
        verify(groceryListRepository, never()).save(any());
    }

    @Test
    void updateListChangesDetailsForCreator() {
        GroceryListDto expected = new GroceryListDto();
        when(userUtils.getUserFromAuthentication()).thenReturn(user);
        when(groceryListRepository.findById(groceryList.getId())).thenReturn(Optional.of(groceryList));
        when(groceryListRepository.save(groceryList)).thenReturn(groceryList);
        when(groceryListMapper.toGroceryListDto(groceryList)).thenReturn(expected);

        assertSame(expected, groceryService.updateList(groceryList.getId(), "Updated", "New description"));
        assertEquals("Updated", groceryList.getName());
        assertEquals("New description", groceryList.getDescription());
        verify(groceryListRepository).save(groceryList);
    }

    @Test
    void deleteListThrowsWhenCurrentUserDoesNotOwnList() {
        when(userUtils.getUserFromAuthentication()).thenReturn(otherUser);
        when(groceryListRepository.findById(groceryList.getId())).thenReturn(Optional.of(groceryList));

        assertThrows(UserIsNotOwnerOfGroceryListException.class,
                () -> groceryService.deleteList(groceryList.getId()));
        verify(groceryListRepository, never()).delete(any(GroceryList.class));
    }

    @Test
    void addItemToListAssociatesAndSavesItem() {
        GroceryItemDto input = new GroceryItemDto(null, "Milk", "Whole", 2, "Dairy", false);
        GroceryItem item = new GroceryItem();
        GroceryItemDto expected = new GroceryItemDto();
        givenCurrentFridge();
        when(groceryItemMapper.toGroceryItem(input)).thenReturn(item);
        when(groceryListRepository.findById(groceryList.getId())).thenReturn(Optional.of(groceryList));
        when(groceryItemRepository.save(item)).thenReturn(item);
        when(groceryItemMapper.toGroceryItemDto(item)).thenReturn(expected);

        assertSame(expected, groceryService.addItemToList(input, groceryList.getId()));
        assertSame(groceryList, item.getGroceryList());
    }

    @Test
    void deleteItemDeletesItemWhenUserOwnsList() {
        GroceryItem item = new GroceryItem();
        item.setId(30L);
        item.setGroceryList(groceryList);
        when(groceryItemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(userUtils.getUserFromAuthentication()).thenReturn(user);
        when(groceryListRepository.findById(groceryList.getId())).thenReturn(Optional.of(groceryList));

        groceryService.deleteItem(item.getId());

        verify(groceryItemRepository).delete(item);
    }

    @Test
    void updateItemMapsAndSavesExistingItemWhenUserOwnsList() {
        GroceryItemDto update = new GroceryItemDto(30L, "Bread", "Sourdough", 1, "Bakery", true);
        GroceryItem item = new GroceryItem();
        item.setId(update.getId());
        item.setGroceryList(groceryList);
        when(groceryItemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(userUtils.getUserFromAuthentication()).thenReturn(user);
        when(groceryListRepository.findById(groceryList.getId())).thenReturn(Optional.of(groceryList));

        groceryService.updateItem(update);

        verify(groceryItemMapper).updateGroceryItemFromDto(update, item);
        verify(groceryItemRepository).save(item);
    }

    @Test
    void toggleItemPurchasedUpdatesAndReturnsItem() {
        GroceryItem item = new GroceryItem();
        item.setId(30L);
        item.setGroceryList(groceryList);
        GroceryItemDto expected = new GroceryItemDto();
        givenCurrentFridge();
        when(groceryItemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(groceryItemRepository.save(item)).thenReturn(item);
        when(groceryItemMapper.toGroceryItemDto(item)).thenReturn(expected);

        assertSame(expected, groceryService.toggleItemPurchased(item.getId(), true));
        assertTrue(item.isPurchased());
    }

    @Test
    void toggleItemPurchasedThrowsWhenItemDoesNotExist() {
        when(groceryItemRepository.findById(30L)).thenReturn(Optional.empty());

        assertThrows(GroceryItemNotFoundException.class,
                () -> groceryService.toggleItemPurchased(30L, true));
    }

    @Test
    void copyPurchasedItemsToFridgeUnpurchasesAndCopiesItems() {
        GroceryItem groceryItem = new GroceryItem();
        groceryItem.setId(30L);
        groceryItem.setGroceryList(groceryList);
        ItemRecordDto purchased = new ItemRecordDto(30L, "Milk", "Whole", 2, "Dairy", 2.5,
                LocalDate.of(2026, 9, 25), null, fridge.getId());
        ItemRecordDto expected = new ItemRecordDto();
        ItemRecord savedItem = new ItemRecord();
        givenCurrentFridge();
        when(groceryListRepository.findById(groceryList.getId())).thenReturn(Optional.of(groceryList));
        when(groceryItemRepository.findById(groceryItem.getId())).thenReturn(Optional.of(groceryItem));
        when(groceryItemRepository.save(groceryItem)).thenReturn(groceryItem);
        when(itemRecordRepository.saveAll(any())).thenReturn(List.of(savedItem));
        when(itemRecordMapper.toItemRecordDtoList(List.of(savedItem))).thenReturn(List.of(expected));

        assertEquals(List.of(expected), groceryService.copyPurchasedItemsToFridge(groceryList.getId(), List.of(purchased)));
        assertFalse(groceryItem.isPurchased());

        ArgumentCaptor<List<ItemRecord>> captor = ArgumentCaptor.forClass(List.class);
        verify(itemRecordRepository).saveAll(captor.capture());
        ItemRecord copied = captor.getValue().getFirst();
        assertEquals("Milk", copied.getName());
        assertEquals("Whole", copied.getDescription());
        assertEquals(2, copied.getQuantity());
        assertEquals("Dairy", copied.getCategory());
        assertEquals(2.5, copied.getPrice());
        assertEquals(purchased.getExpirationDate(), copied.getExpirationDate());
        assertEquals(LocalDate.now(), copied.getBuyingDate());
        assertSame(fridge, copied.getFridge());
    }

    @Test
    void copyPurchasedItemsToFridgeThrowsWhenListDoesNotExist() {
        when(groceryListRepository.findById(groceryList.getId())).thenReturn(Optional.empty());

        assertThrows(GroceryListNotFoundException.class,
                () -> groceryService.copyPurchasedItemsToFridge(groceryList.getId(), List.of()));
    }

    private void givenCurrentFridge() {
        when(userUtils.getCurrentUserFridge()).thenReturn(fridge);
    }

    private void verifyNoListLookup() {
        verify(groceryListRepository, never()).findAllByFridgeId(any());
    }
}
