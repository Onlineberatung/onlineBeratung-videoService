package de.caritas.cob.videoservice.api.service.session;

import de.caritas.cob.videoservice.api.service.securityheader.SecurityHeaderSupplier;
import de.caritas.cob.videoservice.userservice.generated.web.UserControllerApi;
import de.caritas.cob.videoservice.userservice.generated.web.model.ConsultantSessionDTO;
import java.util.Collections;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Service class to provide handle session methods of the UserService.
 */
@Service
@RequiredArgsConstructor
public class SessionService {

  private final @NonNull UserControllerApi userControllerApi;
  private final @NonNull SecurityHeaderSupplier securityHeaderSupplier;

  /**
   * Returns the session for the provided consultant.
   *
   * @param sessionId session Id
   * @return {@link ConsultantSessionDTO}
   */
  public ConsultantSessionDTO findSessionOfCurrentConsultant(Long sessionId) {
    addDefaultHeaders();

    return userControllerApi.fetchSessionForConsultant(sessionId);
  }

  private void addDefaultHeaders() {
    HttpHeaders headers = this.securityHeaderSupplier.getKeycloakAndCsrfHttpHeaders();
    addOriginHeader(headers);
    headers.forEach((key, value) -> this.userControllerApi.getApiClient()
        .addDefaultHeader(key, value.iterator().next()));
  }

  private void addOriginHeader(HttpHeaders headers) {
    String originHeaderValue = getOriginHeaderValue();
    if (originHeaderValue != null) {
      headers.add("origin", originHeaderValue);
    }
  }

  private String getOriginHeaderValue() {

    HttpServletRequest request =
        ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
            .getRequest();

    return Collections.list(request.getHeaderNames())
        .stream()
        .collect(Collectors.toMap(h -> h, request::getHeader)).get("host");
  }



}
