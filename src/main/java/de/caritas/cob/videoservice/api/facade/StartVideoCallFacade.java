package de.caritas.cob.videoservice.api.facade;

import de.caritas.cob.videoservice.api.exception.httpresponse.InternalServerErrorException;
import de.caritas.cob.videoservice.api.service.SessionService;
import de.caritas.cob.videoservice.api.service.liveevent.LiveEventNotificationService;
import de.caritas.cob.videoservice.userservice.generated.web.model.ConsultantSessionDTO;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Facade to encapsulate starting a chat.
 */
@Service
@RequiredArgsConstructor
public class StartVideoCallFacade {

  @Value("${video.call.server.url}")
  private String videoCallServerUrl;

  private final @NonNull SessionService sessionService;
  private final @NonNull LiveEventNotificationService liveEventNotificationService;

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
        .sendVideoCallRequestLiveEvent(videoChatUrl, consultantSessionDto.getGroupId(),
            Collections.singletonList(consultantSessionDto.getAskerId()));

    return videoChatUrl;
  }

  private String generateVideoCallUrl() {
    try {
      return UriComponentsBuilder
          .fromHttpUrl(videoCallServerUrl)
          .pathSegment(UUID.randomUUID().toString())
          .build()
          .toUri()
          .toURL()
          .toString();

    } catch (MalformedURLException ex) {
      throw new InternalServerErrorException("Could not generate video call URL.");
    }
  }
}
