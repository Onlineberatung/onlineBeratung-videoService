package de.caritas.cob.videoservice.api.testhelper;

import static de.caritas.cob.videoservice.api.testhelper.TestConstants.RC_CHAT_ROOM_ID;

public class PathConstants {

  public static final String PATH_START_VIDEO_CALL = "/videocalls/new";
  public static final String PATH_REJECT_VIDEO_CALL = "/videocalls/reject";
  public static final String PATH_GET_WEB_TOKEN = "/videocalls/" + RC_CHAT_ROOM_ID + "/jwt";

}
