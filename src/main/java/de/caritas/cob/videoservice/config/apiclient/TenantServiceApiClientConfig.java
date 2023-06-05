package de.caritas.cob.videoservice.config.apiclient;

import de.caritas.cob.videoservice.tenantservice.generated.ApiClient;
import de.caritas.cob.videoservice.tenantservice.generated.web.TenantControllerApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TenantServiceApiClientConfig {

  @Value("${tenant.service.api.url}")
  private String tenantServiceApiUrl;

  @Bean
  public TenantControllerApi tenantControllerApi(ApiClient apiClient) {
    return new TenantControllerApi(apiClient);
  }

  /**
   * Api client bean definition.
   *
   * @param restTemplate rest template bean
   * @return
   */
  @Bean
  @Primary
  public ApiClient tenantApiClient(RestTemplate restTemplate) {
    ApiClient apiClient = new TenantServiceApiClient(restTemplate);
    apiClient.setBasePath(this.tenantServiceApiUrl);
    return apiClient;
  }
}
