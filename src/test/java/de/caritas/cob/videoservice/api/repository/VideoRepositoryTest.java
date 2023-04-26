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
class VideoRepositoryTest {

  private static final long EXISTING_ID = 1L;

  @Autowired private VideoRepository videoRepository;

  @Test
  void findById_Should_findRoomById() {
    // given, when
    Optional<VideoRoomEntity> videoRoomEntity = videoRepository.findById(1L);
    // then
    assertThat(videoRoomEntity).isPresent();
  }

  @Test
  void findById_Should_findRoomByJitsiRoomId() {
    // given, when
    Optional<VideoRoomEntity> videoRoomEntityEntity = videoRepository.findByJitsiRoomId(1L);
    // then
    assertThat(videoRoomEntityEntity).isPresent();
  }

  @Test
  void findById_Should_findRoomBySessionId() {
    // given, when
    Collection<VideoRoomEntity> videoRoomEntityEntity = videoRepository.findBySessionId(2L);
    // then
    assertThat(videoRoomEntityEntity).hasSize(1);
  }

  @Test
  void create_Should_createVideoRoom() {
    // given
    VideoRoomEntity entity = new VideoRoomEntity();
    entity.setVideolink("https://videolink." + UUID.randomUUID());
    entity.setCreateDate(LocalDateTime.now());
    entity.setJitsiRoomId(2L);
    entity.setSessionId(1L);

    // when
    var saved = videoRepository.save(entity);
    videoRepository.flush();

    Optional<VideoRoomEntity> videoRoomEntity = videoRepository.findById(saved.getId());
    // then
    assertThat(videoRoomEntity).isPresent();
    assertThat(videoRoomEntity).contains(entity);
  }

  @Test
  void save_Should_updateVideoRoom() {
    // given
    VideoRoomEntity videoRoomEntity = videoRepository.findById(EXISTING_ID).get();

    // when
    LocalDateTime now = LocalDateTime.now();
    videoRoomEntity.setUpdateDate(now);
    videoRepository.save(videoRoomEntity);
    videoRepository.flush();

    Optional<VideoRoomEntity> updated = videoRepository.findById(EXISTING_ID);
    // then
    assertThat(updated).isPresent();
    assertThat(updated.get().getUpdateDate()).isEqualTo(now);
  }
}
