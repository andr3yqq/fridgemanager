package com.example.smartfridge.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "fridges")
public class Fridge {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "fridge_id")
    private Long id;
    @Column
    private String name;
    @OneToOne
    @JoinColumn(name = "user_id")
    private User owner;
    @OneToMany(
            mappedBy = "fridgeId",
            cascade = CascadeType.REMOVE,
            orphanRemoval = true
    )
    private List<ItemRecord> items = new ArrayList<>();
    @OneToMany(
            mappedBy = "fridgeId",
            cascade = CascadeType.REMOVE,
            orphanRemoval = true
    )
    private List<UserLogRecord> userLogs = new ArrayList<>();
    @OneToMany(
            mappedBy = "fridge",
            cascade = CascadeType.REMOVE,
            orphanRemoval = true
    )
    private List<GroceryList> groceryLists = new ArrayList<>();


}
