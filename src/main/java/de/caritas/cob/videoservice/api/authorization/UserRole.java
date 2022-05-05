package de.caritas.cob.videoservice.api.authorization;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UserRole {

  USER("user"),
  CONSULTANT("consultant");

  private final String value;

}
