package de.caritas.cob.videoservice.api.testhelper;

import de.caritas.cob.videoservice.api.model.CreateVideoCallResponseDTO;
import java.util.UUID;

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
   * RC
   */
  public static final String RC_USER_ID_HEADER = "RCUserId";
  public static final String RC_USER_ID_VALUE = "rcUser123";
  public static final String RC_CHAT_ROOM_ID = "R_ID_76543210";

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
      "https://video.call/237849234-34534-345345?jwt={guestToken}";
  public static final String MODERATOR_VIDEO_CALL_URL =
      "https://video.call/237849234-34534-345345?jwt={moderatorToken}";
  public static final CreateVideoCallResponseDTO CREATE_VIDEO_CALL_RESPONSE_DTO =
      new CreateVideoCallResponseDTO()
          .moderatorVideoCallUrl(MODERATOR_VIDEO_CALL_URL);
  public static final String VIDEO_CALL_UUID = UUID.randomUUID().toString();

  /*
   * User
   */
  public static final String USERNAME = "username";
  public static final String CONSULTANT_ID = "fb3cbee2-c5f3-4582-a5e4-d853572e9860";

  /*
   * Common
   */
  public static final String ERROR_MESSAGE = "Error message";
}
