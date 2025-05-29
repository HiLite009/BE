package org.example.hilite;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource("classpath:application-test.yml")
class HiliteApplicationTests {

  @Test
  void contextLoads() {}

  @Value("${jwt.secret}")
  private String jwtSecret;

  @Test
  void checkJwtSecret() {
    System.out.println("🔥 Loaded JWT Secret from application-test.yml: " + jwtSecret);
//    assert jwtSecret.equals("test-jwt-secret-for-testing");
  }
}
