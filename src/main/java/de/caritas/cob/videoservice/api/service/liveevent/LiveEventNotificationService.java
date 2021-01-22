package de.caritas.cob.videoservice.api.service.liveevent;

import de.caritas.cob.videoservice.liveservice.generated.web.LiveControllerApi;
import de.caritas.cob.videoservice.liveservice.generated.web.model.LiveEventMessage;
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


  /**
   * Sends a live event to the LiveService to inform the video call receivers.
   *
   * @param liveEventMessage {@link LiveEventMessage}
   * @param userIds          list of receiver user Ids
   */
  public void sendVideoCallRequestLiveEvent(LiveEventMessage liveEventMessage,
      List<String> userIds) {
    liveControllerApi.sendLiveEvent(userIds, liveEventMessage);
  }
}