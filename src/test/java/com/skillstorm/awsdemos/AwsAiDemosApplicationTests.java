package com.skillstorm.awsdemos;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/** Smoke test: fails if the Spring context (all beans, including the AWS clients) can't start. */
@SpringBootTest
class AwsAiDemosApplicationTests {

    @Test
    void contextLoads() {
    }
}
