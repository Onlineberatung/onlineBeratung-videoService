package de.caritas.cob.videoservice.api.service.video;

import de.caritas.cob.videoservice.api.exception.httpresponse.InternalServerErrorException;
import de.caritas.cob.videoservice.api.service.UuidRegistry;
import de.caritas.cob.videoservice.api.service.video.jwt.TokenGeneratorService;
import de.caritas.cob.videoservice.api.service.video.jwt.model.VideoCallToken;
import de.caritas.cob.videoservice.api.service.video.jwt.model.VideoCallUrls;
import java.net.MalformedURLException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Url generator for video call urls.
 */
@Service
@RequiredArgsConstructor
public class VideoCallUrlGeneratorService {

  private static final String JWT_QUERY_PARAM = "jwt";

  private final @NonNull UuidRegistry uuidRegistry;
  private final @NonNull TokenGeneratorService tokenGeneratorService;

  @Value("${video.call.server.url}")
  private String videoCallServerUrl;

  /**
   * Generates the {@link VideoCallUrls} for guest, asker and consultant.
   *
   * @param askerName the username of the asker
   * @return the generated {@link VideoCallUrls}
   */
  public VideoCallUrls generateVideoCallUrls(String askerName) {

    String uuid = uuidRegistry.generateUniqueUuid();
    VideoCallToken token = this.tokenGeneratorService.generateNonModeratorToken(uuid, askerName);

    return VideoCallUrls.builder()
        .userVideoUrl(buildUrl(uuid, token.getUserRelatedToken()))
        .moderatorVideoUrl(buildUrl(uuid, this.tokenGeneratorService
            .generateModeratorToken(uuid, buildUrl(uuid, token.getGuestToken()))))
        .build();
  }

  private String buildUrl(String uuid, String token) {
    try {
      return UriComponentsBuilder
          .fromHttpUrl(this.videoCallServerUrl)
          .pathSegment(uuid)
          .queryParam(JWT_QUERY_PARAM, token)
          .build()
          .toUri()
          .toURL()
          .toString();
    } catch (MalformedURLException | IllegalArgumentException ex) {
      throw new InternalServerErrorException("Could not generate video call URL.");
    }
  }

}
