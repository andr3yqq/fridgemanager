package com.example.smartfridge.services;

import com.example.smartfridge.config.FridgeCommitPropertiesProvider;
import com.example.smartfridge.dtos.UserLogRecordDto;
import com.example.smartfridge.entities.*;
import com.example.smartfridge.enums.AuditAction;
import com.example.smartfridge.enums.AuditEntityType;
import com.example.smartfridge.mappers.UserLogMapper;
import com.example.smartfridge.repositories.UserLogsRepository;
import com.example.smartfridge.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.javers.core.Javers;
import org.javers.core.commit.CommitMetadata;
import org.javers.core.diff.Change;
import org.javers.core.diff.changetype.*;
import org.javers.core.metamodel.object.GlobalId;
import org.javers.repository.jql.QueryBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FridgeActivityService {
    private static final int DEFAULT_PAGE_SIZE = 50;
    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_OFFSET = 10_000;
    private static final int SNAPSHOT_PAGE_SIZE = 200;
    private static final List<AuditEntityType> JAVERS_ENTITY_TYPES = List.of(
            AuditEntityType.FRIDGE_ITEM, AuditEntityType.GROCERY_ITEM, AuditEntityType.GROCERY_LIST);
    private static final Map<AuditEntityType, Set<String>> AUDITED_FIELDS = Map.of(
            AuditEntityType.FRIDGE_ITEM, Set.of("name", "description", "quantity", "category", "price",
                    "expirationDate", "buyingDate"),
            AuditEntityType.GROCERY_ITEM, Set.of("name", "description", "quantity", "category", "purchased"),
            AuditEntityType.GROCERY_LIST, Set.of("name", "description"));

    private final Javers javers;
    private final Clock clock;
    private final UserUtils userUtils;
    private final UserLogsRepository userLogsRepository;
    private final UserLogMapper userLogMapper;

    @Transactional(readOnly = true)
    public List<UserLogRecordDto> getFridgeActivity() {
        return getFridgeActivity(0, DEFAULT_PAGE_SIZE);
    }

    @Transactional(readOnly = true)
    public List<UserLogRecordDto> getFridgeActivity(int offset, int limit) {
        int safeOffset = Math.clamp(offset, 0, MAX_OFFSET);
        int safeLimit = Math.clamp(limit, 1, MAX_PAGE_SIZE);
        int fetchCount = safeOffset + safeLimit;
        Fridge fridge = userUtils.getCurrentUserFridge();
        Long fridgeId = fridge.getId();

        List<UserLogRecordDto> activity = new ArrayList<>(loadJaVersActivity(fridgeId, fetchCount));

        userLogsRepository.findActivityByFridge(fridge, JAVERS_ENTITY_TYPES, PageRequest.of(0, fetchCount)).stream()
                .map(userLogMapper::toUserLogRecordDto)
                .forEach(activity::add);

        return activity.stream()
                .sorted(Comparator.comparing(UserLogRecordDto::getTimestamp,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .skip(safeOffset)
                .limit(safeLimit)
                .toList();
    }

    private List<UserLogRecordDto> loadJaVersActivity(Long fridgeId, int required) {
        int snapshotOffset = 0;
        List<UserLogRecordDto> activity = new ArrayList<>();
        while (true) {
            var query = QueryBuilder.byClass(ItemRecord.class, GroceryItem.class, GroceryList.class)
                    .withCommitProperty(FridgeCommitPropertiesProvider.FRIDGE_ID, fridgeId.toString())
                    .skip(snapshotOffset)
                    .limit(SNAPSHOT_PAGE_SIZE)
                    .build();
            activity.addAll(toJaVersActivities(javers.findChanges(query), fridgeId));
            if (activity.size() >= required || javers.findSnapshots(query).size() < SNAPSHOT_PAGE_SIZE) {
                return activity;
            }
            snapshotOffset += SNAPSHOT_PAGE_SIZE;
        }
    }

    private List<UserLogRecordDto> toJaVersActivities(List<Change> changes, Long fridgeId) {
        Map<ChangeKey, String> namesByChange = namesByChange(changes);
        Map<String, Set<GlobalId>> entitiesByCommit = entitiesByCommit(changes);
        return changes.stream()
                .map(change -> toJaVersActivity(change, fridgeId, namesByChange, entitiesByCommit))
                .filter(Objects::nonNull)
                .toList();
    }

    public void recordUserLog(User user, AuditAction action, AuditEntityType type, Long entityId,
                              String entityName, Map<String, String> details) {
        Objects.requireNonNull(user, "Actor is required to record fridge activity");
        Fridge fridge = user.getFridge();
        if (fridge == null) {
            throw new IllegalStateException("Cannot record fridge activity for a user without a fridge");
        }
        saveUserLog(user, action, type, fridge, entityId, entityName, details);
    }

    public void recordUserLog(User user, AuditAction action, AuditEntityType type, Fridge fridge,
                              Long entityId, String entityName, Map<String, String> details) {
        Objects.requireNonNull(user, "Actor is required to record fridge activity");
        Objects.requireNonNull(fridge, "Fridge is required to record fridge activity");
        saveUserLog(user, action, type, fridge, entityId, entityName, details);
    }

    private void saveUserLog(User user, AuditAction action, AuditEntityType type, Fridge fridge,
                             Long entityId, String entityName, Map<String, String> details) {
        UserLogRecord userLogRecord = new UserLogRecord();
        userLogRecord.setAction(action);
        userLogRecord.setUser(user);
        userLogRecord.setFridge(fridge);
        userLogRecord.setEntityType(type);
        userLogRecord.setEntityId(entityId);
        userLogRecord.setEntityName(entityName);
        userLogRecord.setDetails(details);
        userLogRecord.setTimestamp(Instant.now(clock));
        userLogsRepository.save(userLogRecord);
    }

    private UserLogRecordDto toJaVersActivity(Change change, Long fridgeId,
                                              Map<ChangeKey, String> namesByChange,
                                              Map<String, Set<GlobalId>> entitiesByCommit) {
        if (change instanceof InitialValueChange || change instanceof TerminalValueChange) {
            return null;
        }

        String field = null;
        String oldValue = null;
        String newValue = null;
        if (change instanceof ValueChange valueChange) {
            field = valueChange.getPropertyName();
            oldValue = Objects.toString(valueChange.getLeft(), null);
            newValue = Objects.toString(valueChange.getRight(), null);
        } else if (change instanceof PropertyChange<?> propertyChange) {
            field = propertyChange.getPropertyName();
            oldValue = Objects.toString(propertyChange.getLeft(), null);
            newValue = Objects.toString(propertyChange.getRight(), null);
        } else if (!(change instanceof NewObject || change instanceof ObjectRemoved)) {
            return null;
        }

        Optional<CommitMetadata> metadata = change.getCommitMetadata();
        AuditEntityType entityType = entityTypeFor(change);
        if (entityType == null) {
            return null;
        }
        if (field != null && !AUDITED_FIELDS.getOrDefault(entityType, Set.of()).contains(field)) {
            return null;
        }
        boolean created = change instanceof NewObject;
        boolean deleted = change instanceof ObjectRemoved;
        String action = resolveAction(entityType, created, deleted);
        Long entityId = change.getAffectedLocalId() instanceof Number number ? number.longValue() : null;
        UserLogRecordDto activity = new UserLogRecordDto();
        activity.setUsername(metadata.map(CommitMetadata::getAuthor).orElse(null));
        activity.setTimestamp(metadata.map(CommitMetadata::getCommitDateInstant).orElse(null));
        activity.setAction(action);
        activity.setEntityType(entityType.name());
        activity.setEntityId(entityId);
        activity.setEntityName(resolveEntityName(change, metadata, namesByChange, entitiesByCommit));
        activity.setFridgeId(fridgeId);
        activity.setField(field);
        activity.setOldValue(oldValue);
        activity.setNewValue(newValue);
        activity.setDetails(Map.of());
        return activity;
    }

    private Map<ChangeKey, String> namesByChange(List<Change> changes) {
        Map<ChangeKey, String> names = new HashMap<>();
        for (Change change : changes) {
            if (!(change instanceof ValueChange valueChange)
                    || !"name".equals(valueChange.getPropertyName())) {
                continue;
            }
            String name = Objects.toString(valueChange.getRight(), Objects.toString(valueChange.getLeft(), null));
            change.getCommitMetadata().ifPresent(metadata -> names.put(
                    new ChangeKey(metadata.getId().toString(), change.getAffectedGlobalId()), name));
        }
        return names;
    }

    private Map<String, Set<GlobalId>> entitiesByCommit(List<Change> changes) {
        Map<String, Set<GlobalId>> entities = new HashMap<>();
        for (Change change : changes) {
            change.getCommitMetadata().ifPresent(metadata -> entities
                    .computeIfAbsent(metadata.getId().toString(), ignored -> new HashSet<>())
                    .add(change.getAffectedGlobalId()));
        }
        return entities;
    }

    private String resolveEntityName(Change change, Optional<CommitMetadata> metadata,
                                     Map<ChangeKey, String> namesByChange,
                                     Map<String, Set<GlobalId>> entitiesByCommit) {
        if (metadata.isEmpty()) {
            return null;
        }
        CommitMetadata commit = metadata.get();
        String commitId = commit.getId().toString();
        String name = namesByChange.get(new ChangeKey(commitId, change.getAffectedGlobalId()));
        if (name != null) {
            return name;
        }
        Set<GlobalId> affectedEntities = entitiesByCommit.getOrDefault(commitId, Set.of());
        if (affectedEntities.size() != 1) {
            return null;
        }
        return commit.getProperties().get(FridgeCommitPropertiesProvider.ENTITY_NAME);
    }

    private AuditEntityType entityTypeFor(Change change) {
        String typeName = change.getAffectedGlobalId().getTypeName();
        if (ItemRecord.class.getName().equals(typeName)) {
            return AuditEntityType.FRIDGE_ITEM;
        }
        if (GroceryItem.class.getName().equals(typeName)) {
            return AuditEntityType.GROCERY_ITEM;
        }
        if (GroceryList.class.getName().equals(typeName)) {
            return AuditEntityType.GROCERY_LIST;
        }
        return null;
    }

    private String resolveAction(AuditEntityType entityType, boolean created, boolean deleted) {
        return switch (entityType) {
            case FRIDGE_ITEM -> created ? AuditAction.ITEM_CREATED.name()
                    : deleted ? AuditAction.ITEM_DELETED.name() : AuditAction.ITEM_UPDATED.name();
            case GROCERY_ITEM -> created ? AuditAction.GROCERY_ITEM_CREATED.name()
                    : deleted ? AuditAction.GROCERY_ITEM_DELETED.name() : AuditAction.GROCERY_ITEM_UPDATED.name();
            case GROCERY_LIST -> created ? AuditAction.GROCERY_LIST_CREATED.name()
                    : deleted ? AuditAction.GROCERY_LIST_DELETED.name() : AuditAction.GROCERY_LIST_UPDATED.name();
            default -> throw new IllegalArgumentException("Unsupported JaVers activity entity: " + entityType);
        };
    }

    private record ChangeKey(String commitId, GlobalId globalId) {
    }
}
