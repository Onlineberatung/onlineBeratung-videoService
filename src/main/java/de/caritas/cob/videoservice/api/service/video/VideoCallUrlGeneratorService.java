package de.caritas.cob.videoservice.api.service.video;

import de.caritas.cob.videoservice.api.exception.httpresponse.InternalServerErrorException;
import de.caritas.cob.videoservice.api.service.UuidRegistry;
import de.caritas.cob.videoservice.api.service.video.jwt.TokenGeneratorService;
import de.caritas.cob.videoservice.api.service.video.jwt.model.VideoCallTokenPair;
import de.caritas.cob.videoservice.api.service.video.jwt.model.VideoCallUrlPair;
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
   * Generates the {@link VideoCallUrlPair} for asker and consultant.
   *
   * @param askerName the username of the asker
   * @return the generated {@link VideoCallUrlPair}
   */
  public VideoCallUrlPair generateVideoCallUrlPair(String askerName) {

    String uuid = uuidRegistry.generateUniqueUuid();
    VideoCallTokenPair tokenPair = this.tokenGeneratorService.generateTokenPair(uuid, askerName);

    return VideoCallUrlPair.builder()
        .basicVideoUrl(buildUrl(uuid, tokenPair.getBasicToken()))
        .userVideoUrl(buildUrl(uuid, tokenPair.getUserToken()))
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
