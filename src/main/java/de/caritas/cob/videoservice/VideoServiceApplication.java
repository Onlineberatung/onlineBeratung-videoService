package de.caritas.cob.videoservice;

import static java.util.Objects.isNull;

import de.caritas.cob.videoservice.api.authorization.AuthenticatedUser;
import de.caritas.cob.videoservice.api.exception.KeycloakException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Starter class for the application.
 */
@SpringBootApplication
public class VideoServiceApplication {

  private static final String CLAIM_NAME_USER_ID = "userId";
  private static final String CLAIM_NAME_USERNAME = "username";

  /**
   * Global application entry point.
   *
   * @param args possible provided args
   */
  public static void main(String[] args) {
    SpringApplication.run(VideoServiceApplication.class, args);
  }

  /**
   * Returns the currently authenticated user.
   *
   * @return {@link AuthenticatedUser}
   */
  @Bean
  @Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
  public AuthenticatedUser getAuthenticatedUser() {

    KeycloakSecurityContext keycloakSecContext =
        ((KeycloakAuthenticationToken) getRequest().getUserPrincipal()).getAccount()
            .getKeycloakSecurityContext();
    Map<String, Object> claimMap = keycloakSecContext.getToken().getOtherClaims();

    AuthenticatedUser authenticatedUser = new AuthenticatedUser();
    authenticatedUser.setAccessToken(getUserAccessToken(keycloakSecContext));
    authenticatedUser.setUserId(getUserAttribute(claimMap, CLAIM_NAME_USER_ID));
    authenticatedUser.setUsername(getUserAttribute(claimMap, CLAIM_NAME_USERNAME));

    return authenticatedUser;
  }

  private String getUserAccessToken(KeycloakSecurityContext keycloakSecContext) {
    if (isNull(keycloakSecContext.getTokenString())) {
      throw new KeycloakException("No valid Keycloak access token string found.");
    }

    return keycloakSecContext.getTokenString();
  }

  private String getUserAttribute(Map<String, Object> claimMap, String claimValue) {
    if (!claimMap.containsKey(claimValue)) {
      throw new KeycloakException(
          "Keycloak user attribute '" + claimValue + "' not found.");
    }

    return claimMap.get(claimValue).toString();
  }

  /**
   * Returns the {@link KeycloakAuthenticationToken} which represents the token for a Keycloak
   * authentication.
   *
   * @return {@link KeycloakAuthenticationToken}
   */
  @Bean
  @Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
  public KeycloakAuthenticationToken getAccessToken() {
    return (KeycloakAuthenticationToken) getRequest().getUserPrincipal();
  }

  /**
   * Returns the {@link KeycloakSecurityContext}.
   *
   * @return {@link KeycloakSecurityContext}
   */
  @Bean
  @Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
  public KeycloakSecurityContext getKeycloakSecurityContext() {
    return ((KeycloakAuthenticationToken) getRequest().getUserPrincipal())
        .getAccount().getKeycloakSecurityContext();
  }

  private HttpServletRequest getRequest() {
    return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
        .getRequest();
  }
}
