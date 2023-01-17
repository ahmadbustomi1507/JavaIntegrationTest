package com.fis.app.repository;


import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.fis.app.Environment;
import lombok.extern.log4j.Log4j2;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fis.app.dto.PersonRequestDto;

@DisplayName("Person Redis Integration Tests ")
@Log4j2
public class PersonRedisIT extends Environment {

    ////mvn test allure:report -Dtest=PersonRedisIT#personredisPositiveTest
    @Autowired
    protected TestRestTemplate testRestTemplate;
    @Order(1)
    @ParameterizedTest(name = "{index} {0} - {1}")
    @CsvFileSource(resources = "/files/personRedisPositiveTestCase.csv", numLinesToSkip = 1, delimiter = ';')
    public void personredisPositiveTest(String no, String testName, String request, Integer httpStatus, String response,String inputid,String inputdata) throws JsonMappingException, JsonProcessingException, JSONException {
        log.info("Starting test redis");
        PersonRequestDto personRequest = this.mapper().readValue(request, PersonRequestDto.class);
        redistemplate.opsForValue().set(inputid, inputdata);
        ResponseEntity<String> responseApi = testRestTemplate.postForEntity(this.getPersonRedis(), personRequest, String.class);

        assertEquals(httpStatus, responseApi.getStatusCode().value());
        assertEquals(response , responseApi.getBody());


        ObjectWriter responseObject = this.mapper().writer().withDefaultPrettyPrinter();
        String res = responseObject.writeValueAsString(responseApi);
        log.info("response :\n" + res);

        log.info("End test redis");
    }


}
