package com.inventory.management.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.management.entity.*;
import com.inventory.management.messaging.EventProducer;
import com.inventory.management.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class OrderService {

    private static ObjectMapper mapper = new ObjectMapper();
    @Autowired
    OrderRepository repository;

    @Autowired
    EventProducer producer;

    public ResponseEntity<String> createOrder(Order order){
        order.setStatus(OrderStatus.PLACED);
        repository.save(order);
            //Publish order Event
            try{
                Event event = new Event();
                event.setEventType("ORDER_CREATED");
                event.setTimeStamp(LocalDateTime.now());
                event.setCommandObjStr(mapper.writeValueAsString(order));
                event.setOrderId(order.getOrderId());
                //publish order created event
                producer.publishEvent(event,"orderBroadcastTopic");

                //publish product update event
                publishProductUpdateEvent(event);
                return ResponseEntity.ok().body(mapper.writeValueAsString(order));
            } catch (JsonProcessingException e) {
                return new ResponseEntity<>("Order creation failed", HttpStatus.NOT_FOUND);
            }
    }

    public void publishProductUpdateEvent(Event event) throws JsonProcessingException {
        event.setEventType("PRODUCT_UPDATE");
        Order ord = mapper.readValue(event.getCommandObjStr(),Order.class);
        Product product = new Product();
        product.setProductId(ord.getProductId());
        event.setCommandObjStr(mapper.writeValueAsString(product));
        producer.publishEvent(event,"productUpdateTopic");
    }

     public void finalizeOrderStatus(Event event,OrderStatus status) throws JsonProcessingException{
         Optional<Order> order = repository.findById(event.getOrderId());
         order.get().setStatus(status);
         repository.save(order.get());
         log.info("Order updated successfully !");
         event.setCommandObjStr(mapper.writeValueAsString(order));
         producer.publishEvent(event,"orderBroadcastTopic");
    }

    public void handleSagaEvents(Event event) throws JsonProcessingException {

        Optional<Order> order = repository.findById(event.getOrderId());

        if(event.getEventType().equalsIgnoreCase("PRD_UPD_FAILURE")
            || "OUT_OF_STOCK".equalsIgnoreCase(event.getEventType())){
            finalizeOrderStatus(event,OrderStatus.CANCELLED);
        }else if("PRD_RESTOCK_FAILURE".equalsIgnoreCase(event.getEventType())){
            //compensating transaction
            revertCancelOrder(event);
        }else if ("PRD_RESTOCK_SUCCESS".equalsIgnoreCase(event.getEventType())){
            finalizeOrderStatus(event,OrderStatus.CANCELLED);
        }else if("PRD_UPD_SUCCESS".equalsIgnoreCase(event.getEventType())){
                // initiate payment process
                producer.publishEvent(event,"paymentUpdateTopic");
        }else if("PAYMENT_SUCCESS".equalsIgnoreCase(event.getEventType())){
               // initiate shipping
            producer.publishEvent(event,"shipmentUpdateTopic");
        }else if("PAYMENT_FAILURE".equalsIgnoreCase(event.getEventType())
                || "PAYMENT_CANCELLED".equalsIgnoreCase(event.getEventType())){
                //rollback product update
                rollbackProductUpdate(event);
        }else if("SHIPMENT_SUCCESS".equalsIgnoreCase(event.getEventType())){
            finalizeOrderStatus(event,OrderStatus.DELIVERED);
        }else if("SHIPMENT_FAILURE".equalsIgnoreCase(event.getEventType())){
            // rollback payment
            rollbackPayment(event);
        }else if ("PAYMENT_REFUND_SUCCESS".equalsIgnoreCase(event.getEventType())){
            //initiate product restock
            rollbackProductUpdate(event);
        }else if("ORDER_RETURNED".equalsIgnoreCase(event.getEventType())){
            finalizeOrderStatus(event,OrderStatus.RETURNED);
        }
    }


    public void rollbackPayment(Event event) throws JsonProcessingException{
        Payment payment = new Payment();
        event.setEventType("CANCEL_PAYMENT");
        event.setCommandObjStr(mapper.writeValueAsString(payment));
        producer.publishEvent(event,"paymentUpdateTopic");
    }

    public void rollbackProductUpdate(Event event) throws JsonProcessingException{
        //Find product id form order
        try {
            Optional<Order> order = repository.findById(event.getOrderId());
            Product product = new Product();
            product.setProductId(order.get().getProductId());
            event.setEventType("PRODUCT_RESTOCK");
            event.setCommandObjStr(mapper.writeValueAsString(product));
            producer.publishEvent(event, "productUpdateTopic");
        }
        catch(Exception e){
            //rollback failed
            e.getMessage();
        }
    }



    public ResponseEntity<String> returnProduct(Long oid)  {
        Optional<Order> order = repository.findById(Long.valueOf(oid));
        if (order.isPresent() && order.get().getStatus().equals(OrderStatus.DELIVERED)){

            //initiate product inventory update
            Event event = new Event();
            event.setEventType("ORDER_RETURNED");
            event.setOrderId(oid);
            try {
                rollbackProductUpdate(event);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return new ResponseEntity<>("Order return started", HttpStatus.OK);
    }

    public ResponseEntity<String> cancelOrder(Long oid) {

        Optional<Order> order = repository.findById(Long.valueOf(oid));
        if (order.isPresent()) {
            order.get().setStatus(OrderStatus.CANCELLED);
        }
        repository.save(order.get());
        log.info("Order cancelled successfully !");
        Event productRestock =new Event();
        productRestock.setEventType("PRODUCT_RESTOCK");
        productRestock.setOrderId(oid);
        Product product=new Product();
        product.setProductId(order.get().getProductId());
        try {
            productRestock.setCommandObjStr(mapper.writeValueAsString(product));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        initiateRestockEvent(productRestock);
        return new ResponseEntity<>("Order cancellation started", HttpStatus.OK);
    }

    public void initiateRestockEvent(Event event){
        producer.publishEvent(event,"productUpdateTopic");
    }

    //compensating transaction
    public void revertCancelOrder(Event event){
        if("PRODUCT_UPDATE_FAILED".equalsIgnoreCase(event.getEventType())){
            // publish revert order event
            Optional<Order> order = repository.findById(event.getOrderId());
            order.get().setStatus(OrderStatus.PLACED);
            repository.save(order.get());
            log.info("Order updated successfully !");
            producer.publishEvent(event,"orderTopic");
        }
    }
}