package com.inventory.management.messaging;

import com.inventory.management.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class EventProducer {


    @Autowired
    private KafkaTemplate<String, Event> kafkaTemplate;

    public void publishEvent(Event event,String topicName) {

        kafkaTemplate.send(topicName,event);
    }

}
