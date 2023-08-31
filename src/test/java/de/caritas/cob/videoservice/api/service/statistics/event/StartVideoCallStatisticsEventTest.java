package de.caritas.cob.videoservice.api.service.statistics.event;

import static de.caritas.cob.videoservice.api.testhelper.TestConstants.CONSULTANT_ID;
import static de.caritas.cob.videoservice.api.testhelper.TestConstants.SESSION_ID;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import de.caritas.cob.videoservice.offsetdatetime.CustomOffsetDateTime;
import de.caritas.cob.videoservice.statisticsservice.generated.web.model.EventType;
import de.caritas.cob.videoservice.statisticsservice.generated.web.model.UserRole;
import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StartVideoCallStatisticsEventTest {

  private StartVideoCallStatisticsEvent startVideoCallStatisticsEvent;
  private UUID uuid;

  private UUID adviceSeekerUuid;

  @Before
  public void setup() {
    uuid = UUID.randomUUID();
    startVideoCallStatisticsEvent =
        new StartVideoCallStatisticsEvent(
            CONSULTANT_ID,
            UserRole.CONSULTANT,
            SESSION_ID,
            uuid.toString(),
            adviceSeekerUuid.toString());
  }

  @Test
  public void getEventType_Should_ReturnEventTypeCreateMessage() {

    assertThat(startVideoCallStatisticsEvent.getEventType(), is(EventType.START_VIDEO_CALL));
  }

  @Test
  public void getPayload_Should_ReturnValidJsonPayload() {

    String expectedJson =
        "{"
            + "  \"userId\":\""
            + CONSULTANT_ID
            + "\","
            + "  \"userRole\":\""
            + UserRole.CONSULTANT
            + "\","
            + "  \"sessionId\":"
            + SESSION_ID
            + ","
            + "  \"timestamp\":\""
            + CustomOffsetDateTime.nowInUtc()
            + "\","
            + "  \"eventType\":\""
            + EventType.START_VIDEO_CALL
            + "\","
            + "  \"videoCallUuid\":\""
            + uuid
            + "\""
            + "}";

    Optional<String> result = startVideoCallStatisticsEvent.getPayload();

    assertThat(result.isPresent(), is(true));
    assertThat(result.get(), jsonEquals(expectedJson).whenIgnoringPaths("timestamp"));
  }
}
