package de.caritas.cob.videoservice.api.controller;

import static de.caritas.cob.videoservice.api.testhelper.TestConstants.AUTHORITY_CONSULTANT;
import static de.caritas.cob.videoservice.api.testhelper.TestConstants.AUTHORITY_JITSI_TECHNICAL;
import static de.caritas.cob.videoservice.api.testhelper.TestConstants.AUTHORITY_USER;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.caritas.cob.videoservice.api.authorization.VideoUser;
import javax.servlet.http.Cookie;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("testing")
class VideoControllerE2eIT {

  private static final String CSRF_HEADER = "csrfHeader";
  private static final String CSRF_VALUE = "test";
  private static final Cookie CSRF_COOKIE = new Cookie("csrfCookie", CSRF_VALUE);
  private static final String EXISTING_ROOM_ID = "653ae5b9-a932-42a6-8935-d24010e3c5c1";
  public static final String MUC_MEET_JITSI_SUFFIX = "@muc.meet.jitsi";

  @Autowired private MockMvc mockMvc;

  @MockBean
  @SuppressWarnings("unused")
  private VideoUser authenticatedUser;

  private String roomId;

  @AfterEach
  void reset() {
    roomId = null;
  }

  @Test
  @WithMockUser(authorities = AUTHORITY_CONSULTANT)
  void stopVideoCallShouldReturnNoContent() throws Exception {
    givenAValidAuthUser();

    mockMvc
        .perform(
            post("/videocalls/stop/" + EXISTING_ROOM_ID)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(authorities = AUTHORITY_USER)
  void stopVideoCallShouldNotAllowToStopIfCalledAsUserAuthority() throws Exception {
    givenAValidAuthUser();

    mockMvc
        .perform(
            post("/videocalls/stop/" + EXISTING_ROOM_ID)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(authorities = AUTHORITY_JITSI_TECHNICAL)
  void stopVideoCallShouldReturnNoContentIfJitsiTechnicalUserRole() throws Exception {
    givenAValidAuthUser();

    mockMvc
        .perform(
            post("/videocalls/event/stop/" + EXISTING_ROOM_ID + MUC_MEET_JITSI_SUFFIX)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(authorities = AUTHORITY_CONSULTANT)
  void stopVideoCallShouldReturnClientErrorOnNonUuidRoomId() throws Exception {
    givenAnInvalidRoomId();
    givenAValidAuthUser();

    mockMvc
        .perform(
            post("/videocalls/stop/" + roomId)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is4xxClientError());
  }

  private void givenAnInvalidRoomId() {
    roomId = RandomStringUtils.randomAlphabetic(16);
  }

  private void givenAValidAuthUser() {
    when(authenticatedUser.getUserId()).thenReturn(RandomStringUtils.randomAlphabetic(16));
  }
}
