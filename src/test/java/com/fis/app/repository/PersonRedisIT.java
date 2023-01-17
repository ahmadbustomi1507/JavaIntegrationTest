package com.fis.app.repository;


import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fis.app.Environment;
import lombok.extern.log4j.Log4j2;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
//import com.fis.app.DemoServiceTests;
import com.fis.app.dto.PersonRequestDto;

@DisplayName("Person API Integration Test")
@Log4j2
public class PersonRedisIT extends Environment {

    ////mvn test allure:report -Dtest=PersonRedisIT#personredisPositiveTest


    @Autowired
    private TestRestTemplate testRestTemplate;
//
//    private RedisServer redisServer;
//
//    private JedisConnectionFactory connectionFactory;
//
//
//    private static GenericContainer<?> genericContainer;

    @Autowired
    private RedisTemplate<String, String> redistemplate;



//    private String getPersonAPI() {
//        return "/api/get-data";
//    }

//    private String getPersonAPIThirdParty() {
//        return "/api/get-data-from-third-party";
//    }

    private String getPersinRedis() {
        return "/api/get-person";
    }


    /**
     * TEST POSITIVE CASE /api/get-person
     *
     * @param no
     * @param testName
     * @param request
     * @param httpStatus
     * @param response
     * @throws JsonMappingException
     * @throws JsonProcessingException
     * @throws JSONException
     */

    @ParameterizedTest
    @CsvFileSource(resources = "/files/personRedisPositiveTestCase.csv", numLinesToSkip = 1, delimiter = ';')
    public void personredisPositiveTest(String no, String testName, String request, Integer httpStatus, String response,String inputid,String inputdata) throws JsonMappingException, JsonProcessingException, JSONException {
        log.info("Starting test redis");
        JSONObject object = new JSONObject(request);
        redistemplate.opsForValue().set(inputid, inputdata);
        PersonRequestDto personRequest = new PersonRequestDto();

        personRequest.setId((String)object.get("id"));


        ResponseEntity<String> responseApi = testRestTemplate.postForEntity(this.getPersinRedis(), personRequest, String.class);

        assertEquals(httpStatus, responseApi.getStatusCode().value());
        assertEquals(response , responseApi.getBody());
        log.info("End test redis");
    }


}
