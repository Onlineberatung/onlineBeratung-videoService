package de.caritas.cob.videoservice.api.facade;

import static de.caritas.cob.videoservice.api.service.session.SessionStatus.IN_PROGRESS;
import static java.util.Collections.singletonList;

import de.caritas.cob.videoservice.api.authorization.AuthenticatedUser;
import de.caritas.cob.videoservice.api.exception.httpresponse.BadRequestException;
import de.caritas.cob.videoservice.api.model.CreateVideoCallResponseDTO;
import de.caritas.cob.videoservice.api.service.LogService;
import de.caritas.cob.videoservice.api.service.liveevent.LiveEventNotificationService;
import de.caritas.cob.videoservice.api.service.session.SessionService;
import de.caritas.cob.videoservice.api.service.video.VideoCallUrlGeneratorService;
import de.caritas.cob.videoservice.api.service.video.jwt.model.VideoCallUrls;
import de.caritas.cob.videoservice.liveservice.generated.web.model.EventType;
import de.caritas.cob.videoservice.liveservice.generated.web.model.LiveEventMessage;
import de.caritas.cob.videoservice.liveservice.generated.web.model.VideoCallRequestDTO;
import de.caritas.cob.videoservice.userservice.generated.web.model.ConsultantSessionDTO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Facade to encapsulate starting a video call.
 */
@Service
@RequiredArgsConstructor
public class StartVideoCallFacade {

  private final @NonNull SessionService sessionService;
  private final @NonNull LiveEventNotificationService liveEventNotificationService;
  private final @NonNull AuthenticatedUser authenticatedUser;
  private final @NonNull VideoCallUrlGeneratorService videoCallUrlGeneratorService;

  /**
   * Generates unique video call URLs and triggers a live event to inform the receiver of the call.
   *
   * @param sessionId         session ID
   * @param initiatorRcUserId initiator Rocket.Chat user ID
   * @return {@link CreateVideoCallResponseDTO}
   */
  public CreateVideoCallResponseDTO startVideoCall(Long sessionId, String initiatorRcUserId) {

    ConsultantSessionDTO consultantSessionDto = this.sessionService
        .findSessionOfCurrentConsultant(sessionId);
    verifySessionStatus(consultantSessionDto);

    VideoCallUrls videoCallUrls = this.videoCallUrlGeneratorService
        .generateVideoCallUrls(consultantSessionDto.getAskerUserName());

    this.liveEventNotificationService
        .sendVideoCallRequestLiveEvent(buildLiveEventMessage(consultantSessionDto,
            videoCallUrls.getUserVideoUrl(), initiatorRcUserId),
            singletonList(consultantSessionDto.getAskerId()));

    return new CreateVideoCallResponseDTO()
        .guestVideoCallUrl(videoCallUrls.getGuestVideoUrl())
        .moderatorVideoCallUrl(videoCallUrls.getModeratorVideoUrl());
  }

  private void verifySessionStatus(ConsultantSessionDTO consultantSessionDto) {
    if (!IN_PROGRESS.getValue().equals(consultantSessionDto.getStatus())) {
      throw new BadRequestException("Session must be in progress", LogService::logWarning);
    }
  }

  private LiveEventMessage buildLiveEventMessage(ConsultantSessionDTO consultantSessionDto,
      String videoChatUrl, String initiatorRcUserId) {
    VideoCallRequestDTO videoCallRequestDto = new VideoCallRequestDTO()
        .videoCallUrl(videoChatUrl)
        .rcGroupId(consultantSessionDto.getGroupId())
        .initiatorRcUserId(initiatorRcUserId)
        .initiatorUsername(authenticatedUser.getUsername());

    return new LiveEventMessage()
        .eventType(EventType.VIDEOCALLREQUEST)
        .eventContent(videoCallRequestDto);
  }
}
