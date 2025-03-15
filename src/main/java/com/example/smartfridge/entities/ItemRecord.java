package com.example.smartfridge.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table (name = "item_record")
public class ItemRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @Column
    private String name;
    @Column
    private String description;
    @Column
    private Integer quantity;
    @Column
    private String category;
    @Column
    private Double price;
    @Column
    private LocalDate expirationDate;
    @Column
    private LocalDate buyingDate;
    @ManyToOne
    @JoinColumn(name = "fridge_id")
    private Fridge fridgeId;
}
