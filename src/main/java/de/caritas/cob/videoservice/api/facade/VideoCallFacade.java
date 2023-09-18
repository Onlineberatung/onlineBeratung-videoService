package de.caritas.cob.videoservice.api.facade;

import static de.caritas.cob.videoservice.api.service.session.SessionStatus.IN_PROGRESS;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import de.caritas.cob.videoservice.api.authorization.VideoUser;
import de.caritas.cob.videoservice.api.exception.httpresponse.BadRequestException;
import de.caritas.cob.videoservice.api.model.CreateVideoCallDTO;
import de.caritas.cob.videoservice.api.model.VideoCallResponseDTO;
import de.caritas.cob.videoservice.api.model.VideoRoomEntity;
import de.caritas.cob.videoservice.api.service.LogService;
import de.caritas.cob.videoservice.api.service.UuidRegistry;
import de.caritas.cob.videoservice.api.service.liveevent.LiveEventNotificationService;
import de.caritas.cob.videoservice.api.service.message.MessageService;
import de.caritas.cob.videoservice.api.service.session.ChatService;
import de.caritas.cob.videoservice.api.service.session.SessionService;
import de.caritas.cob.videoservice.api.service.statistics.StatisticsService;
import de.caritas.cob.videoservice.api.service.statistics.event.StartVideoCallStatisticsEvent;
import de.caritas.cob.videoservice.api.service.statistics.event.StopVideoCallStatisticsEvent;
import de.caritas.cob.videoservice.api.service.video.VideoCallUrlGeneratorService;
import de.caritas.cob.videoservice.api.service.video.VideoRoomService;
import de.caritas.cob.videoservice.api.tenant.TenantContext;
import de.caritas.cob.videoservice.liveservice.generated.web.model.EventType;
import de.caritas.cob.videoservice.liveservice.generated.web.model.LiveEventMessage;
import de.caritas.cob.videoservice.liveservice.generated.web.model.VideoCallRequestDTO;
import de.caritas.cob.videoservice.statisticsservice.generated.web.model.UserRole;
import de.caritas.cob.videoservice.userservice.generated.web.model.ConsultantSessionDTO;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Facade for video call starts and stops. */
@Service
@RequiredArgsConstructor
@Slf4j
public class VideoCallFacade {

  public static final String MUC_MEET_JITSI_SUFFIX = "@muc.meet.jitsi";
  private final @NonNull SessionService sessionService;

  private final @NonNull ChatService chatService;
  private final @NonNull LiveEventNotificationService liveEventNotificationService;
  private final @NonNull VideoUser authenticatedUser;
  private final @NonNull VideoCallUrlGeneratorService videoCallUrlGeneratorService;
  private final @NonNull UuidRegistry uuidRegistry;
  private final @NonNull StatisticsService statisticsService;
  private final @NonNull VideoRoomService videoRoomService;

  private final @NonNull MessageService messageService;

  /**
   * Generates unique video call URLs and triggers a live event to inform the receiver of the call.
   *
   * @param createVideoCallRequest The requested DTO containing session id and optional initiators
   *     username
   * @param initiatorRcUserId initiator Rocket.Chat user ID
   * @return {@link VideoCallResponseDTO}
   */
  public VideoCallResponseDTO startVideoCall(
      CreateVideoCallDTO createVideoCallRequest, String initiatorRcUserId) {
    if (createVideoCallRequest.getGroupChatId() != null) {
      return startGroupVideoCall(createVideoCallRequest, initiatorRcUserId);
    } else {
      var sessionId = createVideoCallRequest.getSessionId();
      return startOneToOneVideoCall(createVideoCallRequest, initiatorRcUserId, sessionId);
    }
  }

  private VideoCallResponseDTO startOneToOneVideoCall(
      CreateVideoCallDTO createVideoCallRequest, String initiatorRcUserId, Long sessionId) {
    log.info("Starting one to one video call for sessionId {}", sessionId);

    var consultantSessionDto = this.sessionService.findSessionOfCurrentConsultant(sessionId);
    verifySessionStatus(consultantSessionDto);
    var videoCallUuid = uuidRegistry.generateUniqueUuid();
    var videoCallUrls = this.videoCallUrlGeneratorService.generateVideoCallUrls(videoCallUuid);
    this.liveEventNotificationService.sendVideoCallRequestLiveEvent(
        buildLiveEventMessage(
            consultantSessionDto.getGroupId(),
            videoCallUrls.getUserVideoUrl(),
            initiatorRcUserId,
            createVideoCallRequest.getInitiatorDisplayName()),
        singletonList(consultantSessionDto.getAskerId()));

    this.videoRoomService.createOneToOneVideoRoom(
        consultantSessionDto.getId(), consultantSessionDto.getGroupId(), videoCallUuid);
    var createVideoCallResponseDto =
        new VideoCallResponseDTO().moderatorVideoCallUrl(videoCallUrls.getModeratorVideoUrl());
    statisticsService.fireEvent(
        new StartVideoCallStatisticsEvent(
            authenticatedUser.getUserId(),
            UserRole.CONSULTANT,
            sessionId,
            videoCallUuid,
            consultantSessionDto.getAskerId(),
            TenantContext.getCurrentTenant()));

    log.info("Started one to one video call for sessionId {}", sessionId);
    return createVideoCallResponseDto;
  }

  private VideoCallResponseDTO startGroupVideoCall(
      CreateVideoCallDTO createVideoCallRequest, String initiatorRcUserId) {
    chatService.assertCanModerateChat(createVideoCallRequest.getGroupChatId());
    log.info(
        "Starting group video call for groupChatId {}", createVideoCallRequest.getGroupChatId());

    var chatById = chatService.findChatById(createVideoCallRequest.getGroupChatId());
    var chatMembers = chatService.getChatMembers(createVideoCallRequest.getGroupChatId());
    var videoCallUuid = uuidRegistry.generateUniqueUuid();
    var videoCallUrls = this.videoCallUrlGeneratorService.generateVideoCallUrls(videoCallUuid);
    var chatMemberIds =
        chatMembers.getMembers().stream()
            .filter(member -> !initiatorRcUserId.equals(member.getId()))
            .map(member -> member.getUserId())
            .collect(Collectors.toList());
    this.liveEventNotificationService.sendVideoCallRequestLiveEvent(
        buildLiveEventMessage(
            chatById.getGroupId(),
            videoCallUrls.getUserVideoUrl(),
            initiatorRcUserId,
            createVideoCallRequest.getInitiatorDisplayName()),
        chatMemberIds);

    var groupVideoRoom =
        this.videoRoomService.createGroupVideoRoom(
            createVideoCallRequest.getGroupChatId(), chatById.getGroupId(), videoCallUuid);

    messageService.createAndSendVideoCallStartedMessage(
        chatById.getGroupId(),
        authenticatedUser.getUsername(),
        groupVideoRoom,
        videoCallUrls.getUserVideoUrl(),
        createVideoCallRequest.getInitiatorDisplayName(),
        initiatorRcUserId);

    log.info(
        "Started group video call for groupChatId {}", createVideoCallRequest.getGroupChatId());
    return new VideoCallResponseDTO().moderatorVideoCallUrl(videoCallUrls.getModeratorVideoUrl());
  }

  public VideoCallResponseDTO joinGroupVideoCall(String jitsiRoomId) {
    var videoRoomEntity = videoRoomService.findByJitsiRoomId(jitsiRoomId).orElseThrow();
    if (videoRoomEntity.getSessionId() != null) {
      throw new BadRequestException(
          "Bad request: jitsiRoomId is not a group video call id", LogService::logWarning);
    }
    chatService.assertCanModerateChat(videoRoomEntity.getGroupChatId());
    log.info("Joining group video call for jitsiRoomId {}", jitsiRoomId);
    var videoCallUrls = this.videoCallUrlGeneratorService.generateVideoCallUrls(jitsiRoomId);
    videoRoomService.findLatestActiveRoomForSessionId(videoRoomEntity.getGroupChatId());
    return new VideoCallResponseDTO().moderatorVideoCallUrl(videoCallUrls.getModeratorVideoUrl());
  }

  public void stopVideoCall(String roomId) {
    fireVideoCallStoppedStatisticsEvent(roomId);
  }

  public void handleVideoCallStoppedEvent(String roomId) {
    log.info("Handling video call stopped event for roomId {}", roomId);
    roomId = removeJitsiSuffix(roomId);
    var videoRoomEntity = videoRoomService.findByJitsiRoomId(roomId).orElseThrow();
    if (videoRoomEntity.getGroupChatId() != null) {
      stopGroupVideoCall(videoRoomEntity);
    } else {
      stopOneToOneVideoCall(videoRoomEntity);
    }
    log.info("Stopped video call with roomId {}", roomId);
  }

  private String removeJitsiSuffix(String roomId) {
    if (roomId != null) {
      roomId = roomId.replace(MUC_MEET_JITSI_SUFFIX, "");
    }
    return roomId;
  }

  private void stopGroupVideoCall(VideoRoomEntity videoRoomEntity) {
    log.info("Stopping group video call with groupChatId {}", videoRoomEntity.getGroupChatId());
    videoRoomService.closeVideoRoom(videoRoomEntity);
    messageService.createAndSendVideoCallEndedMessage(
        videoRoomEntity.getRocketChatRoomId(), "Video-Call stopped", videoRoomEntity);
    fireVideoCallStoppedStatisticsEvent(videoRoomEntity.getJitsiRoomId());
    log.info("Stopped group video call with groupChatId {}", videoRoomEntity.getGroupChatId());
  }

  private void fireVideoCallStoppedStatisticsEvent(String roomId) {
    var event =
        new StopVideoCallStatisticsEvent(
            authenticatedUser.getUserId(), UserRole.CONSULTANT, roomId);
    statisticsService.fireEvent(event);
  }

  private void stopOneToOneVideoCall(VideoRoomEntity videoRoomEntity) {
    videoRoomService.closeVideoRoom(videoRoomEntity);
    fireVideoCallStoppedStatisticsEvent(videoRoomEntity.getJitsiRoomId());
  }

  private void verifySessionStatus(ConsultantSessionDTO consultantSessionDto) {
    if (!IN_PROGRESS.getValue().equals(consultantSessionDto.getStatus())) {
      throw new BadRequestException("Session must be in progress", LogService::logWarning);
    }
  }

  private LiveEventMessage buildLiveEventMessage(
      String rcGroupId,
      String videoChatUrl,
      String initiatorRcUserId,
      String initiatorDisplayName) {
    var username =
        isNotBlank(initiatorDisplayName) ? initiatorDisplayName : authenticatedUser.getUsername();

    var videoCallRequestDto =
        new VideoCallRequestDTO()
            .videoCallUrl(videoChatUrl)
            .rcGroupId(rcGroupId)
            .initiatorRcUserId(initiatorRcUserId)
            .initiatorUsername(username);

    return new LiveEventMessage()
        .eventType(EventType.VIDEOCALLREQUEST)
        .eventContent(videoCallRequestDto);
  }
}
