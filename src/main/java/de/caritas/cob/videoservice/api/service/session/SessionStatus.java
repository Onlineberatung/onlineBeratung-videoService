package de.caritas.cob.videoservice.api.service.session;

import lombok.Getter;

@Getter
public enum SessionStatus {

  INITIAL(0),
  NEW(1),
  IN_PROGRESS(2);

  private final Integer value;

  SessionStatus(Integer value) {
    this.value = value;
  }

}
