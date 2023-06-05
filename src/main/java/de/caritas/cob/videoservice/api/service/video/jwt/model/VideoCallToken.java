package de.caritas.cob.videoservice.api.service.video.jwt.model;

import lombok.Builder;
import lombok.Getter;

/** Video call token for anonymous user and asker (containing user name). */
@Getter
@Builder
public class VideoCallToken {

  private final String guestToken;
  private final String userRelatedToken;
}
