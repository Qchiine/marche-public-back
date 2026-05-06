package com.emsi.marches_backend;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MarchesBackendApplicationTests {

	@Test
	void contextLoads() {
		// Vérifie que la classe de configuration est bien chargée
		assertNotNull(MarchesBackendApplication.class);
	}

}
