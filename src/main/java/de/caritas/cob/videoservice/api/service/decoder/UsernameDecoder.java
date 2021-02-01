package de.caritas.cob.videoservice.api.service.decoder;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import org.apache.commons.codec.binary.Base32;

/**
 * Decoder class to decode encoded usernames.
 */
public class UsernameDecoder {

  private static final String ENCODING_PREFIX = "enc.";
  private static final String BASE32_PLACEHOLDER = "=";
  private static final String BASE32_PLACEHOLDER_USERNAME_REPLACE_STRING = ".";

  private final Base32 base32 = new Base32();

  /**
   * Decodes the given username if it isn't already decoded.
   *
   * @param username the username to decode
   * @return the decoded username
   */
  public String decodeUsername(String username) {
    if (isNull(username)) {
      throw new IllegalArgumentException("Username must not be null");
    }
    return username.startsWith(ENCODING_PREFIX) ? decodeBase32Username(username) : username;
  }

  private String decodeBase32Username(String username) {
    return new String(this.base32.decode(prepareForDecoding(username)));
  }

  private String prepareForDecoding(String username) {
    return username
        .replace(ENCODING_PREFIX, EMPTY)
        .toUpperCase()
        .replace(BASE32_PLACEHOLDER_USERNAME_REPLACE_STRING, BASE32_PLACEHOLDER);
  }

}
