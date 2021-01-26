package de.caritas.cob.videoservice.api.service.session;

import de.caritas.cob.videoservice.api.authorization.AuthenticatedUser;
import de.caritas.cob.videoservice.api.exception.httpresponse.InternalServerErrorException;
import de.caritas.cob.videoservice.api.service.LogService;
import de.caritas.cob.videoservice.api.service.securityheader.SecurityHeaderSupplier;
import de.caritas.cob.videoservice.userservice.generated.web.UserControllerApi;
import de.caritas.cob.videoservice.userservice.generated.web.model.ConsultantSessionDTO;
import java.util.Arrays;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service class to provide handle session methods of the UserService.
 */
@Service
@RequiredArgsConstructor
public class SessionService {

  private static final String GET_SESSION_ERROR_MSG = "Could not get session %s for consultant %s.";
  
  private final @NonNull UserControllerApi userControllerApi;
  private final @NonNull SecurityHeaderSupplier securityHeaderSupplier;
  private final @NonNull AuthenticatedUser authenticatedUser;

  /**
   * Returns the session for the provided consultant.
   *
   * @param sessionId session Id
   * @return {@link ConsultantSessionDTO}
   */
  public ConsultantSessionDTO findSessionOfCurrentConsultant(Long sessionId) {
    addDefaultHeaders();

    try {
      return userControllerApi.fetchSessionForConsultant(sessionId);
    } catch (RestClientException ex) {

      checkIfIsClientBadRequest(sessionId, ex);

      throw new InternalServerErrorException(
          String.format(GET_SESSION_ERROR_MSG, sessionId,
              authenticatedUser.getUserId()), ex, LogService::logInternalServerError);
    }
  }

  private void addDefaultHeaders() {
    HttpHeaders headers = this.securityHeaderSupplier.getKeycloakAndCsrfHttpHeaders();
    headers.forEach((key, value) -> this.userControllerApi.getApiClient()
        .addDefaultHeader(key, value.iterator().next()));
  }

  private void checkIfIsClientBadRequest(Long sessionId, RestClientException ex) {
    if (isNoInternalServerError(ex)) {
      throw new ResponseStatusException(((HttpClientErrorException) ex).getStatusCode(),
          String.format(GET_SESSION_ERROR_MSG, sessionId,
              authenticatedUser.getUserId()));
    }
  }

  private boolean isNoInternalServerError(RestClientException ex) {
    if (ex instanceof HttpClientErrorException) {
      return Arrays
          .asList(HttpStatus.FORBIDDEN, HttpStatus.NOT_FOUND)
          .contains(((HttpClientErrorException) ex).getStatusCode());
    }

    return false;
  }
}
