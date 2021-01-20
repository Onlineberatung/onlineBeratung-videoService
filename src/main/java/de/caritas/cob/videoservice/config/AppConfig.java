package de.caritas.cob.videoservice.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Contains some general spring boot application configuration.
 */
@Configuration
@ComponentScan(basePackages = {"de.caritas.cob.videoservice"})
public class AppConfig {

  /**
   * RestTemplate Bean.
   *
   * @param builder {@link RestTemplateBuilder}
   * @return {@link RestTemplate}
   */
  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder.build();
  }
}
