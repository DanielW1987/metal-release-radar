package com.metalr2;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Profile("test")
class MetalReleaseRadarApplicationIT {

  @Test
  void contextLoads() {
    // Simple test to check if the Spring Application context can be loaded successfully
  }

}
