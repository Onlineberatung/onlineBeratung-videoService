package de.caritas.cob.videoservice.api.helper;

import static de.caritas.cob.videoservice.api.testhelper.TestConstants.CONSULTANT_ID;
import static de.caritas.cob.videoservice.api.testhelper.TestConstants.SESSION_ID;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.videoservice.api.service.LogService;
import de.caritas.cob.videoservice.offsetdatetime.CustomOffsetDateTime;
import de.caritas.cob.videoservice.statisticsservice.generated.web.model.EventType;
import de.caritas.cob.videoservice.statisticsservice.generated.web.model.StartVideoCallStatisticsEventMessage;
import de.caritas.cob.videoservice.statisticsservice.generated.web.model.UserRole;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import org.junit.Test;
import org.mockito.Mockito;

public class JsonHelperTest {

  @Test
  public void serialize_Should_returnOptionalWithSerializedObject() {

    OffsetDateTime offsetDateTime = CustomOffsetDateTime.nowInUtc();
    UUID uuid = UUID.randomUUID();

    StartVideoCallStatisticsEventMessage startVideoCallStatisticsEventMessage =
        new StartVideoCallStatisticsEventMessage()
            .eventType(EventType.START_VIDEO_CALL)
            .sessionId(SESSION_ID)
            .userId(CONSULTANT_ID)
            .userRole(UserRole.CONSULTANT)
            .videoCallUuid(uuid.toString())
            .timestamp(offsetDateTime);

    Optional<String> result =
        JsonHelper.serializeWithOffsetDateTimeAsString(
            startVideoCallStatisticsEventMessage, LogService::logInternalServerError);

    assertThat(result.isPresent(), is(true));

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
            + offsetDateTime.format(DateTimeFormatter.ISO_DATE_TIME)
            + "\","
            + "  \"eventType\":\""
            + EventType.START_VIDEO_CALL
            + "\","
            + "  \"videoCallUuid\":\""
            + uuid
            + "\""
            + "}";

    assertThat(result.get(), jsonEquals(expectedJson));
  }

  @Test
  public void serialize_Should_returnOptionalEmpty_When_jsonStringCanNotBeConverted()
      throws JsonProcessingException {

    ObjectMapper om = Mockito.spy(new ObjectMapper());
    when(om.writeValueAsString(Object.class)).thenThrow(new JsonProcessingException("") {});

    Optional<String> result =
        JsonHelper.serializeWithOffsetDateTimeAsString(
            new Object(), LogService::logInternalServerError);

    assertThat(result.isPresent(), is(false));
  }
}
