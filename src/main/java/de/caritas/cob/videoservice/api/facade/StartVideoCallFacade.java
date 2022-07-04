package de.caritas.cob.videoservice.api.facade;

import static de.caritas.cob.videoservice.api.service.session.SessionStatus.IN_PROGRESS;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import de.caritas.cob.videoservice.api.authorization.VideoUser;
import de.caritas.cob.videoservice.api.exception.httpresponse.BadRequestException;
import de.caritas.cob.videoservice.api.model.CreateVideoCallDTO;
import de.caritas.cob.videoservice.api.model.CreateVideoCallResponseDTO;
import de.caritas.cob.videoservice.api.service.LogService;
import de.caritas.cob.videoservice.api.service.UuidRegistry;
import de.caritas.cob.videoservice.api.service.liveevent.LiveEventNotificationService;
import de.caritas.cob.videoservice.api.service.session.SessionService;
import de.caritas.cob.videoservice.api.service.statistics.StatisticsService;
import de.caritas.cob.videoservice.api.service.statistics.event.StartVideoCallStatisticsEvent;
import de.caritas.cob.videoservice.api.service.video.VideoCallUrlGeneratorService;
import de.caritas.cob.videoservice.liveservice.generated.web.model.EventType;
import de.caritas.cob.videoservice.liveservice.generated.web.model.LiveEventMessage;
import de.caritas.cob.videoservice.liveservice.generated.web.model.VideoCallRequestDTO;
import de.caritas.cob.videoservice.statisticsservice.generated.web.model.UserRole;
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
  private final @NonNull VideoUser authenticatedUser;
  private final @NonNull VideoCallUrlGeneratorService videoCallUrlGeneratorService;
  private final @NonNull UuidRegistry uuidRegistry;
  private final @NonNull StatisticsService statisticsService;

  /**
   * Generates unique video call URLs and triggers a live event to inform the receiver of the call.
   *
   * @param createVideoCallRequest The requested DTO containing session id and optional initiators
   *                           username
   * @param initiatorRcUserId  initiator Rocket.Chat user ID
   * @return {@link CreateVideoCallResponseDTO}
   */
  public CreateVideoCallResponseDTO startVideoCall(CreateVideoCallDTO createVideoCallRequest,
      String initiatorRcUserId) {

    var sessionId = createVideoCallRequest.getSessionId();
    var consultantSessionDto = this.sessionService
        .findSessionOfCurrentConsultant(sessionId);
    verifySessionStatus(consultantSessionDto);

    var videoCallUuid = uuidRegistry.generateUniqueUuid();
    var videoCallUrls = this.videoCallUrlGeneratorService
        .generateVideoCallUrls(videoCallUuid);

    this.liveEventNotificationService
        .sendVideoCallRequestLiveEvent(buildLiveEventMessage(consultantSessionDto,
            videoCallUrls.getUserVideoUrl(), initiatorRcUserId,
            createVideoCallRequest.getInitiatorDisplayName()),
            singletonList(consultantSessionDto.getAskerId()));

    var createVideoCallResponseDto = new CreateVideoCallResponseDTO()
        .moderatorVideoCallUrl(videoCallUrls.getModeratorVideoUrl());

    statisticsService.fireEvent(
        new StartVideoCallStatisticsEvent(
            authenticatedUser.getUserId(),
            UserRole.CONSULTANT,
            sessionId,
            videoCallUuid));

    return createVideoCallResponseDto;
  }

  private void verifySessionStatus(ConsultantSessionDTO consultantSessionDto) {
    if (!IN_PROGRESS.getValue().equals(consultantSessionDto.getStatus())) {
      throw new BadRequestException("Session must be in progress", LogService::logWarning);
    }
  }

  private LiveEventMessage buildLiveEventMessage(ConsultantSessionDTO consultantSessionDto,
      String videoChatUrl, String initiatorRcUserId, String initiatorDisplayName) {
    var username =
        isNotBlank(initiatorDisplayName) ? initiatorDisplayName : authenticatedUser.getUsername();

    var videoCallRequestDto = new VideoCallRequestDTO()
        .videoCallUrl(videoChatUrl)
        .rcGroupId(consultantSessionDto.getGroupId())
        .initiatorRcUserId(initiatorRcUserId)
        .initiatorUsername(username);

    return new LiveEventMessage()
        .eventType(EventType.VIDEOCALLREQUEST)
        .eventContent(videoCallRequestDto);
  }
}
