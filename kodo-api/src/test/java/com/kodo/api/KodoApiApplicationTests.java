package com.kodo.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
@SpringBootTest(properties = {
		"spring.kafka.admin.auto-create=false"
})
class KodoApiApplicationTests {

	@Container
	static final PostgreSQLContainer postgres =
			new PostgreSQLContainer("postgres:17");

	@DynamicPropertySource
	static void configurePostgres(
			DynamicPropertyRegistry registry
	) {
		registry.add(
				"spring.datasource.url",
				postgres::getJdbcUrl
		);
		registry.add(
				"spring.datasource.username",
				postgres::getUsername
		);
		registry.add(
				"spring.datasource.password",
				postgres::getPassword
		);
	}

	@Test
	void contextLoads() {
	}
}