package com.inventory.management.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "PRODUCTS_JPA")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;
    private String name;

    @Enumerated(EnumType.STRING)
    private ProductType type;

    private double price;
    private long quantity;
}

