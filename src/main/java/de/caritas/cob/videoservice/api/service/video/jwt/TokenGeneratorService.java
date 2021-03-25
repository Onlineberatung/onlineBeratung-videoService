package de.caritas.cob.videoservice.api.service.video.jwt;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.HOURS;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.algorithms.Algorithm;
import de.caritas.cob.videoservice.api.service.decoder.UsernameDecoder;
import de.caritas.cob.videoservice.api.service.video.jwt.model.VideoCallToken;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * JWT token generator service.
 */
@Service
public class TokenGeneratorService {

  private static final String CONTEXT_CLAIM = "context";
  private static final String ROOM_CLAIM = "room";
  private static final String MODERATOR_CLAIM = "moderator";
  private static final String CONTEXT_USER = "user";
  private static final String USER_NAME = "name";

  @Value("${video.call.security.jwt.audience}")
  private String audience;

  @Value("${video.call.security.jwt.issuer}")
  private String issuer;

  @Value("${video.call.security.jwt.subject}")
  private String subject;

  @Value("${video.call.security.jwt.secret}")
  private String secret;

  @Value("${video.call.security.jwt.validity.hours}")
  private long validityHours;

  private Algorithm algorithm;

  /**
   * Generates the {@link VideoCallToken} for anonymous user, asker (containing user name) and
   * moderator.
   *
   * @param roomId    the generated unique roomId
   * @param askerName the username of the asker
   * @return the generated {@link VideoCallToken}
   */
  public VideoCallToken generateToken(String roomId, String askerName) {
    algorithm = Algorithm.HMAC256(this.secret);

    return VideoCallToken.builder()
        .guestToken(buildGuestJwt(roomId))
        .userRelatedToken(buildUserRelatedJwt(roomId, askerName))
        .moderatorToken(buildModeratorJwt(roomId))
        .build();
  }

  private String buildGuestJwt(String roomId) {
    return buildBasicJwt(roomId)
        .sign(algorithm);
  }

  private String buildUserRelatedJwt(String roomId, String askerName) {
    return buildBasicJwt(roomId)
        .withClaim(CONTEXT_CLAIM, createUserContext(askerName))
        .sign(algorithm);
  }

  private String buildModeratorJwt(String roomId) {
    return buildBasicJwt(roomId)
        .withClaim(MODERATOR_CLAIM, true)
        .sign(algorithm);
  }

  private Builder buildBasicJwt(String roomId) {
    return JWT.create()
        .withAudience(this.audience)
        .withIssuer(this.issuer)
        .withSubject(this.subject)
        .withClaim(ROOM_CLAIM, roomId)
        .withExpiresAt(buildThreeHoursValidityDate());
  }

  private Date buildThreeHoursValidityDate() {
    long epochMilli = LocalDateTime.now(UTC)
        .plus(this.validityHours, HOURS)
        .toInstant(UTC)
        .toEpochMilli();
    return new Date(epochMilli);
  }

  private Map<String, Map<String, String>> createUserContext(String askerName) {
    Map<String, Map<String, String>> context = new HashMap<>();
    Map<String, String> user = new HashMap<>();
    user.put(USER_NAME, new UsernameDecoder().decodeUsername(askerName));
    context.put(CONTEXT_USER, user);

    return context;
  }

}
