package de.caritas.cob.videoservice.api.service.video.jwt;

import static de.caritas.cob.videoservice.api.testhelper.TestConstants.GUEST_VIDEO_CALL_URL;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import com.auth0.jwt.JWT;
import com.auth0.jwt.impl.NullClaim;
import com.auth0.jwt.interfaces.DecodedJWT;
import de.caritas.cob.videoservice.api.authorization.VideoUser;
import de.caritas.cob.videoservice.api.exception.httpresponse.InternalServerErrorException;
import de.caritas.cob.videoservice.api.service.video.jwt.model.VideoCallToken;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TokenGeneratorServiceTest {

  private static final String AUDIENCE_VALUE = "server";
  private static final String ISSUER_VALUE = "client";
  private static final String SUBJECT_VALUE = "meet";

  @InjectMocks private TokenGeneratorService tokenGeneratorService;

  @Mock private VideoUser authenticatedUser;

  @Before
  public void setup() {
    setField(tokenGeneratorService, "audience", AUDIENCE_VALUE);
    setField(tokenGeneratorService, "issuer", ISSUER_VALUE);
    setField(tokenGeneratorService, "subject", SUBJECT_VALUE);
    setField(tokenGeneratorService, "secret", "password");
    setField(tokenGeneratorService, "validityHours", 10);
    tokenGeneratorService.initAlgorithm();
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
  public void generateNonModeratorToken_Should_returnExpectedTokens_When_roomIdIsGiven() {
    VideoCallToken token =
        this.tokenGeneratorService.generateNonModeratorVideoCallToken("validRoomId");

    String guestToken = token.getGuestToken();
    String userToken = token.getUserRelatedToken();

    verifyBasicTokenFields(guestToken, "validRoomId");
    verifyBasicTokenFields(userToken, "validRoomId");
  }

  @Test
  public void generateNonModeratorToken_Should_returnExpectedTokens_When_roomIdAndAskerAreEmpty() {
    VideoCallToken token = this.tokenGeneratorService.generateNonModeratorVideoCallToken("");

    String guestToken = token.getGuestToken();
    String userToken = token.getUserRelatedToken();

    verifyBasicTokenFields(guestToken, "");
    verifyBasicTokenFields(userToken, "");
    assertThat(JWT.decode(guestToken).getClaim("context"), instanceOf(NullClaim.class));
  }

  @Test(expected = InternalServerErrorException.class)
  public void generateModeratorToken_Should_ThrowInternalServerErrorException_When_roomIdIsEmpty() {
    this.tokenGeneratorService.generateModeratorToken("", GUEST_VIDEO_CALL_URL);

    verifyNoMoreInteractions(authenticatedUser);
  }

  @Test(expected = InternalServerErrorException.class)
  public void
      generateModeratorToken_Should_ThrowInternalServerErrorException_When_guestVideoCallUrlIsEmpty() {
    this.tokenGeneratorService.generateModeratorToken("validRoomId", "");

    verifyNoMoreInteractions(authenticatedUser);
  }

  @Test
  public void generateModeratorToken_Should_returnExpectedToken_When_ParamsAreGiven() {
    String moderatorToken =
        this.tokenGeneratorService.generateModeratorToken("validRoomId", GUEST_VIDEO_CALL_URL);

    verifyBasicTokenFields(moderatorToken, "validRoomId");
    assertThat(JWT.decode(moderatorToken).getClaim("moderator").asBoolean(), is(true));
    assertThat(
        JWT.decode(moderatorToken).getClaim("guestVideoCallUrl").asString(),
        is(GUEST_VIDEO_CALL_URL));
  }

  @Test
  public void generateToken_should_generate_moderator_token_if_user_is_consultant() {
    when(authenticatedUser.isConsultant()).thenReturn(true);

    var moderatorToken = tokenGeneratorService.generateToken("privateRoom4711");

    verifyBasicTokenFields(moderatorToken, "privateRoom4711");
    assertThat(JWT.decode(moderatorToken).getClaim("moderator").asBoolean(), is(true));
  }

  @Test
  public void generateToken_should_generate_non_moderator_token_if_user_is_no_consultant() {
    when(authenticatedUser.isConsultant()).thenReturn(false);

    var moderatorToken = tokenGeneratorService.generateToken("privateRoom4711");

    assertThat(JWT.decode(moderatorToken).getClaim("moderator").isNull(), is(true));
    verifyBasicTokenFields(moderatorToken, "privateRoom4711");
  }
}
