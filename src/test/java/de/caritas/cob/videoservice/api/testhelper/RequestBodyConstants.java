package de.caritas.cob.videoservice.api.testhelper;

import static de.caritas.cob.videoservice.api.testhelper.TestConstants.SESSION_ID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.videoservice.api.model.CreateVideoCallDTO;

public class RequestBodyConstants {

  public static final CreateVideoCallDTO CREATE_VIDEO_CALL_DTO =
      new CreateVideoCallDTO().sessionId(SESSION_ID);
  public static String VALID_START_VIDEO_CALL_BODY = null;

  static {
    try {
      VALID_START_VIDEO_CALL_BODY = new ObjectMapper().writeValueAsString(CREATE_VIDEO_CALL_DTO);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
  }
}
