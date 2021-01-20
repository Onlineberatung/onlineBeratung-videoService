package de.caritas.cob.videoservice.api.exception;

/**
 * Custom Keycloak exception.
 */
public class KeycloakException extends RuntimeException {

  private static final long serialVersionUID = -5083156826149548581L;

  /**
   * Keycloak exception.
   *
   * @param message {@link String}
   */
  public KeycloakException(String message) {
    super(message);
  }
}
