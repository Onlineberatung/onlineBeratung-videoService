package de.caritas.cob.videoservice.api.testhelper;

import static de.caritas.cob.videoservice.api.testhelper.TestConstants.SESSION_ID;

public class RequestBodyConstants {

  public static final String VALID_START_VIDEO_CALL_BODY =
      "{\"sessionId\": \"" + SESSION_ID + "\"}";
}
