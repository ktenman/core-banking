package com.tuum.banking;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(initializers = IntegrationTest.Initializer.class)
@Sql(scripts = "/cleanup_database_before_test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public @interface IntegrationTest {
	class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		private static final String CUSTOM_USERNAME = "user";
		private static final String CUSTOM_PASSWORD = "something";
		
		private static final PostgreSQLContainer<?> POSTGRES_DB_CONTAINER =
				new PostgreSQLContainer<>("postgres:16.2-alpine");
		
		private static final GenericContainer<?> REDIS_CONTAINER =
				new GenericContainer<>(DockerImageName.parse("redis:7.2-alpine"))
						.withExposedPorts(6379)
						.withCommand("redis-server", "--requirepass", CUSTOM_PASSWORD);
		
		private static final DockerImageName RABBITMQ_IMAGE =
				DockerImageName.parse("rabbitmq:3.9-management-alpine");
		
		private static final GenericContainer<?> RABBIT_MQ_CONTAINER =
				new GenericContainer<>(RABBITMQ_IMAGE)
						.withExposedPorts(5672, 15672)
						.withEnv("RABBITMQ_DEFAULT_USER", CUSTOM_USERNAME)
						.withEnv("RABBITMQ_DEFAULT_PASS", CUSTOM_PASSWORD);
		
		static {
			REDIS_CONTAINER.start();
			POSTGRES_DB_CONTAINER.start();
			RABBIT_MQ_CONTAINER.start();
		}
		
		@Override
		public void initialize(ConfigurableApplicationContext applicationContext) {
			TestPropertyValues.of(
					"spring.data.redis.host=" + REDIS_CONTAINER.getHost(),
					"spring.data.redis.port=" + REDIS_CONTAINER.getFirstMappedPort(),
					"spring.data.redis.password=" + CUSTOM_PASSWORD,
					"spring.datasource.url=" + POSTGRES_DB_CONTAINER.getJdbcUrl(),
					"spring.datasource.username=" + POSTGRES_DB_CONTAINER.getUsername(),
					"spring.datasource.password=" + POSTGRES_DB_CONTAINER.getPassword(),
					"spring.rabbitmq.host=" + RABBIT_MQ_CONTAINER.getHost(),
					"spring.rabbitmq.port=" + RABBIT_MQ_CONTAINER.getMappedPort(5672),
					"spring.rabbitmq.username=" + CUSTOM_USERNAME,
					"spring.rabbitmq.password=" + CUSTOM_PASSWORD
			).applyTo(applicationContext.getEnvironment());
		}
	}
}
