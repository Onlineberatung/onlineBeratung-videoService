package de.caritas.cob.videoservice.api.facade;

import de.caritas.cob.videoservice.api.authorization.AuthenticatedUser;
import de.caritas.cob.videoservice.api.exception.httpresponse.InternalServerErrorException;
import de.caritas.cob.videoservice.api.service.UuidRegistry;
import de.caritas.cob.videoservice.api.service.liveevent.LiveEventNotificationService;
import de.caritas.cob.videoservice.api.service.session.SessionService;
import de.caritas.cob.videoservice.liveservice.generated.web.model.EventType;
import de.caritas.cob.videoservice.liveservice.generated.web.model.LiveEventMessage;
import de.caritas.cob.videoservice.liveservice.generated.web.model.VideoCallRequestDTO;
import de.caritas.cob.videoservice.userservice.generated.web.model.ConsultantSessionDTO;
import java.net.MalformedURLException;
import java.util.Collections;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Facade to encapsulate starting a video call.
 */
@Service
@RequiredArgsConstructor
public class StartVideoCallFacade {

  private final @NonNull SessionService sessionService;
  private final @NonNull LiveEventNotificationService liveEventNotificationService;
  private final @NonNull UuidRegistry uuidRegistry;
  private final @NonNull AuthenticatedUser authenticatedUser;

  @Value("${video.call.server.url}")
  private String videoCallServerUrl;

  /**
   * Generates an unique video call URL and triggers a live event to inform the receiver of the
   * call.
   *
   * @param sessionId session Id
   * @return video call URL
   */
  public String startVideoCall(Long sessionId) {

    ConsultantSessionDTO consultantSessionDto = sessionService
        .findSessionOfCurrentConsultant(sessionId);
    String videoChatUrl = generateVideoCallUrl();

    liveEventNotificationService
        .sendVideoCallRequestLiveEvent(buildLiveEventMessage(consultantSessionDto, videoChatUrl),
            Collections.singletonList(consultantSessionDto.getAskerId()));

    return videoChatUrl;
  }

  private String generateVideoCallUrl() {
    try {
      return UriComponentsBuilder
          .fromHttpUrl(videoCallServerUrl)
          .pathSegment(uuidRegistry.generateUniqueUuid())
          .build()
          .toUri()
          .toURL()
          .toString();

    } catch (MalformedURLException | IllegalArgumentException ex) {
      throw new InternalServerErrorException("Could not generate video call URL.");
    }
  }

  private LiveEventMessage buildLiveEventMessage(ConsultantSessionDTO consultantSessionDto,
      String videoChatUrl) {
    VideoCallRequestDTO videoCallRequestDto = new VideoCallRequestDTO()
        .videoCallUrl(videoChatUrl)
        .rcGroupId(consultantSessionDto.getGroupId())
        .rcUserId(consultantSessionDto.getConsultantRcId())
        .username(authenticatedUser.getUsername());

    return new LiveEventMessage()
        .eventType(EventType.VIDEOCALLREQUEST)
        .eventContent(videoCallRequestDto);
  }
}
