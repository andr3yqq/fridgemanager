package com.example.smartfridge.services;

import com.example.smartfridge.config.FridgeCommitPropertiesProvider;
import com.example.smartfridge.dtos.UserLogRecordDto;
import com.example.smartfridge.entities.*;
import com.example.smartfridge.enums.AuditAction;
import com.example.smartfridge.mappers.UserLogMapper;
import com.example.smartfridge.repositories.UserLogsRepository;
import com.example.smartfridge.utils.UserUtils;
import org.javers.core.JaversBuilder;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JaversAuditTests {

    @Test
    void committingAuditedEntitiesDoesNotSnapshotRelatedEntitiesOrUsers() {
        User owner = new User();
        owner.setId(1L);
        owner.setPassword("test-password-hash");
        Fridge fridge = new Fridge();
        fridge.setId(2L);
        fridge.setOwner(owner);
        fridge.getUsers().add(owner);
        owner.setFridge(fridge);
        ItemRecord item = new ItemRecord();
        item.setId(3L);
        item.setName("Milk");
        item.setFridge(fridge);
        fridge.getItems().add(item);
        GroceryList list = new GroceryList();
        list.setId(4L);
        list.setName("Weekly");
        list.setFridge(fridge);
        list.setUser(owner);
        fridge.getGroceryLists().add(list);
        GroceryItem groceryItem = new GroceryItem();
        groceryItem.setId(5L);
        groceryItem.setName("Bread");
        groceryItem.setGroceryList(list);
        list.setItems(List.of(groceryItem));

        for (Object entity : List.of(item, list, groceryItem)) {
            var javers = JaversBuilder.javers().build();
            var commit = javers.commit("alex", entity);

            assertEquals(1, commit.getSnapshots().size());
            assertEquals(entity.getClass().getName(),
                    commit.getSnapshots().getFirst().getGlobalId().getTypeName());
        }
    }

    @Test
    void activityReadsSnapshotBatchesWithoutRepeatingBoundaryChanges() {
        Fridge fridge = new Fridge();
        fridge.setId(1L);
        ItemRecord item = new ItemRecord();
        item.setId(2L);
        item.setName("Milk");
        item.setFridge(fridge);
        var javers = JaversBuilder.javers().build();
        var properties = new FridgeCommitPropertiesProvider().provideForCommittedObject(item);
        for (int quantity = 0; quantity < 250; quantity++) {
            item.setQuantity(quantity);
            javers.commit("alex", item, properties);
        }
        UserUtils userUtils = mock(UserUtils.class);
        UserLogsRepository logs = mock(UserLogsRepository.class);
        when(userUtils.getCurrentUserFridge()).thenReturn(fridge);
        when(logs.findActivityByFridge(eq(fridge), anyList(), any())).thenReturn(List.of());
        var service = new FridgeActivityService(javers, Clock.systemUTC(), userUtils, logs, mock(UserLogMapper.class));

        var activity = service.getFridgeActivity(200, 50);

        assertEquals(50, activity.size());
        assertEquals("49", activity.getFirst().getNewValue());
        assertEquals(AuditAction.ITEM_CREATED.name(), activity.getLast().getAction());
        assertEquals(49, activity.stream().filter(entry -> entry.getField() != null)
                .map(UserLogRecordDto::getNewValue).distinct().count());
    }
}
