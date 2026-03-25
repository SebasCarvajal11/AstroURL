package com.astrourl.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = AstrourlApplication.class)
@ActiveProfiles("test")
class AstrourlApplicationTests {

    @Test
    void contextLoads() {
    }
}
