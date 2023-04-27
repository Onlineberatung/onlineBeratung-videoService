package de.caritas.cob.videoservice.api.service.video;

import de.caritas.cob.videoservice.api.model.VideoRoomEntity;
import de.caritas.cob.videoservice.api.repository.VideoRoomRepository;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Url generator for video call urls. */
@Service
@RequiredArgsConstructor
public class VideoRoomService {

  private final @NonNull VideoRoomRepository videoRoomRepository;

  public VideoRoomEntity createOneToOneVideoRoom(
      Long sessionId, String jitsiRoomId, String videoLink) {
    var videoRoom = new VideoRoomEntity();
    videoRoom.setSessionId(sessionId);
    videoRoom.setJitsiRoomId(jitsiRoomId);
    videoRoom.setVideolink(videoLink);
    return videoRoomRepository.save(videoRoom);
  }

  public VideoRoomEntity createGroupVideoRoom(
      Long groupChatId, String jitsiRoomId, String videoLink) {
    var videoRoom = new VideoRoomEntity();
    videoRoom.setGroupChatId(groupChatId);
    videoRoom.setJitsiRoomId(jitsiRoomId);
    videoRoom.setVideolink(videoLink);
    return videoRoomRepository.save(videoRoom);
  }

  public Optional<VideoRoomEntity> findLatestActiveRoomForSessionId(Long sessionId) {
    Collection<VideoRoomEntity> bySessionId = videoRoomRepository.findBySessionId(sessionId);
    return bySessionId.stream()
        .filter(x -> x.getCloseDate() == null)
        .sorted(Comparator.comparing(VideoRoomEntity::getCreateDate).reversed())
        .findFirst();
  }

  public void closeVideoRoom(String roomId) {
    videoRoomRepository.findByJitsiRoomId(roomId).ifPresent(this::closeVideoRoom);
  }

  private void closeVideoRoom(VideoRoomEntity videoRoomEntity) {
    videoRoomEntity.setCloseDate(LocalDateTime.now());
    videoRoomRepository.save(videoRoomEntity);
  }
}
