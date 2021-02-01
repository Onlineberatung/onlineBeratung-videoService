package de.caritas.cob.videoservice.api.service.video.jwt.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VideoCallUrlPair {

  private final String basicVideoUrl;
  private final String userVideoUrl;

}
