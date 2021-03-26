package de.caritas.cob.videoservice.api.testhelper;

import de.caritas.cob.videoservice.api.model.CreateVideoCallResponseDTO;

public class TestConstants {

  /*
   * Authorization
   */
  public static final String ROLE_CONSULTANT = "consultant";
  public static final String ROLE_USER = "user";
  public static final String ROLE_UNKNOWN = "unknown";
  public static final String AUTHORITY_CONSULTANT = "AUTHORIZATION_CONSULTANT_DEFAULT";
  public static final String AUTHORITY_USER = "AUTHORIZATION_USER_DEFAULT";
  public static final String BEARER_TOKEN = "w948hisidfgjaoidg839huishdfkjsdkfjhsdf34";

  /*
   * RC Header
   */
  public static final String RC_USER_ID_HEADER = "RCUserId";
  public static final String RC_USER_ID_VALUE = "rcUser123";

  /*
   * CSRF token
   */
  public static final String CSRF_COOKIE = "csrfCookie";
  public static final String CSRF_HEADER = "csrfHeader";
  public static final String CSRF_VALUE = "test";

  /*
   * Session
   */
  public static final Long SESSION_ID = 0L;

  /*
   * Video call
   */
  public static final String GUEST_VIDEO_CALL_URL =
      "http://video.call/237849234-34534-345345?jwt={guestToken}";
  public static final String MODERATOR_VIDEO_CALL_URL =
      "http://video.call/237849234-34534-345345?jwt={moderatorToken}";
  public static final CreateVideoCallResponseDTO CREATE_VIDEO_CALL_RESPONSE_DTO =
      new CreateVideoCallResponseDTO()
          .guestVideoCallUrl(GUEST_VIDEO_CALL_URL)
          .moderatorVideoCallUrl(MODERATOR_VIDEO_CALL_URL);

  /*
   * User
   */
  public static final String USERNAME = "username";
}
