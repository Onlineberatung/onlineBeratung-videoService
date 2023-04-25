package de.caritas.cob.videoservice.config.apiclient;

import de.caritas.cob.videoservice.liveservice.generated.ApiClient;
import de.caritas.cob.videoservice.liveservice.generated.web.LiveControllerApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

/** Configuration beans for the generated LiveService API client. */
@Configuration
public class LiveServiceApiClientConfig {

  @Value("${live.service.api.url}")
  private String liveServiceApiUrl;

  /**
   * LiveControllerApi bean.
   *
   * @param liveServiceApiClient {@link ApiClient}
   * @return {@link LiveControllerApi}
   */
  @Bean
  public LiveControllerApi liveControllerApi(ApiClient liveServiceApiClient) {
    return new LiveControllerApi(liveServiceApiClient);
  }

  /**
   * ApiClient bean.
   *
   * @param restTemplate {@link RestTemplate}
   * @return {@link ApiClient}
   */
  @Bean
  @Primary
  public ApiClient liveServiceApiClient(RestTemplate restTemplate) {
    return new ApiClient(restTemplate).setBasePath(this.liveServiceApiUrl);
  }
}
