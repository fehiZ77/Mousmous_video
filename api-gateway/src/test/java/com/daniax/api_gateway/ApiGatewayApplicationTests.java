package com.daniax.api_gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
    classes = ApiGatewayApplication.class,
    properties = {
        "spring.cloud.bootstrap.enabled=false",
        "spring.main.web-application-type=reactive"
    }
)
@ActiveProfiles("test")
class ApiGatewayApplicationTests {

    @Test
    void contextLoads() {
    }
}
