package com.guzem.uzaktan;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Requires SQL Server database connection — use @WebMvcTest for web layer tests instead")
class UzaktanApplicationTests {

	@Test
	void contextLoads() {
	}

}
