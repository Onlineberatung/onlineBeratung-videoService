package de.caritas.cob.videoservice.api.service.statistics;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import de.caritas.cob.videoservice.api.service.LogService;
import de.caritas.cob.videoservice.api.service.statistics.event.StartVideoCallStatisticsEvent;
import de.caritas.cob.videoservice.statisticsservice.generated.web.model.EventType;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;

@RunWith(MockitoJUnitRunner.class)
public class StatisticsServiceTest {

  private static final String FIELD_NAME_STATISTICS_ENABLED = "statisticsEnabled";
  private static final String FIELD_NAME_RABBIT_EXCHANGE_NAME = "rabbitMqExchangeName";
  private static final String RABBIT_EXCHANGE_NAME = "exchange";
  private static final String PAYLOAD = "payload";

  private StartVideoCallStatisticsEvent startVideoCallStatisticsEvent;
  private final EventType eventType = EventType.START_VIDEO_CALL;

  @InjectMocks
  private StatisticsService statisticsService;
  @Mock
  private AmqpTemplate amqpTemplate;
  @Mock
  Logger logger;

  @Before
  public void setup() {
    startVideoCallStatisticsEvent = Mockito.mock(StartVideoCallStatisticsEvent.class);
    when(startVideoCallStatisticsEvent.getEventType()).thenReturn(eventType);
    when(startVideoCallStatisticsEvent.getPayload()).thenReturn(Optional.of(PAYLOAD));
    setInternalState(LogService.class, "LOGGER", logger);
    setField(statisticsService, FIELD_NAME_RABBIT_EXCHANGE_NAME, RABBIT_EXCHANGE_NAME);
  }

  @Test
  public void fireEvent_Should_NotSendStatisticsMessage_WhenStatisticsIsDisabled() {

    setField(statisticsService, FIELD_NAME_STATISTICS_ENABLED, false);
    statisticsService.fireEvent(startVideoCallStatisticsEvent);
    verify(amqpTemplate, times(0))
        .convertAndSend(eq(RABBIT_EXCHANGE_NAME), anyString(), anyString());
  }

  @Test
  public void fireEvent_Should_SendStatisticsMessage_WhenStatisticsIsEnabled() {

    setField(statisticsService, FIELD_NAME_STATISTICS_ENABLED, true);
    when(startVideoCallStatisticsEvent.getEventType()).thenReturn(eventType);
    when(startVideoCallStatisticsEvent.getPayload()).thenReturn(Optional.of(PAYLOAD));

    statisticsService.fireEvent(startVideoCallStatisticsEvent);
    verify(amqpTemplate, times(1))
        .convertAndSend(eq(RABBIT_EXCHANGE_NAME), anyString(), eq(buildPayloadMessage()));
  }

  @Test
  public void fireEvent_Should_LogWarning_WhenPayloadIsEmpty() {

    setField(statisticsService, FIELD_NAME_STATISTICS_ENABLED, true);
    when(startVideoCallStatisticsEvent.getPayload()).thenReturn(Optional.empty());
    statisticsService.fireEvent(startVideoCallStatisticsEvent);
    verify(logger, times(1)).warn(anyString(), anyString(), anyString());
  }

  @Test
  public void fireEvent_Should_UseEventTypeAsTopicAndSendPayloadOfEvent() {

    setField(statisticsService, FIELD_NAME_STATISTICS_ENABLED, true);
    statisticsService.fireEvent(startVideoCallStatisticsEvent);
    verify(amqpTemplate, times(1))
        .convertAndSend(RABBIT_EXCHANGE_NAME, eventType.toString(), buildPayloadMessage());
  }

  private org.springframework.amqp.core.Message buildPayloadMessage() {
    return MessageBuilder.withBody(PAYLOAD.getBytes(StandardCharsets.UTF_8))
        .setContentType(MessageProperties.CONTENT_TYPE_JSON)
        .build();
  }

}
