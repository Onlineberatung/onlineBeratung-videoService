package de.caritas.cob.videoservice.api.service.video;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Lists;
import de.caritas.cob.videoservice.api.model.VideoRoomEntity;
import de.caritas.cob.videoservice.api.repository.VideoRoomRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VideoRoomServiceTest {

  @InjectMocks VideoRoomService videoRoomService;

  @Mock VideoRoomRepository videoRoomRepository;

  @Test
  void createOneToOneVideoRoom_Should_ReturnsCreatedVideoRoom() {
    // When
    String jitsiRoomId = UUID.randomUUID().toString();
    String rocketChatRoomId = UUID.randomUUID().toString();
    var videoRoomResult =
        videoRoomService.createOneToOneVideoRoom(1L, rocketChatRoomId, jitsiRoomId);

    // Then
    ArgumentCaptor<VideoRoomEntity> captor = ArgumentCaptor.forClass(VideoRoomEntity.class);
    Mockito.verify(videoRoomRepository).save(captor.capture());
    VideoRoomEntity capturedValue = captor.getValue();
    assertThat(capturedValue.getJitsiRoomId()).isEqualTo(jitsiRoomId);
    assertThat(capturedValue.getSessionId()).isEqualTo(1L);
    assertThat(capturedValue.getRocketChatRoomId()).isEqualTo(rocketChatRoomId);
    assertThat(capturedValue.getCreateDate()).isNotNull();
  }

  @Test
  void createGroupVideoRoom_Should_ReturnsCreatedVideoRoom() {
    // When
    String jitsiRoomId = UUID.randomUUID().toString();
    String rocketChatRoomId = UUID.randomUUID().toString();
    var videoRoomResult = videoRoomService.createGroupVideoRoom(1L, rocketChatRoomId, jitsiRoomId);

    // Then
    ArgumentCaptor<VideoRoomEntity> captor = ArgumentCaptor.forClass(VideoRoomEntity.class);
    Mockito.verify(videoRoomRepository).save(captor.capture());
    VideoRoomEntity capturedValue = captor.getValue();
    assertThat(capturedValue.getJitsiRoomId()).isEqualTo(jitsiRoomId);
    assertThat(capturedValue.getGroupChatId()).isEqualTo(1L);
  }

  @Test
  void findLatestRoomForSessionId_Should_ReturnLatestRoom() {
    // Given
    LocalDateTime olderDate = LocalDateTime.now().plusDays(1);
    Mockito.when(videoRoomRepository.findBySessionId(1L))
        .thenReturn(
            Lists.newArrayList(
                giveEntityWithCreationDate(LocalDateTime.now()),
                giveEntityWithCreationDate(olderDate)));
    // when
    var videoRoomResult = videoRoomService.findLatestActiveRoomForSessionId(1L);

    // Then
    assertThat(videoRoomResult.get().getCreateDate()).isEqualTo(olderDate);
  }

  private VideoRoomEntity giveEntityWithCreationDate(LocalDateTime localDateTime) {
    var entity = new VideoRoomEntity();
    entity.setCreateDate(localDateTime);
    return entity;
  }
}
