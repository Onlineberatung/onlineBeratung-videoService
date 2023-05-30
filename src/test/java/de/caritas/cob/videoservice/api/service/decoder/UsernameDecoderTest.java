package de.caritas.cob.videoservice.api.service.decoder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

public class UsernameDecoderTest {

  private static final String USERNAME_DECODED = "Username!#123";
  private static final String USERNAME_ENCODED = "enc.KVZWK4TOMFWWKIJDGEZDG...";

  @Test
  public void decodeUsername_Should_ReturnDecodedUsername_WhenEncodedUsernameIsGiven() {
    String decodedUsername = new UsernameDecoder().decodeUsername(USERNAME_ENCODED);

    assertThat(decodedUsername, is(USERNAME_DECODED));
  }

  @Test
  public void decodeUsername_Should_ReturnDecodedUsername_WhenDecodedUsernameIsGiven() {
    String decodedUsername = new UsernameDecoder().decodeUsername(USERNAME_DECODED);

    assertThat(decodedUsername, is(USERNAME_DECODED));
  }

  @Test(expected = IllegalArgumentException.class)
  public void decodeUsername_Should_throwIllegalArgumentException_WhenDecodedUsernameIsNull() {
    String decodedUsername = new UsernameDecoder().decodeUsername(null);
  }
}
