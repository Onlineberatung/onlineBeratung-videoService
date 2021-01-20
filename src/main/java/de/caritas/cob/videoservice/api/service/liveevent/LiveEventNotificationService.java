package de.caritas.cob.videoservice.api.service.liveevent;

import de.caritas.cob.videoservice.api.authorization.AuthenticatedUser;
import de.caritas.cob.videoservice.liveservice.generated.web.LiveControllerApi;
import de.caritas.cob.videoservice.liveservice.generated.web.model.VideoCallRequestDTO;
import de.caritas.cob.videoservice.liveservice.generated.web.model.VideoEventType;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service class to provide live event triggers to the LiveService.
 */
@Service
@RequiredArgsConstructor
public class LiveEventNotificationService {

  private final @NonNull LiveControllerApi liveControllerApi;
  private final @NonNull AuthenticatedUser authenticatedUser;

  /**
   * Sends a live event to the LiveService to inform the video call receivers.
   *
   * @param videoChatUrl URL to the video call
   * @param rcGroupId    Rocket.Chat group Id of the outbound session
   * @param userIds      list of receiver user Ids
   */
  public void sendVideoCallRequestLiveEvent(String videoChatUrl, String rcGroupId,
      List<String> userIds) {

    VideoCallRequestDTO videoCallRequestDto = new VideoCallRequestDTO()
        .eventType(VideoEventType.VIDEOCALLREQUEST)
        .videoChatUrl(videoChatUrl)
        .rcRoomId(rcGroupId)
        .usernameConsultant(authenticatedUser.getUsername());

    // TODO
    //liveControllerApi.videoCallRequest(userIds, videoCallRequestDto);
  }
}
