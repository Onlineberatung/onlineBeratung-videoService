package de.caritas.cob.videoservice.api.authorization;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UserRole {
  USER("user"),
  CONSULTANT("consultant"),
  JITSI_TECHNICAL("jitsi-technical");

  private final String value;
}
