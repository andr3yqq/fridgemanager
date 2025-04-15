package com.example.smartfridge.repositories;

import com.example.smartfridge.entities.FridgeInvites;
import com.example.smartfridge.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FridgeInvitesRepository extends JpaRepository<FridgeInvites, Long> {
    List<FridgeInvites> findAllByInvitedUser(User invitedUser);
}
