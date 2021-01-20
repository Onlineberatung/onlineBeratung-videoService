package de.caritas.cob.videoservice.api.config.apiclient;

import de.caritas.cob.videoservice.userservice.generated.ApiClient;
import de.caritas.cob.videoservice.userservice.generated.web.UserControllerApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration beans for the generated UserService API client.
 */
@Configuration
public class UserServiceApiClientConfig {

  @Value("${user.service.api.url}")
  private String userServiceApiUrl;

  /**
   * UserControllerApi bean.
   *
   * @param userServiceApiClient {@link ApiClient}
   * @return {@link UserControllerApi}
   */
  @Bean
  public UserControllerApi userControllerApi(ApiClient userServiceApiClient) {
    return new UserControllerApi(userServiceApiClient);
  }

  /**
   * ApiClient bean.
   *
   * @param restTemplate {@link RestTemplate}
   * @return {@link ApiClient}
   */
  @Bean
  @Primary
  public ApiClient userServiceApiClient(RestTemplate restTemplate) {
    return new ApiClient(restTemplate).setBasePath(this.userServiceApiUrl);
  }
}
