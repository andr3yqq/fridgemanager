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
@Table(name = "grocery_item")
public class GroceryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @Column
    private String name;
    private String description;
    private Integer quantity;
    private String category;
    private boolean purchased = false;
    @ManyToOne
    @JoinColumn(name = "grocery_list_id")
    private GroceryList groceryList;
}
