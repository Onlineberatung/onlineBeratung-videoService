package de.caritas.cob.videoservice.api.repository;

import de.caritas.cob.videoservice.api.model.VideoRoomEntity;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoRepository extends JpaRepository<VideoRoomEntity, Long> {

  Optional<VideoRoomEntity> findByJitsiRoomId(Long jitsiRoomId);

  Collection<VideoRoomEntity> findBySessionId(Long sessionId);
}
