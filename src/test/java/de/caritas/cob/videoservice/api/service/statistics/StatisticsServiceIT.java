package de.caritas.cob.videoservice.api.service.statistics;

import static de.caritas.cob.videoservice.api.testhelper.TestConstants.ADVICESEEKER_ID;
import static de.caritas.cob.videoservice.api.testhelper.TestConstants.CONSULTANT_ID;
import static de.caritas.cob.videoservice.api.testhelper.TestConstants.SESSION_ID;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.apache.commons.codec.CharEncoding.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;

import de.caritas.cob.videoservice.VideoServiceApplication;
import de.caritas.cob.videoservice.api.service.statistics.event.StartVideoCallStatisticsEvent;
import de.caritas.cob.videoservice.offsetdatetime.CustomOffsetDateTime;
import de.caritas.cob.videoservice.statisticsservice.generated.web.model.EventType;
import de.caritas.cob.videoservice.statisticsservice.generated.web.model.UserRole;
import de.caritas.cob.videoservice.testconfig.RabbitMqTestConfig;
import java.io.IOException;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@ContextConfiguration(classes = RabbitMqTestConfig.class)
@SpringBootTest(classes = VideoServiceApplication.class)
public class StatisticsServiceIT {

  private static final long MAX_TIMEOUT_MILLIS = 5000;

  @Autowired StatisticsService statisticsService;
  @Autowired AmqpTemplate amqpTemplate;

  @Test
  public void fireEvent_Should_Send_ExpectedAssignSessionStatisticsEventMessageToQueue()
      throws IOException {

    UUID uuid = UUID.randomUUID();
    StartVideoCallStatisticsEvent startVideoCallStatisticsEvent =
        new StartVideoCallStatisticsEvent(
            CONSULTANT_ID, UserRole.CONSULTANT, SESSION_ID, uuid.toString(), ADVICESEEKER_ID);

    statisticsService.fireEvent(startVideoCallStatisticsEvent);
    Message message =
        amqpTemplate.receive(RabbitMqTestConfig.QUEUE_NAME_START_VIDEO_CALL, MAX_TIMEOUT_MILLIS);
    assert message != null;

    String expectedJson =
        "{"
            + "  \"adviceSeekerId\":\""
            + ADVICESEEKER_ID
            + "\","
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

    assertThat(
        extractBodyFromAmpQMessage(message),
        jsonEquals(expectedJson).whenIgnoringPaths("timestamp"));
  }

  private String extractBodyFromAmpQMessage(Message message) throws IOException {
    return IOUtils.toString(message.getBody(), UTF_8);
  }
}
