package com.example.smartfridge.repositories;

import com.example.smartfridge.entities.UserLogRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserLogsRepository extends JpaRepository<UserLogRecord, Long> {
}
