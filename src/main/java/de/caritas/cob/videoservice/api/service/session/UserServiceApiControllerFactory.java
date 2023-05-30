package de.caritas.cob.videoservice.api.service.session;

import de.caritas.cob.videoservice.userservice.generated.web.UserControllerApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UserServiceApiControllerFactory {

  @Value("${user.service.api.url}")
  private String userServiceApiUrl;

  @Autowired private RestTemplate restTemplate;

  public UserControllerApi createControllerApi() {
    var apiClient =
        new de.caritas.cob.videoservice.userservice.generated.ApiClient(restTemplate)
            .setBasePath(this.userServiceApiUrl);
    return new UserControllerApi(apiClient);
  }
}
