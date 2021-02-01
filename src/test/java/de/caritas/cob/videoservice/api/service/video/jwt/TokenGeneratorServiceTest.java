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
import de.caritas.cob.videoservice.api.service.video.jwt.model.VideoCallTokenPair;
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
  public void generateTokenPair_Should_returnExpectedTokens_When_roomIdAndAskerAreEmpty() {
    VideoCallTokenPair tokenPair = this.tokenGeneratorService.generateTokenPair("", "");

    String basicToken = tokenPair.getBasicToken();
    String userToken = tokenPair.getUserToken();

    verifyBasicTokenFields(basicToken, "");
    verifyBasicTokenFields(userToken, "");
    assertThat(JWT.decode(basicToken).getClaim("context"), instanceOf(NullClaim.class));
    assertThat(JWT.decode(userToken).getClaim("context").asMap().get("user").toString(),
        is("{name=}"));
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
  public void generateTokenPair_Should_returnExpectedTokens_When_roomIdIsGiven() {
    VideoCallTokenPair tokenPair = this.tokenGeneratorService.generateTokenPair("validRoomId", "");

    String basicToken = tokenPair.getBasicToken();
    String userToken = tokenPair.getUserToken();

    verifyBasicTokenFields(basicToken, "validRoomId");
    verifyBasicTokenFields(userToken, "validRoomId");
  }

  @Test
  public void generateTokenPair_Should_returnExpectedContextInUserToken_When_askerUsernameIsGiven() {
    VideoCallTokenPair tokenPair = this.tokenGeneratorService.generateTokenPair("", "asker123");

    String userToken = tokenPair.getUserToken();

    assertThat(JWT.decode(userToken).getClaim("context").asMap().get("user").toString(),
        is("{name=asker123}"));
  }

}
