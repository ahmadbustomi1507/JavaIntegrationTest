package com.fis.app;

import com.fis.app.service.KafkaProducerService;
import com.fis.app.service.PersonService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import com.fasterxml.jackson.databind.ObjectMapper;

@ActiveProfiles("integration")
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource("classpath:application-integration.properties")
@Log4j2
public class Environment {
	@Autowired
	protected RedisTemplate<String, String> redisTemplate;
	@Autowired
	protected TestRestTemplate testRestTemplate;
	@Autowired
	protected RedisTemplate<String, String> redistemplate;
	@Autowired
	protected KafkaProducerService kafkaProducerService;

	@MockBean
	protected PersonService personService;

	@Value("${spring.kafka.topic.name}")
	protected String topic;

	@Container
	public static PostgreSQLContainer<?> postgreDBContainer = new PostgreSQLContainer<>("postgres:12.0")
			.withDatabaseName("postgres")
			.withUsername("postgres")
			.withPassword("postgres")
			.withReuse(true);

	@Container
	static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))
			.withReuse(true);


	private static final int REDIS_PORT = 6379;
	@Container
	public static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:6-alpine"))
					.withReuse(true)
					.withExposedPorts(REDIS_PORT);

	@Container
	public static MockServerContainer mockServerContainer = new MockServerContainer(
			DockerImageName.parse("jamesdbloom/mockserver:mockserver-5.11.2"))
			.withReuse(true);

	/*
	 * static block to starting container and will be used to workaround shutting
	 * down of container after each test class executed
	 */
	static {
		redis.start();
		postgreDBContainer.start();
		mockServerContainer.start();
	}
	protected String getPersonAPI() {
		return "/api/get-data";
	}
	protected String getPersonAPIThirdParty() {
		return "/api/get-data-from-third-party";
	}
	protected String getPersonRedis() {
		return "/api/get-person";
	}

	protected void cleanCache() {
		redisTemplate.getConnectionFactory().getConnection().flushAll();
	}

	protected ObjectMapper mapper() {
		return new ObjectMapper();
	}
	@DynamicPropertySource
	static void contextInitializerConfig(DynamicPropertyRegistry registry) {
		/*
		* Rest API Configuration
		* */
		registry.add("spring.datasource.url",
				() -> String.format("jdbc:postgresql://localhost:%d/postgres", postgreDBContainer.getFirstMappedPort()));
		registry.add("spring.datasource.username", () -> "postgres");
		registry.add("spring.datasource.password", () -> "postgres");

		/*
		Kafka Consumer & Producer Configuration
		 */
		registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
		// TODO - replace this with test container
		registry.add("spring.datasource.url", () -> "jdbc:h2:mem:test");
		registry.add("spring.datasource.driverClassName", () -> "org.h2.Driver");
		registry.add("spring.flyway.enabled", () -> "false");

		//For Redis
		registry.add("testcontainer.redis.host", ()-> redis.getHost());
		registry.add("testcontainer.redis.port", ()-> redis.getMappedPort(REDIS_PORT));
		registry.add("testcontainer.redis.password", ()-> "password");

		// config 3rd party url with mockserver
		registry.add("mock.server.endpoint", mockServerContainer::getEndpoint);
		registry.add("mockserver.base-url", () -> mockServerContainer.getEndpoint());

	}




}
