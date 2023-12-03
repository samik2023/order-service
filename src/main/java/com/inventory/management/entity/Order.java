package com.inventory.management.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="ORDERS_JPA")
public class Order{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long orderId;
    private Long userId;
    private Long productId;
    private String userName;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

}

