package com.fis.app.service;

import com.fis.app.dto.PersonDto;
import com.fis.app.utils.JSONUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class KafkaConsumerService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);

    @Autowired
    private PersonService personService;

    public KafkaConsumerService(PersonService personService) {
        this.personService = personService;
    }

    @KafkaListener(topics = "${spring.kafka.topic.name}", groupId = "${spring.kafka.consumer.group-id}")
    public void logKafkaMessages(String data) {
        log.info("Incoming request message {} ",data);
        PersonDto personDto = JSONUtils.convertToObject(data, PersonDto.class);
//        personService.save(personDto);
        log.info("Success consume message {} ",data);
    }


}
