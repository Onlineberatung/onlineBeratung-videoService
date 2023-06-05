package de.caritas.cob.videoservice.config.resttemplate;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/** Contains some general spring boot application configuration. */
@Configuration
public class RestTemplateConfig {

  /**
   * RestTemplate Bean.
   *
   * @param builder {@link RestTemplateBuilder}
   * @return {@link RestTemplate}
   */
  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder.errorHandler(new CustomResponseErrorHandler()).build();
  }
}
