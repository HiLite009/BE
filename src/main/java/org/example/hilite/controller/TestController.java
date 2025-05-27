package org.example.hilite.controller;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ApiResponse(responseCode = "200", description = "OK")
public class TestController {

  private static final Logger logger = LoggerFactory.getLogger(TestController.class);

  @GetMapping("/test")
  public String test() {
    logger.info("Hello World!");
    logger.debug("Hello World!");
    return "Hello World!";
  }
}
