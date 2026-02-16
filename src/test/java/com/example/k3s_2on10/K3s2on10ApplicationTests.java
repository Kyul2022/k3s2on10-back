package com.example.k3s_2on10;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(exclude = DataSourceAutoConfiguration.class)
class K3s2on10ApplicationTests {

	@Test
	void contextLoads() {
	}

}
