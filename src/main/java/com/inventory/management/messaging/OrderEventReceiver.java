package com.inventory.management.messaging;

import com.inventory.management.entity.Event;
import com.inventory.management.entity.OrderStatus;
import com.inventory.management.service.OrderService;
import com.inventory.management.service.PaymentService;
import com.inventory.management.service.ShippingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderEventReceiver {
    private static final String TOPIC_PAYMENT = "paymentUpdateTopic";
    private static final String TOPIC_SHIPMENT = "shipmentUpdateTopic";
    private static final String TOPIC_PRODUCT_BRD = "productBroadcastTopic";
    private static final String TOPIC_ORDER_UPDATE = "orderUpdateTopic";

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ShippingService shippingService;


    @KafkaListener(topics = TOPIC_ORDER_UPDATE, groupId = "order-group", containerFactory = "tranRecordListener")
    private void listenOrderUpdates(Event event) throws Exception {
        log.info("Received message :" + event + " in " + TOPIC_ORDER_UPDATE);
        orderService.handleSagaEvents(event);
    }

    @KafkaListener(topics = TOPIC_PAYMENT, groupId = "order-group", containerFactory = "tranRecordListener")
    private void listenShipmentUpdate(Event event) throws Exception {
        log.info("Received message :" + event + " in " + TOPIC_PAYMENT);
        paymentService.initiatePayment(event);
    }

    @KafkaListener(topics = TOPIC_SHIPMENT, groupId = "order-group", containerFactory = "tranRecordListener")
    private void listenShipmentEvents(Event event) throws Exception {
        log.info("Received message :" + event + " in " + TOPIC_SHIPMENT);
        shippingService.updateShippingStatus(event);
    }

}
