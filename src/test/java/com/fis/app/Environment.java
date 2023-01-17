package com.fis.app;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
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

//	@LocalServerPort
//	protected int port;

	@Autowired
	private static RedisTemplate<String, String> redisTemplate;


	@Value("${spring.kafka.topic.name}")
	protected String topic;
	protected void cleanCache() {
		redisTemplate.getConnectionFactory().getConnection().flushAll();
	}
	private static final int REDIS_PORT = 6379;
//	protected StringBuilder getHost() {
//		return new StringBuilder("http://localhost:").append(port);
//	}
	public static PostgreSQLContainer<?> postgreDBContainer = new PostgreSQLContainer<>("postgres:12.0")
			.withDatabaseName("postgres")
			.withUsername("postgres")
			.withPassword("postgres")
			//.withExposedPorts(5432)
			.withReuse(true); // activated this options to faster development test on your local system

	@Container
	static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"));

	public static GenericContainer<?> redis =
			new GenericContainer<>(DockerImageName.parse("redis:6-alpine"))
					.withExposedPorts(REDIS_PORT).withReuse(true);

	public static MockServerContainer mockServerContainer = new MockServerContainer(
			DockerImageName.parse("jamesdbloom/mockserver:mockserver-5.11.2"))
			.withReuse(true);

	/*
	 * static block to starting container and will be used to workaround shutting
	 * down of container after each test class executed
	 */
	static {
		redis.start();
//		postgreDBContainer.start();
		mockServerContainer.start();
	}

	@DynamicPropertySource
	static void contextInitializerConfig(DynamicPropertyRegistry registry) {
		// For rest api test
		registry.add("spring.datasource.url",
				() -> String.format("jdbc:postgresql://localhost:%d/postgres", postgreDBContainer.getFirstMappedPort()));
		registry.add("spring.datasource.username", () -> "postgres");
		registry.add("spring.datasource.password", () -> "postgres");
		// config 3rd party url with mockserver
		registry.add("mock.server.endpoint", mockServerContainer::getEndpoint);
		registry.add("mockserver.base-url", () -> mockServerContainer.getEndpoint());

		// For kafka
		registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
		// TODO - replace this with test container
		registry.add("spring.datasource.url", () -> "jdbc:h2:mem:test");
		registry.add("spring.datasource.driverClassName", () -> "org.h2.Driver");
//		registry.add("spring.datasource.username", () -> "root");
//		registry.add("spring.datasource.password", () -> "secret");
		registry.add("spring.flyway.enabled", () -> "false");

		//For Redis
		registry.add("spring.redis.host", ()-> redis.getContainerIpAddress());
		registry.add("spring.redis.port", ()-> redis.getMappedPort(REDIS_PORT));
		registry.add("spring.redis.password", ()-> "password");

		// config 3rd party url with mockserver
//		registry.add("mock.server.endpoint", mockServerContainer::getEndpoint);
//		registry.add("mockserver.base-url", () -> mockServerContainer.getEndpoint());

	}

	protected ObjectMapper mapper() {
		return new ObjectMapper();
	}


}
