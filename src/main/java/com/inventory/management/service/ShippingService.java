package com.inventory.management.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.management.entity.Event;
import com.inventory.management.entity.Shipping;
import com.inventory.management.messaging.EventProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ShippingService {

    private static ObjectMapper mapper = new ObjectMapper();

    @Autowired
    EventProducer producer ;

    public void handleShipment(Event event) {


    }

        public void updateShippingStatus(Event event) {

            event.setEventType("SHIPMENT_COMPLETED");
            Event shippingEvent = new Event();
            shippingEvent.setOrderId(event.getOrderId());
            Shipping shipping = new Shipping();
            shipping.setOrderId(event.getOrderId());
            shippingEvent.setEventType("SHIPMENT_SUCCESS");
            try {
                shippingEvent.setCommandObjStr(mapper.writeValueAsString(shipping));
            } catch (Exception e) {
                shippingEvent.setEventType("SHIPMENT_FAILURE");
            }
            producer.publishEvent(shippingEvent, "orderUpdateTopic");
        }
    }

