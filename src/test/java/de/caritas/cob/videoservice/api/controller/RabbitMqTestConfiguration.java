package de.caritas.cob.videoservice.api.controller;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class RabbitMqTestConfiguration {

  @Bean
  public HealthIndicator rabbitHealthIndicator() {
    return () -> Health.up().withDetail("version", "mock").build();
  }
}
