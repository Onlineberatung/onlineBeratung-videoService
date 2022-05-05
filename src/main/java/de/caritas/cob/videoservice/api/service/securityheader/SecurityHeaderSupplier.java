package de.caritas.cob.videoservice.api.service.securityheader;

import de.caritas.cob.videoservice.api.authorization.VideoUser;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityHeaderSupplier {

  private final @NonNull VideoUser authenticatedUser;

  @Value("${csrf.header.property}")
  private String csrfHeaderProperty;

  @Value("${csrf.cookie.property}")
  private String csrfCookieProperty;

  /**
   * Creates the headers containing keycloak token and csrf headers {@link HttpHeaders} object.
   *
   * @return the created {@link HttpHeaders}
   */
  public HttpHeaders getKeycloakAndCsrfHttpHeaders() {
    var header = getCsrfHttpHeaders();
    this.addKeycloakAuthorizationHeader(header);

    return header;
  }

  private HttpHeaders getCsrfHttpHeaders() {
    var httpHeaders = new HttpHeaders();

    return this.addCsrfValues(httpHeaders);
  }

  private HttpHeaders addCsrfValues(HttpHeaders httpHeaders) {
    var csrfToken = UUID.randomUUID().toString();

    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    httpHeaders.add("Cookie", csrfCookieProperty + "=" + csrfToken);
    httpHeaders.add(csrfHeaderProperty, csrfToken);

    return httpHeaders;
  }

  private void addKeycloakAuthorizationHeader(HttpHeaders httpHeaders) {
    httpHeaders.add("Authorization", "Bearer " + authenticatedUser.getAccessToken());
  }
}
