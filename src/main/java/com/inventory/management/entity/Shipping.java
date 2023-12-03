package com.inventory.management.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class Shipping {

    private String shippmentId;
    private Long orderId;
    private String address;
    private String status;
}
