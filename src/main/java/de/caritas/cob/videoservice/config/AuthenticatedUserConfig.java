package de.caritas.cob.videoservice.config;

import static java.util.Objects.isNull;

import de.caritas.cob.videoservice.api.authorization.VideoUser;
import de.caritas.cob.videoservice.api.exception.KeycloakException;
import java.security.Principal;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** Configuration for the {@link VideoUser}. */
@Configuration
public class AuthenticatedUserConfig {

  private static final String CLAIM_NAME_USER_ID = "userId";
  private static final String CLAIM_NAME_USERNAME = "username";
  private static final VideoUser ANONYMOUS_USER = new VideoUser();

  /**
   * Returns the currently authenticated user.
   *
   * @return {@link VideoUser}
   */
  @Bean
  @Primary
  @Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
  public VideoUser getAuthenticatedUser() {
    var userPrincipal = getRequest().getUserPrincipal();
    return createAuthenticatedUser(userPrincipal);
  }

  /**
   * Returns the currently authenticated user, or an anonymous representation.
   *
   * @return {@link VideoUser}
   */
  @Bean
  @Qualifier("AuthenticatedOrAnonymousUser")
  @Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
  public VideoUser getAuthenticatedOrAnonymousUser() {
    var userPrincipal = getRequest().getUserPrincipal();
    if (isNull(userPrincipal)) {
      return ANONYMOUS_USER;
    }
    return createAuthenticatedUser(userPrincipal);
  }

  private VideoUser createAuthenticatedUser(Principal userPrincipal) {
    var keycloakUser = (KeycloakAuthenticationToken) userPrincipal;
    var keycloakSecContext = keycloakUser.getAccount().getKeycloakSecurityContext();
    Map<String, Object> claimMap = keycloakSecContext.getToken().getOtherClaims();

    var authenticatedUser = new VideoUser();
    authenticatedUser.setAccessToken(getUserAccessToken(keycloakSecContext));
    authenticatedUser.setUserId(getUserAttribute(claimMap, CLAIM_NAME_USER_ID));
    authenticatedUser.setUsername(getUserAttribute(claimMap, CLAIM_NAME_USERNAME));

    var roles = keycloakSecContext.getToken().getRealmAccess().getRoles();
    authenticatedUser.setRoles(roles);

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
      throw new KeycloakException("Keycloak user attribute '" + claimValue + "' not found.");
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
        .getAccount()
        .getKeycloakSecurityContext();
  }

  private HttpServletRequest getRequest() {
    return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
        .getRequest();
  }
}
