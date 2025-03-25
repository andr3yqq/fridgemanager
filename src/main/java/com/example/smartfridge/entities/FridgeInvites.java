package com.example.smartfridge.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "fridge_invites")
public class FridgeInvites {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @Column
    private String status;
    @ManyToOne
    @JoinColumn(name = "invited_user_id")
    private User invitedUser;
    @ManyToOne
    @JoinColumn(name = "fridge_id")
    private Fridge fridgeId;
}
