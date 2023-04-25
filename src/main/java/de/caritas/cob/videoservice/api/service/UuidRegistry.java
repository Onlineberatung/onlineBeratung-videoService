package de.caritas.cob.videoservice.api.service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Registry to hold and handle all current video call {@link UUID}s. */
@Component
public class UuidRegistry {

  private static final List<UUID> GENERATED_UUIDS = new CopyOnWriteArrayList<>();

  /**
   * Generates an unique {@link UUID} string that is currently not registered and adds it to the
   * list.
   *
   * @return registry unique {@link UUID}
   */
  public synchronized String generateUniqueUuid() {
    UUID uuid;
    do {
      uuid = UUID.randomUUID();
    } while (GENERATED_UUIDS.contains(uuid));

    GENERATED_UUIDS.add(uuid);
    return uuid.toString();
  }

  /** Clears the {@link UUID} registry. */
  @Scheduled(cron = "${video.call.uuid.registry.cron}")
  public synchronized void cleanUpUuidRegistry() {
    GENERATED_UUIDS.clear();
    LogService.logInfo("UUIDs have been reset!");
  }
}
