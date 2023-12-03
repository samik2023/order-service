package com.inventory.management.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.management.entity.Event;
import com.inventory.management.entity.Payment;
import com.inventory.management.messaging.EventProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private static ObjectMapper mapper = new ObjectMapper();

    @Autowired
    EventProducer producer ;

    public void initiatePayment(Event event){

            if("CANCEL_PAYMENT".equalsIgnoreCase(event.getEventType())){
                cancelPayment(event);
            }
            Event paymentEvent = new Event();
            paymentEvent.setOrderId(event.getOrderId());
            Payment payment =new Payment();
            payment.setPaymentType("CARD");
            try {
                paymentEvent.setCommandObjStr(mapper.writeValueAsString(payment));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            paymentEvent.setEventType("PAYMENT_SUCCESS");
            producer.publishEvent(paymentEvent,"orderUpdateTopic");
    }

    public void cancelPayment(Event event) {

        event.setEventType("PAYMENT_CANCELLED");
        producer.publishEvent(event,"orderUpdateTopic");
    }
}
