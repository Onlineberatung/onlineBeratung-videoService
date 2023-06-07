package de.caritas.cob.videoservice.api.service.video;

import de.caritas.cob.videoservice.api.model.VideoRoomEntity;
import de.caritas.cob.videoservice.api.repository.VideoRoomRepository;
import java.time.LocalDateTime;
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
      Long sessionId, String rocketChatRoomId, String jitsiRoomId) {
    var videoRoom = new VideoRoomEntity();
    videoRoom.setSessionId(sessionId);
    videoRoom.setRocketChatRoomId(rocketChatRoomId);
    videoRoom.setJitsiRoomId(jitsiRoomId);
    videoRoom.setCreateDate(LocalDateTime.now());
    return videoRoomRepository.save(videoRoom);
  }

  public VideoRoomEntity createGroupVideoRoom(
      Long groupChatId, String rocketChatRoomId, String jitsiRoomId) {
    var videoRoom = new VideoRoomEntity();
    videoRoom.setGroupChatId(groupChatId);
    videoRoom.setJitsiRoomId(jitsiRoomId);
    videoRoom.setRocketChatRoomId(rocketChatRoomId);
    videoRoom.setCreateDate(LocalDateTime.now());
    videoRoom.setCloseDate(null);
    return videoRoomRepository.save(videoRoom);
  }

  public Optional<VideoRoomEntity> findLatestActiveRoomForSessionId(Long sessionId) {
    var bySessionId = videoRoomRepository.findBySessionId(sessionId);
    return bySessionId.stream()
        .filter(x -> x.getCloseDate() == null)
        .sorted(Comparator.comparing(VideoRoomEntity::getCreateDate).reversed())
        .findFirst();
  }

  public void closeVideoRoom(VideoRoomEntity videoRoomEntity) {
    videoRoomEntity.setCloseDate(LocalDateTime.now());
    videoRoomRepository.save(videoRoomEntity);
  }

  public Optional<VideoRoomEntity> findByJitsiRoomId(String roomId) {
    return videoRoomRepository.findByJitsiRoomId(roomId);
  }
}
