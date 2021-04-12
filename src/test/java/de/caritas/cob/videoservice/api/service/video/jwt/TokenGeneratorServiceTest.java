package de.caritas.cob.videoservice.api.service.video.jwt;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import com.auth0.jwt.JWT;
import com.auth0.jwt.impl.NullClaim;
import com.auth0.jwt.interfaces.DecodedJWT;
import de.caritas.cob.videoservice.api.service.video.jwt.model.VideoCallToken;
import org.junit.Before;
import org.junit.Test;

public class TokenGeneratorServiceTest {

  private static final String AUDIENCE_VALUE = "server";
  private static final String ISSUER_VALUE = "client";
  private static final String SUBJECT_VALUE = "meet";

  private final TokenGeneratorService tokenGeneratorService = new TokenGeneratorService();

  @Before
  public void setup() {
    setField(tokenGeneratorService, "audience", AUDIENCE_VALUE);
    setField(tokenGeneratorService, "issuer", ISSUER_VALUE);
    setField(tokenGeneratorService, "subject", SUBJECT_VALUE);
    setField(tokenGeneratorService, "secret", "password");
    setField(tokenGeneratorService, "validityHours", 10);
  }

  @Test
  public void generateToken_Should_returnExpectedTokens_When_roomIdAndAskerAreEmpty() {
    VideoCallToken token = this.tokenGeneratorService.generateToken("", "");

    String guestToken = token.getGuestToken();
    String userToken = token.getUserRelatedToken();
    String moderatorToken = token.getModeratorToken();

    verifyBasicTokenFields(guestToken, "");
    verifyBasicTokenFields(userToken, "");
    verifyBasicTokenFields(moderatorToken, "");
    assertThat(JWT.decode(guestToken).getClaim("context"), instanceOf(NullClaim.class));
    assertThat(JWT.decode(userToken).getClaim("context").asMap().get("user").toString(),
        is("{name=}"));
    assertThat(JWT.decode(moderatorToken).getClaim("moderator").asBoolean(),
        is(true));
  }

  private void verifyBasicTokenFields(String jwt, String expectedRoomId) {
    DecodedJWT decodedBasicToken = JWT.decode(jwt);
    assertThat(decodedBasicToken.getAudience(), is(singletonList(AUDIENCE_VALUE)));
    assertThat(decodedBasicToken.getIssuer(), is(ISSUER_VALUE));
    assertThat(decodedBasicToken.getSubject(), is(SUBJECT_VALUE));
    assertThat(decodedBasicToken.getSignature(), notNullValue());
    assertThat(decodedBasicToken.getExpiresAt(), notNullValue());
    assertThat(decodedBasicToken.getClaim("room").asString(), is(expectedRoomId));
  }

  @Test
  public void generateToken_Should_returnExpectedTokens_When_roomIdIsGiven() {
    VideoCallToken token = this.tokenGeneratorService.generateToken("validRoomId", "");

    String guestToken = token.getGuestToken();
    String userToken = token.getUserRelatedToken();
    String moderatorToken = token.getModeratorToken();

    verifyBasicTokenFields(guestToken, "validRoomId");
    verifyBasicTokenFields(userToken, "validRoomId");
    verifyBasicTokenFields(moderatorToken, "validRoomId");
    assertThat(JWT.decode(moderatorToken).getClaim("moderator").asBoolean(),
        is(true));
  }

  @Test
  public void generateToken_Should_returnExpectedContextInUserToken_When_askerUsernameIsGiven() {
    VideoCallToken token = this.tokenGeneratorService.generateToken("", "asker123");

    String userToken = token.getUserRelatedToken();

    assertThat(JWT.decode(userToken).getClaim("context").asMap().get("user").toString(),
        is("{name=asker123}"));
  }

}
