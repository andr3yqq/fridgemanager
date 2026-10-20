package com.example.smartfridge.repositories;

import com.example.smartfridge.entities.Fridge;
import com.example.smartfridge.entities.UserLogRecord;
import com.example.smartfridge.enums.AuditEntityType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserLogsRepository extends JpaRepository<UserLogRecord, Long> {
    @Query("select log from UserLogRecord log left join fetch log.user " +
            "where log.fridge = :fridge and (log.entityType is null or log.entityType not in :excludedTypes) " +
            "order by log.timestamp desc")
    List<UserLogRecord> findActivityByFridge(@Param("fridge") Fridge fridge,
                                             @Param("excludedTypes") List<AuditEntityType> excludedTypes,
                                             Pageable pageable);
}
