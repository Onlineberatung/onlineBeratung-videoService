package de.caritas.cob.videoservice.api.service.statistics.event;

import de.caritas.cob.videoservice.api.helper.JsonHelper;
import de.caritas.cob.videoservice.api.service.LogService;
import de.caritas.cob.videoservice.offsetdatetime.CustomOffsetDateTime;
import de.caritas.cob.videoservice.statisticsservice.generated.web.model.EventType;
import de.caritas.cob.videoservice.statisticsservice.generated.web.model.StopVideoCallStatisticsEventMessage;
import de.caritas.cob.videoservice.statisticsservice.generated.web.model.UserRole;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StopVideoCallStatisticsEvent implements StatisticsEvent {

  private static final EventType EVENT_TYPE = EventType.STOP_VIDEO_CALL;

  private @NonNull String userId;
  private @NonNull UserRole userRole;
  private @NonNull String videoCallUuid;

  /** {@inheritDoc} */
  @Override
  public Optional<String> getPayload() {
    return JsonHelper.serializeWithOffsetDateTimeAsString(
        createStopVideoCallStatisticsEventMessage(), LogService::logStatisticsEventError);
  }

  /** {@inheritDoc} */
  @Override
  public EventType getEventType() {
    return EVENT_TYPE;
  }

  private StopVideoCallStatisticsEventMessage createStopVideoCallStatisticsEventMessage() {
    return new StopVideoCallStatisticsEventMessage()
        .eventType(EVENT_TYPE)
        .userId(userId)
        .userRole(userRole)
        .videoCallUuid(videoCallUuid)
        .timestamp(CustomOffsetDateTime.nowInUtc());
  }
}
