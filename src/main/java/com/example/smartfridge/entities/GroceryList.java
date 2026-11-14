package com.example.smartfridge.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.javers.core.metamodel.annotation.DiffIgnore;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "grocery_list")
public class GroceryList {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @Column
    private String name;
    private String description;
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(
            mappedBy = "groceryList",
            cascade = CascadeType.REMOVE,
            orphanRemoval = true
    )
    @DiffIgnore
    private List<GroceryItem> items;

    @ManyToOne
    @JoinColumn(name = "fridge_id")
    @DiffIgnore
    private Fridge fridge;
    @ManyToOne
    @JoinColumn(name = "user_id")
    @DiffIgnore
    private User user;

}
