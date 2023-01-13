package com.fis.app.service;

import com.fis.app.dto.PersonDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaProducerService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;


    @Value("${spring.kafka.replication.factor:1}")
    private int replicationFactor;

    @Value("${spring.kafka.partition.number:1}")
    private int partitionNumber;


    public void writeToKafka(String topic, String message) {
        try {
            log.info("Sending message to kafka topic {} ", topic);
            kafkaTemplate.send(topic, message);
            log.info("Success send message to kafka topic {} ", topic);
        } catch (Exception e) {
            log.error("Error send message to kafka topic {} ", topic);
        }

    }

}
