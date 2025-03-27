package com.example.smartfridge.repositories;

import com.example.smartfridge.entities.FridgeInvites;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FridgeInvitesRepository extends JpaRepository<FridgeInvites, Long> {
}
