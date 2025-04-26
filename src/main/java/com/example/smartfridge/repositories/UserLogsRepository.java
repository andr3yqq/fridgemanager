package com.example.smartfridge.repositories;

import com.example.smartfridge.entities.Fridge;
import com.example.smartfridge.entities.User;
import com.example.smartfridge.entities.UserLogRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserLogsRepository extends JpaRepository<UserLogRecord, Long> {
    List<UserLogRecord> findAllByUser(User user);
    List<UserLogRecord> findAllByFridgeId(Fridge fridgeId);
}
