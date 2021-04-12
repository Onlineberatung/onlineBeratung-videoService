package de.caritas.cob.videoservice.api.service.video.jwt.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VideoCallUrls {

  private final String guestVideoUrl;
  private final String userVideoUrl;
  private final String moderatorVideoUrl;
}
