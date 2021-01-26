package de.caritas.cob.videoservice.api.config.apiclient;

import de.caritas.cob.videoservice.messageservice.generated.ApiClient;
import de.caritas.cob.videoservice.messageservice.generated.web.MessageControllerApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration beans for the generated MessageService API client.
 */
@Configuration
public class MessageServiceApiClientConfig {

  @Value("${message.service.api.url}")
  private String messageServiceApiUrl;

  /**
   * MessageControllerApi bean.
   *
   * @param messageServiceApiClient {@link ApiClient}
   * @return {@link MessageControllerApi}
   */
  @Bean
  public MessageControllerApi messageControllerApi(ApiClient messageServiceApiClient) {
    return new MessageControllerApi(messageServiceApiClient);
  }

  /**
   * ApiClient bean.
   *
   * @param restTemplate {@link RestTemplate}
   * @return {@link ApiClient}
   */
  @Bean
  @Primary
  public ApiClient messageServiceApiClient(RestTemplate restTemplate) {
    return new ApiClient(restTemplate).setBasePath(this.messageServiceApiUrl);
  }

}
