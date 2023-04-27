package de.caritas.cob.videoservice.api.repository;

import de.caritas.cob.videoservice.api.model.VideoRoomEntity;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoRoomRepository extends JpaRepository<VideoRoomEntity, Long> {

  Optional<VideoRoomEntity> findByJitsiRoomId(String jitsiRoomId);

  Collection<VideoRoomEntity> findBySessionId(Long sessionId);
}
