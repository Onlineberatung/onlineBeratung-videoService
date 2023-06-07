package de.caritas.cob.videoservice.api.repository;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import de.caritas.cob.videoservice.api.model.VideoRoomEntity;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ExtendWith(SpringExtension.class)
@DataJpaTest
class VideoRoomRepositoryTest {

  private static final long EXISTING_ID = 1L;

  @Autowired private VideoRoomRepository videoRoomRepository;

  @Test
  void findById_Should_findRoomById() {
    // given, when
    Optional<VideoRoomEntity> videoRoomEntity = videoRoomRepository.findById(1L);
    // then
    assertThat(videoRoomEntity).isPresent();
  }

  @Test
  void findById_Should_findRoomByJitsiRoomId() {
    // given, when
    Optional<VideoRoomEntity> videoRoomEntityEntity =
        videoRoomRepository.findByJitsiRoomId("653ae5b9-a932-42a6-8935-d24010e3c5c1");
    // then
    assertThat(videoRoomEntityEntity).isPresent();
  }

  @Test
  void findById_Should_findRoomBySessionId() {
    // given, when
    Collection<VideoRoomEntity> videoRoomEntityEntity = videoRoomRepository.findBySessionId(2L);
    // then
    assertThat(videoRoomEntityEntity).hasSize(1);
  }

  @Test
  void create_Should_createVideoRoom() {
    // given
    VideoRoomEntity entity = new VideoRoomEntity();
    entity.setCreateDate(LocalDateTime.now());
    entity.setJitsiRoomId(UUID.randomUUID().toString());
    entity.setSessionId(1L);
    entity.setRocketChatRoomId("rocketChatRoomId");

    // when
    var saved = videoRoomRepository.save(entity);
    videoRoomRepository.flush();

    Optional<VideoRoomEntity> videoRoomEntity = videoRoomRepository.findById(saved.getId());
    // then
    assertThat(videoRoomEntity).isPresent();
    assertThat(videoRoomEntity).contains(entity);
  }

  @Test
  void save_Should_updateVideoRoom() {
    // given
    VideoRoomEntity videoRoomEntity = videoRoomRepository.findById(EXISTING_ID).get();

    // when
    LocalDateTime now = LocalDateTime.now();
    videoRoomEntity.setCloseDate(now);
    videoRoomRepository.save(videoRoomEntity);
    videoRoomRepository.flush();

    Optional<VideoRoomEntity> updated = videoRoomRepository.findById(EXISTING_ID);
    // then
    assertThat(updated).isPresent();
    assertThat(updated.get().getCloseDate()).isEqualTo(now);
  }
}
