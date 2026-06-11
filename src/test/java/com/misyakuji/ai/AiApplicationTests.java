package com.misyakuji.ai;

import com.misyakuji.ai.config.TestBeanConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestBeanConfig.class)
class AiApplicationTests {

	@Test
	void contextLoads() {
	}

}
