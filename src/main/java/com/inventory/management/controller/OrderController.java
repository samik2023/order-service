package com.inventory.management.controller;

import com.inventory.management.entity.Order;
import com.inventory.management.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
@Slf4j
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    OrderService service;

    @PostMapping(value = "/createOrder",consumes = APPLICATION_JSON_VALUE,
            produces =APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createOrder(@RequestBody Order order){
        return service.createOrder(order);
    }

    @PostMapping(value ="/cancelOrder/{oid}")
    public ResponseEntity<String> cancelOrder(@PathVariable Long oid){
        return service.cancelOrder(oid);
    }

    @PostMapping(value ="/returnOrder/{oid}")
    public ResponseEntity<String> returnProduct(@PathVariable Long oid){
        return service.returnProduct(oid);
    }

}
