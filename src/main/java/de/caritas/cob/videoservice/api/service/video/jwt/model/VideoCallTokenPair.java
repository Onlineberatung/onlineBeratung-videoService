package de.caritas.cob.videoservice.api.service.video.jwt.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VideoCallTokenPair {

  private final String basicToken;
  private final String userToken;

}
