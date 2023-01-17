package com.fis.app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.log4j.Log4j2;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.rules.TestName;
import org.mockserver.client.MockServerClient;
import org.mockserver.matchers.Times;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.HttpStatusCode;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fis.app.Environment;
import com.fis.app.dto.PersonDto;
import com.fis.app.dto.PersonRequestDto;
import io.qameta.allure.Description;
import io.qameta.allure.Allure;
@DisplayName("Person API Integration Test")
@Log4j2
public class PersonIT extends Environment {

	//mvn test -Dtest=PersonIT#personAPIThirdPartyNegativeTest

	@Autowired
	private TestRestTemplate testRestTemplate;
	private String getPersonAPI() {
		return "/api/get-data";
	}

	/**
	 * TEST POSITIVE CASE /api/get-data
	 * 
	 * @param no
	 * @param testName
	 * @param request
	 * @param httpStatus
	 * @param response
	 * @throws JsonMappingException
	 * @throws JsonProcessingException
	 */
	@Order(1)
	@Sql(value = { "classpath:db/personDataIT.sql" }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
	@ParameterizedTest(name = "[{index}] {0} {1}")
	@CsvFileSource(resources = "/files/personTestCase.csv", numLinesToSkip = 1, delimiter = ';')
	@Description("before method description")
	public void personAPIPositiveTest(String no, String testName, String request, Integer httpStatus, String response) throws JsonMappingException, JsonProcessingException {
		Allure.description(testName);
		log.info("my request " + request);
		PersonRequestDto personRequest = this.mapper().readValue(request, PersonRequestDto.class);
		PersonDto personResponseExpected = this.mapper().readValue(response, PersonDto.class);
		log.info("send request");
		ResponseEntity<PersonDto> responseApi = testRestTemplate.postForEntity(this.getPersonAPI(), personRequest, PersonDto.class);
		log.info("testing " + responseApi.toString());
		log.info("testing " + responseApi.getBody());
		log.info("success send request");
		assertEquals(httpStatus, responseApi.getStatusCode().value());
		assertEquals(personResponseExpected.getEmail(), responseApi.getBody().getEmail());
		assertEquals(personResponseExpected.getName(), responseApi.getBody().getName());
	}

	@Order(2)
	@Sql(value = { "classpath:db/personDataIT.sql" }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
	@ParameterizedTest
	@CsvFileSource(resources = "/files/personNegativeTestCase.csv", numLinesToSkip = 1, delimiter = ';')
	public void personAPINegativeTest(String no, String testName, String request, Integer httpStatus, String response) throws JsonMappingException, JsonProcessingException {
		log.info("Executing Test : "+no + " " +testName );
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		log.info("Request:\n"	+request);
		ResponseEntity<String> responseApi = testRestTemplate.postForEntity(this.getPersonAPI(), request, String.class);

		String res = ow.writeValueAsString(responseApi);
		log.info("Response :\n"	+responseApi);

		assertEquals(httpStatus, responseApi.getStatusCode().value());
		assertEquals(response,responseApi.getBody().toString());
	}

	@Order(3)
	@ParameterizedTest
	@CsvFileSource(resources = "/files/personThirdPartyTestCase.csv", numLinesToSkip = 1, delimiter = ';')
	public void personAPIThirdPartyTest(String no, String testName, String request, Integer httpStatus, String response, String mockExpected, Integer mockQueryparam) throws JsonMappingException, JsonProcessingException {
		log.info("Executing Test : "+no);

		PersonRequestDto personRequest = this.mapper().readValue(request, PersonRequestDto.class);
		PersonDto personResponseExpected = this.mapper().readValue(response, PersonDto.class);
		
		String name=mockQueryparam+"";
		String path = "/api/v1/users/"+name;

		MockServerClient mockServerClient = new MockServerClient(mockServerContainer.getHost(), mockServerContainer.getServerPort());
		mockServerClient.when(HttpRequest
	                .request()
	                .withPath(path)
	                .withMethod(HttpMethod.GET.name()), Times.exactly(1))
	                .respond(HttpResponse
	                        .response()
	                        .withBody(mockExpected)
	                        .withHeaders(new Header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString()) )
	                        .withStatusCode(HttpStatusCode.OK_200.code()));

		ResponseEntity<PersonDto> responseApi = testRestTemplate.postForEntity(this.getPersonAPIThirdParty(), personRequest, PersonDto.class);

		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String req = ow.writeValueAsString(personRequest);
		String res = ow.writeValueAsString(responseApi);
		log.info("Request: "+req);
		log.info("Response : "+res);

		assertEquals(httpStatus, responseApi.getStatusCode().value());
		assertEquals(personResponseExpected.getEmail(), responseApi.getBody().getEmail());
		assertEquals(personResponseExpected.getName(), responseApi.getBody().getName());

	}
	@Order(4)
	@ParameterizedTest
	@CsvFileSource(resources = "/files/personThirdPartyNegativeTestCase.csv", numLinesToSkip = 1, delimiter = ';')
	public void personAPIThirdPartyNegativeTest(String no, String testName, String request,
												Integer httpStatus, String response,
												String mockExpected,
												Integer mockQueryparam,
												Integer mockStatusCode) throws InterruptedException,JsonMappingException, JsonProcessingException {
		PersonRequestDto personRequest = this.mapper().readValue(request, PersonRequestDto.class);
		String path = "/api/v1/users/"+mockQueryparam;
		log.info("Create api with endpoint " + path);
		MockServerClient mockServerClient = new MockServerClient(mockServerContainer.getHost(), mockServerContainer.getServerPort());
		mockServerClient.when(HttpRequest
						.request()
						.withPath(path)
						.withMethod(HttpMethod.GET.name()), Times.exactly(1))
				.respond(HttpResponse
						.response()
						.withBody(mockExpected)
						.withHeaders(new Header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString()) )
						.withStatusCode(mockStatusCode));

		ResponseEntity<String> responseApi = testRestTemplate.postForEntity(this.getPersonAPIThirdParty(), personRequest, String.class);

		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String req = ow.writeValueAsString(personRequest);
		log.info("Request: \n"+req);

		log.info("Response : \n"+responseApi);
		assertEquals(httpStatus, responseApi.getStatusCode().value());
		assertEquals(response,responseApi.getBody().toString());
	}

}
