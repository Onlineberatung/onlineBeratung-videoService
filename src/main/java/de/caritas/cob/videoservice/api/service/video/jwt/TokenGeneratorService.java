package de.caritas.cob.videoservice.api.service.video.jwt;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.HOURS;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.algorithms.Algorithm;
import de.caritas.cob.videoservice.api.authorization.VideoUser;
import de.caritas.cob.videoservice.api.exception.httpresponse.InternalServerErrorException;
import de.caritas.cob.videoservice.api.service.video.jwt.model.VideoCallToken;
import java.sql.Date;
import java.time.LocalDateTime;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/** JWT token generator service. */
@Service
public class TokenGeneratorService {

  @Autowired
  public TokenGeneratorService(
      @NonNull @Qualifier("AuthenticatedOrAnonymousUser") VideoUser authenticatedUser) {
    this.videoUser = authenticatedUser;
  }

  private final @NonNull VideoUser videoUser;

  private static final String ROOM_CLAIM = "room";
  private static final String MODERATOR_CLAIM = "moderator";
  private static final String GUEST_URL_CLAIM = "guestVideoCallUrl";

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

  @EventListener(ApplicationReadyEvent.class)
  public void initAlgorithm() {
    this.algorithm = Algorithm.HMAC256(this.secret);
  }

  /**
   * Generate token.
   *
   * @param roomId room id
   * @return token
   */
  public String generateToken(String roomId) {
    return videoUser.isConsultant()
        ? generateModeratorToken(roomId)
        : generateNonModeratorToken(roomId);
  }

  /**
   * Generates the {@link VideoCallToken} for anonymous user and asker.
   *
   * @param roomId the generated unique roomId
   * @return the generated {@link VideoCallToken}
   */
  public VideoCallToken generateNonModeratorVideoCallToken(String roomId) {
    return VideoCallToken.builder()
        .guestToken(generateNonModeratorToken(roomId))
        .userRelatedToken(buildUserRelatedJwt(roomId))
        .build();
  }

  public String generateNonModeratorToken(String roomId) {
    return buildBasicJwt(roomId).sign(algorithm);
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
    long epochMilli =
        LocalDateTime.now(UTC).plus(this.validityHours, HOURS).toInstant(UTC).toEpochMilli();
    return new Date(epochMilli);
  }

  private String buildUserRelatedJwt(String roomId) {
    return buildBasicJwt(roomId).sign(algorithm);
  }

  /**
   * Generates the {@link VideoCallToken} for the currently logged in moderator.
   *
   * @param roomId the generated unique roomId
   * @param guestVideoCallUrl the guest video call URL
   * @return the generated moderator token
   */
  public String generateModeratorToken(String roomId, String guestVideoCallUrl) {
    if (isEmpty(roomId) || isEmpty(guestVideoCallUrl)) {
      throw new InternalServerErrorException(
          String.format(
              "Room ID (%s) or guest video call URL (%s) cannot be empty.",
              roomId, guestVideoCallUrl));
    }

    return buildModeratorJwt(roomId, guestVideoCallUrl);
  }

  /**
   * Generate moderator token.
   *
   * @param roomId room id
   * @return token
   */
  public String generateModeratorToken(String roomId) {
    return buildBasicJwt(roomId).withClaim(MODERATOR_CLAIM, true).sign(algorithm);
  }

  private String buildModeratorJwt(String roomId, String guestVideoCallUrl) {
    return buildBasicJwt(roomId)
        .withClaim(MODERATOR_CLAIM, true)
        .withClaim(GUEST_URL_CLAIM, guestVideoCallUrl)
        .sign(algorithm);
  }
}
