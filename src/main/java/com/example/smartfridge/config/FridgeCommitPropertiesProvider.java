package com.example.smartfridge.config;

import com.example.smartfridge.entities.GroceryItem;
import com.example.smartfridge.entities.GroceryList;
import com.example.smartfridge.entities.ItemRecord;
import org.javers.spring.auditable.CommitPropertiesProvider;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@Component
public class FridgeCommitPropertiesProvider implements CommitPropertiesProvider {
    public static final String FRIDGE_ID = "fridgeId";
    public static final String ENTITY_NAME = "entityName";

    @Override
    public Map<String, String> provideForCommittedObject(Object entity) {
        Long fridgeId = switch (entity) {
            case ItemRecord item -> item.getFridge() == null ? null : item.getFridge().getId();
            case GroceryList list -> list.getFridge() == null ? null : list.getFridge().getId();
            case GroceryItem item -> item.getGroceryList() == null || item.getGroceryList().getFridge() == null
                    ? null : item.getGroceryList().getFridge().getId();
            default -> null;
        };
        if (fridgeId == null) {
            return Collections.emptyMap();
        }
        String entityName = switch (entity) {
            case ItemRecord item -> item.getName();
            case GroceryList list -> list.getName();
            case GroceryItem item -> item.getName();
            default -> null;
        };
        return Map.of(
                FRIDGE_ID, fridgeId.toString(),
                ENTITY_NAME, Objects.toString(entityName, entity.getClass().getSimpleName()));
    }
}
