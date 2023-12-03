package com.inventory.management.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Payment {

    private String paymentId;
    private String orderId;
    private String paymentType;
    private String payer;
}
