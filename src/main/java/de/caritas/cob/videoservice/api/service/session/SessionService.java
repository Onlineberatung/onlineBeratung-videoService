package de.caritas.cob.videoservice.api.service.session;


import de.caritas.cob.videoservice.api.service.TenantHeaderSupplier;
import de.caritas.cob.videoservice.api.service.securityheader.SecurityHeaderSupplier;
import de.caritas.cob.videoservice.userservice.generated.ApiClient;
import de.caritas.cob.videoservice.userservice.generated.web.model.ConsultantSessionDTO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

/**
 * Service class to provide handle session methods of the UserService.
 */
@Service
@RequiredArgsConstructor
public class SessionService {

  private final @NonNull UserServiceApiControllerFactory userControllerApiControllerFactory;
  private final @NonNull SecurityHeaderSupplier securityHeaderSupplier;
  private final @NonNull TenantHeaderSupplier tenantHeaderSupplier;

  /**
   * Returns the session for the provided consultant.
   *
   * @param sessionId session Id
   * @return {@link ConsultantSessionDTO}
   */
  public ConsultantSessionDTO findSessionOfCurrentConsultant(Long sessionId) {
    var userControllerApi = userControllerApiControllerFactory.createControllerApi();
    addDefaultHeaders(userControllerApi.getApiClient());

    return userControllerApi.fetchSessionForConsultant(sessionId);
  }

  private void addDefaultHeaders(ApiClient apiClient) {
    HttpHeaders headers = this.securityHeaderSupplier.getKeycloakAndCsrfHttpHeaders();
    tenantHeaderSupplier.addTenantHeader(headers);
    headers.forEach(
        (key, value) ->
            apiClient.addDefaultHeader(key, value.iterator().next()));
  }
}
