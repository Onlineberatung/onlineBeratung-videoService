package de.caritas.cob.videoservice.api.service.message;

import static org.assertj.core.api.Assertions.assertThat;

import de.caritas.cob.videoservice.api.model.VideoRoomEntity;
import de.caritas.cob.videoservice.messageservice.generated.web.model.AliasMessageDTO;
import de.caritas.cob.videoservice.messageservice.generated.web.model.VideoCallMessageDTO.EventTypeEnum;
import java.time.LocalDateTime;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

  @InjectMocks MessageService messageService;

  @Test
  void getMessageContent_Should_CalculateCallDuration() {
    // given
    VideoRoomEntity videoRoomEntity = new VideoRoomEntity();
    videoRoomEntity.setCreateDate(LocalDateTime.now().minusMinutes(1));
    // when
    JSONObject messageContent =
        this.messageService.getMessageContent(
            "user",
            videoRoomEntity,
            "guestVideoCallUrl",
            "title",
            new AliasMessageDTO(),
            EventTypeEnum.CALL_STARTED);
    // then
    assertThat((Long) messageContent.get("durationSeconds")).isGreaterThanOrEqualTo(60L);
  }

  @Test
  void getMessageContent_Should_CalculateCallDurationToZero_When_CreateDateIsNotSet() {
    // given
    VideoRoomEntity videoRoomEntity = new VideoRoomEntity();
    // when
    JSONObject messageContent =
        this.messageService.getMessageContent(
            "user",
            videoRoomEntity,
            "guestVideoCallUrl",
            "title",
            new AliasMessageDTO(),
            EventTypeEnum.CALL_STARTED);
    // then
    assertThat((Long) messageContent.get("durationSeconds")).isZero();
  }
}
