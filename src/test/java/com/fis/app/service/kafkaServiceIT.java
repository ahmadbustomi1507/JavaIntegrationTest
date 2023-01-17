package com.fis.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fis.app.Environment;
import com.fis.app.dto.PersonDto;
import com.fis.app.utils.JSONUtils;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@DisplayName("Kafka service integration Test")
@Log4j2
public class kafkaServiceIT extends Environment {
    // mvn test -Dtest=kafkaServiceIT#sampleKafkaProduceAndConsumeTest

    @Order(1)
    @ParameterizedTest
    @CsvFileSource(resources = "/files/sampleKafkaTest.csv", numLinesToSkip = 1, delimiter = ';')
    void sampleKafkaProduceAndConsumeTest(String no, String desc,String produceMessage,String consumeMessage)throws JsonMappingException, JsonProcessingException {

        PersonDto personDto  = this.mapper().readValue(produceMessage,PersonDto.class);
        kafkaProducerService.writeToKafka(topic, JSONUtils.convertToJson(personDto));

        ArgumentCaptor<PersonDto> captor = ArgumentCaptor.forClass(PersonDto.class);
        verify(personService, timeout(10000)).save(captor.capture());

        assertNotNull(captor.getValue());
        assertEquals(personDto.getId(), captor.getValue().getId());
        assertEquals(personDto.getName(), captor.getValue().getName());
        assertEquals(personDto.getEmail(), captor.getValue().getEmail());
    }
}
