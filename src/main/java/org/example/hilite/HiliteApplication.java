package org.example.hilite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class HiliteApplication {

  public static void main(String[] args) {
    SpringApplication.run(HiliteApplication.class, args);
  }
}
