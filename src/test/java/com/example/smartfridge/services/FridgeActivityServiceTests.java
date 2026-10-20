package com.example.smartfridge.services;

import com.example.smartfridge.config.FridgeCommitPropertiesProvider;
import com.example.smartfridge.dtos.UserLogRecordDto;
import com.example.smartfridge.entities.Fridge;
import com.example.smartfridge.entities.User;
import com.example.smartfridge.entities.UserLogRecord;
import com.example.smartfridge.enums.AuditAction;
import com.example.smartfridge.enums.AuditEntityType;
import com.example.smartfridge.mappers.UserLogMapper;
import com.example.smartfridge.repositories.UserLogsRepository;
import com.example.smartfridge.utils.UserUtils;
import org.javers.common.string.PrettyValuePrinter;
import org.javers.core.Changes;
import org.javers.core.Javers;
import org.javers.core.commit.CommitId;
import org.javers.core.commit.CommitMetadata;
import org.javers.core.diff.Change;
import org.javers.core.diff.changetype.*;
import org.javers.core.metamodel.object.CdoSnapshot;
import org.javers.core.metamodel.object.InstanceId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FridgeActivityServiceTests {

    private final Instant fixedTime = Instant.parse("2026-10-16T12:00:00Z");
    @Mock
    private Javers javers;
    @Mock
    private Clock clock;
    @Mock
    private UserUtils userUtils;
    @Mock
    private UserLogsRepository userLogsRepository;
    @Mock
    private UserLogMapper userLogMapper;
    @InjectMocks
    private FridgeActivityService fridgeActivityService;
    private User user;
    private Fridge fridge;

    @BeforeEach
    void setUp() {
        lenient().when(clock.instant()).thenReturn(fixedTime);
        user = new User();
        user.setId(7L);
        user.setUsername("alex");
        fridge = new Fridge();
        fridge.setId(12L);
        fridge.setName("Home");
        user.setFridge(fridge);
    }

    @Test
    void recordUserLogStoresCurrentUserFridgeAndEventDetails() {
        Map<String, String> details = Map.of("invitedUsername", "sam");
        fridgeActivityService.recordUserLog(user, AuditAction.USER_INVITED, AuditEntityType.USER,
                22L, "sam", details);

        ArgumentCaptor<UserLogRecord> captor = ArgumentCaptor.forClass(UserLogRecord.class);
        verify(userLogsRepository).save(captor.capture());
        UserLogRecord saved = captor.getValue();
        assertSame(user, saved.getUser());
        assertSame(fridge, saved.getFridge());
        assertEquals(AuditAction.USER_INVITED, saved.getAction());
        assertEquals(AuditEntityType.USER, saved.getEntityType());
        assertEquals(22L, saved.getEntityId());
        assertEquals("sam", saved.getEntityName());
        assertEquals(details, saved.getDetails());
        assertEquals(fixedTime, saved.getTimestamp());
    }

    @Test
    void recordUserLogUsesExplicitFridgeForLeavingUser() {
        Fridge formerFridge = new Fridge();
        formerFridge.setId(13L);
        user.setFridge(null);
        fridgeActivityService.recordUserLog(user, AuditAction.USER_LEFT, AuditEntityType.USER,
                formerFridge, user.getId(), user.getUsername(), Map.of());

        ArgumentCaptor<UserLogRecord> captor = ArgumentCaptor.forClass(UserLogRecord.class);
        verify(userLogsRepository).save(captor.capture());
        assertSame(formerFridge, captor.getValue().getFridge());
    }

    @Test
    void getFridgeActivityCombinesJaVersAndManualEventsWithoutDuplicateEntityLogs() {
        Instant jaVersTimestamp = Instant.parse("2026-10-01T10:00:00Z");
        ValueChange itemChange = valueChange("ItemRecord", 31L, "name", "Milk", "Oat milk",
                "alex", jaVersTimestamp, "Oat milk");
        ValueChange groceryChange = valueChange("GroceryItem", 41L, "quantity", 1, 2,
                "sam", jaVersTimestamp.plusSeconds(30), "Rice");
        ObjectRemoved removedList = new ObjectRemoved(instanceId("GroceryList", 51L), Optional.empty(),
                Optional.of(commit("alex", jaVersTimestamp.plusSeconds(60), "Weekly")));
        when(userUtils.getCurrentUserFridge()).thenReturn(fridge);
        when(javers.findChanges(any())).thenReturn(new Changes(
                List.of(itemChange, groceryChange, removedList), PrettyValuePrinter.getDefault()));

        UserLogRecord inviteLog = new UserLogRecord();
        inviteLog.setEntityType(AuditEntityType.USER);
        UserLogRecordDto inviteDto = new UserLogRecordDto();
        inviteDto.setAction(AuditAction.USER_INVITED.name());
        inviteDto.setTimestamp(jaVersTimestamp.minusSeconds(30));
        inviteDto.setEntityName("sam");
        when(userLogsRepository.findActivityByFridge(eq(fridge), anyList(), any(Pageable.class)))
                .thenReturn(List.of(inviteLog));
        when(userLogMapper.toUserLogRecordDto(inviteLog)).thenReturn(inviteDto);

        List<UserLogRecordDto> activity = fridgeActivityService.getFridgeActivity();

        assertEquals(4, activity.size());
        assertEquals(AuditAction.GROCERY_LIST_DELETED.name(), activity.getFirst().getAction());
        assertEquals(AuditEntityType.GROCERY_LIST.name(), activity.get(0).getEntityType());
        assertEquals("Weekly", activity.get(0).getEntityName());
        assertEquals(AuditAction.GROCERY_ITEM_UPDATED.name(), activity.get(1).getAction());
        assertEquals(AuditEntityType.GROCERY_ITEM.name(), activity.get(1).getEntityType());
        assertEquals("quantity", activity.get(1).getField());
        assertEquals("1", activity.get(1).getOldValue());
        assertEquals("2", activity.get(1).getNewValue());
        assertEquals(AuditAction.ITEM_UPDATED.name(), activity.get(2).getAction());
        assertEquals("name", activity.get(2).getField());
        assertEquals("Milk", activity.get(2).getOldValue());
        assertEquals("Oat milk", activity.get(2).getNewValue());
        assertSame(inviteDto, activity.get(3));
        assertEquals(fridge.getId(), activity.get(0).getFridgeId());
        verify(userLogMapper).toUserLogRecordDto(inviteLog);
        verify(userLogsRepository).findActivityByFridge(eq(fridge),
                ArgumentMatchers.argThat(types -> types.containsAll(List.of(
                        AuditEntityType.FRIDGE_ITEM, AuditEntityType.GROCERY_ITEM, AuditEntityType.GROCERY_LIST))),
                any(Pageable.class));
    }

    @Test
    void getFridgeActivityMapsCreatedAndDeletedJaVersChanges() {
        Instant timestamp = Instant.parse("2026-10-01T10:00:00Z");
        NewObject created = new NewObject(instanceId("ItemRecord", 31L), Optional.empty(),
                Optional.of(commit("alex", timestamp, "Apples")));
        ObjectRemoved deleted = new ObjectRemoved(instanceId("GroceryItem", 41L), Optional.empty(),
                Optional.of(commit("sam", timestamp.plusSeconds(1), "Bread")));
        InitialValueChange initialName = new InitialValueChange(new PropertyChangeMetadata(
                instanceId("ItemRecord", 31L), "name", Optional.of(commit("alex", timestamp, "Apples")),
                PropertyChangeType.PROPERTY_ADDED), "Apples");
        TerminalValueChange terminalName = new TerminalValueChange(new PropertyChangeMetadata(
                instanceId("GroceryItem", 41L), "name", Optional.of(commit("sam", timestamp.plusSeconds(1), "Bread")),
                PropertyChangeType.PROPERTY_REMOVED), "Bread");
        when(userUtils.getCurrentUserFridge()).thenReturn(fridge);
        when(javers.findChanges(any())).thenReturn(new Changes(List.of(created, initialName, deleted, terminalName),
                PrettyValuePrinter.getDefault()));
        when(userLogsRepository.findActivityByFridge(eq(fridge), anyList(), any(Pageable.class))).thenReturn(List.of());

        List<UserLogRecordDto> activity = fridgeActivityService.getFridgeActivity();

        assertEquals(2, activity.size());
        assertEquals(AuditAction.GROCERY_ITEM_DELETED.name(), activity.get(0).getAction());
        assertEquals("Bread", activity.get(0).getEntityName());
        assertEquals(AuditAction.ITEM_CREATED.name(), activity.get(1).getAction());
        assertEquals("Apples", activity.get(1).getEntityName());
    }

    @Test
    void getFridgeActivityReturnsRequestedSliceOfMergedEvents() {
        Instant timestamp = Instant.parse("2026-10-01T10:00:00Z");
        List<Change> changes = List.of(
                valueChange("ItemRecord", 1L, "name", "First", "First updated", "alex", timestamp, "First"),
                valueChange("ItemRecord", 2L, "name", "Second", "Second updated", "alex", timestamp.plusSeconds(1), "Second"),
                valueChange("ItemRecord", 3L, "name", "Third", "Third updated", "alex", timestamp.plusSeconds(2), "Third"));
        when(userUtils.getCurrentUserFridge()).thenReturn(fridge);
        when(javers.findChanges(any())).thenReturn(new Changes(changes, PrettyValuePrinter.getDefault()));
        when(userLogsRepository.findActivityByFridge(eq(fridge), anyList(), any(Pageable.class))).thenReturn(List.of());

        List<UserLogRecordDto> page = fridgeActivityService.getFridgeActivity(1, 2);

        assertEquals(2, page.size());
        assertEquals(2L, page.get(0).getEntityId());
        assertEquals(1L, page.get(1).getEntityId());
        verify(userLogsRepository).findActivityByFridge(eq(fridge), anyList(),
                ArgumentMatchers.argThat(pageable -> pageable.getPageSize() == 3));
    }

    @Test
    void getFridgeActivityFetchesMoreJaVersChangesWhenCreationDetailsAreFilteredOut() {
        Instant firstTimestamp = Instant.parse("2026-10-01T10:00:00Z");
        Instant secondTimestamp = firstTimestamp.plusSeconds(1);
        NewObject firstCreated = new NewObject(instanceId("ItemRecord", 1L), Optional.empty(),
                Optional.of(commit("alex", firstTimestamp, "First")));
        InitialValueChange firstName = initialName(1L, "First", firstTimestamp);
        NewObject secondCreated = new NewObject(instanceId("ItemRecord", 2L), Optional.empty(),
                Optional.of(commit("alex", secondTimestamp, "Second")));
        InitialValueChange secondName = initialName(2L, "Second", secondTimestamp);
        Changes firstPage = new Changes(List.of(firstCreated, firstName), PrettyValuePrinter.getDefault());
        Changes expandedPage = new Changes(List.of(secondCreated, secondName),
                PrettyValuePrinter.getDefault());
        when(userUtils.getCurrentUserFridge()).thenReturn(fridge);
        when(javers.findChanges(any())).thenReturn(firstPage, expandedPage);
        when(javers.findSnapshots(any())).thenReturn(Collections.nCopies(200, mock(CdoSnapshot.class)));
        when(userLogsRepository.findActivityByFridge(eq(fridge), anyList(), any(Pageable.class))).thenReturn(List.of());

        List<UserLogRecordDto> activity = fridgeActivityService.getFridgeActivity(0, 2);

        assertEquals(2, activity.size());
        assertEquals(List.of(2L, 1L), activity.stream().map(UserLogRecordDto::getEntityId).toList());
        assertEquals(List.of(AuditAction.ITEM_CREATED.name(), AuditAction.ITEM_CREATED.name()),
                activity.stream().map(UserLogRecordDto::getAction).toList());
        verify(javers, times(2)).findChanges(any());
    }

    @Test
    void getFridgeActivityContinuesPastFiveThousandFilteredSnapshots() {
        Instant timestamp = Instant.parse("2026-10-01T10:00:00Z");
        ValueChange ignored = valueChange("GroceryList", 1L, "createdAt", "before", "after",
                "alex", timestamp, "Weekly");
        ValueChange visible = valueChange("ItemRecord", 2L, "quantity", 1, 2,
                "alex", timestamp.minusSeconds(1), "Milk");
        Changes ignoredChanges = new Changes(List.of(ignored), PrettyValuePrinter.getDefault());
        Changes visibleChanges = new Changes(List.of(visible), PrettyValuePrinter.getDefault());
        when(userUtils.getCurrentUserFridge()).thenReturn(fridge);
        int[] calls = {0};
        when(javers.findChanges(any())).thenAnswer(invocation -> ++calls[0] <= 25
                ? ignoredChanges : visibleChanges);
        when(javers.findSnapshots(any())).thenReturn(Collections.nCopies(200, mock(CdoSnapshot.class)));
        when(userLogsRepository.findActivityByFridge(eq(fridge), anyList(), any(Pageable.class))).thenReturn(List.of());

        List<UserLogRecordDto> activity = fridgeActivityService.getFridgeActivity(0, 1);

        assertEquals(1, activity.size());
        assertEquals(2L, activity.getFirst().getEntityId());
        verify(javers, times(26)).findChanges(any());
    }

    @Test
    void getFridgeActivityDoesNotApplyCommitNameToOtherEntitiesInSameCommit() {
        Instant timestamp = Instant.parse("2026-10-01T10:00:00Z");
        CommitMetadata sharedCommit = commit("alex", timestamp, "Root object");
        ValueChange itemQuantity = new ValueChange(new PropertyChangeMetadata(instanceId("ItemRecord", 1L),
                "quantity", Optional.of(sharedCommit), PropertyChangeType.PROPERTY_VALUE_CHANGED), 1, 2);
        ValueChange groceryName = new ValueChange(new PropertyChangeMetadata(instanceId("GroceryItem", 2L),
                "name", Optional.of(sharedCommit), PropertyChangeType.PROPERTY_VALUE_CHANGED), "Rice", "Brown rice");
        when(userUtils.getCurrentUserFridge()).thenReturn(fridge);
        when(javers.findChanges(any())).thenReturn(new Changes(List.of(itemQuantity, groceryName),
                PrettyValuePrinter.getDefault()));
        when(userLogsRepository.findActivityByFridge(eq(fridge), anyList(), any(Pageable.class))).thenReturn(List.of());

        List<UserLogRecordDto> activity = fridgeActivityService.getFridgeActivity();

        assertEquals(2, activity.size());
        UserLogRecordDto groceryChange = activity.stream()
                .filter(entry -> entry.getEntityType().equals(AuditEntityType.GROCERY_ITEM.name()))
                .findFirst().orElseThrow();
        UserLogRecordDto itemChange = activity.stream()
                .filter(entry -> entry.getEntityType().equals(AuditEntityType.FRIDGE_ITEM.name()))
                .findFirst().orElseThrow();
        assertEquals("Brown rice", groceryChange.getEntityName());
        assertNull(itemChange.getEntityName());
    }

    @Test
    void getFridgeActivityExcludesFridgeCreationAndDeletion() {
        Instant timestamp = Instant.parse("2026-10-01T10:00:00Z");
        InstanceId fridgeId = new InstanceId(Fridge.class.getName(), fridge.getId(), "id");
        NewObject createdFridge = new NewObject(fridgeId, Optional.empty(),
                Optional.of(commit("alex", timestamp, "Home")));
        ObjectRemoved deletedFridge = new ObjectRemoved(fridgeId, Optional.empty(),
                Optional.of(commit("alex", timestamp.plusSeconds(1), "Home")));
        when(userUtils.getCurrentUserFridge()).thenReturn(fridge);
        when(javers.findChanges(any())).thenReturn(new Changes(List.of(createdFridge, deletedFridge),
                PrettyValuePrinter.getDefault()));
        when(userLogsRepository.findActivityByFridge(eq(fridge), anyList(), any(Pageable.class))).thenReturn(List.of());

        assertTrue(fridgeActivityService.getFridgeActivity().isEmpty());
    }

    private ValueChange valueChange(String typeName, Long id, String field, Object oldValue,
                                    Object newValue, String author, Instant timestamp, String entityName) {
        InstanceId idValue = instanceId(typeName, id);
        CommitMetadata metadata = commit(author, timestamp, entityName);
        PropertyChangeMetadata changeMetadata = new PropertyChangeMetadata(idValue, field,
                Optional.of(metadata), PropertyChangeType.PROPERTY_VALUE_CHANGED);
        return new ValueChange(changeMetadata, oldValue, newValue);
    }

    private InitialValueChange initialName(Long id, String name, Instant timestamp) {
        return new InitialValueChange(new PropertyChangeMetadata(instanceId("ItemRecord", id), "name",
                Optional.of(commit("alex", timestamp, name)), PropertyChangeType.PROPERTY_ADDED), name);
    }

    private InstanceId instanceId(String typeName, Long id) {
        return new InstanceId("com.example.smartfridge.entities." + typeName, id, "id");
    }

    private CommitMetadata commit(String author, Instant timestamp, String entityName) {
        return new CommitMetadata(author,
                Map.of(FridgeCommitPropertiesProvider.ENTITY_NAME, entityName),
                LocalDateTime.ofInstant(timestamp, java.time.ZoneOffset.UTC), timestamp,
                new CommitId(timestamp.toEpochMilli(), 0));
    }
}
