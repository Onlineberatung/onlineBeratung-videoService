package de.caritas.cob.videoservice.api.controller;

import static de.caritas.cob.videoservice.api.testhelper.TestConstants.AUTHORITY_CONSULTANT;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.caritas.cob.videoservice.api.authorization.VideoUser;
import java.util.UUID;
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

  @Autowired
  private MockMvc mockMvc;

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
    givenARoomId();
    givenAValidAuthUser();

    mockMvc.perform(
        post("/videocalls/stop/" + roomId)
            .cookie(CSRF_COOKIE)
            .header(CSRF_HEADER, CSRF_VALUE)
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(authorities = AUTHORITY_CONSULTANT)
  void stopVideoCallShouldReturnClientErrorOnNonUuidRoomId() throws Exception {
    givenAnInvalidRoomId();
    givenAValidAuthUser();

    mockMvc.perform(
            post("/videocalls/stop/" + roomId)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().is4xxClientError());
  }

  private void givenARoomId() {
    roomId = UUID.randomUUID().toString();
  }

  private void givenAnInvalidRoomId() {
    roomId = RandomStringUtils.randomAlphabetic(16);
  }

  private void givenAValidAuthUser() {
    when(authenticatedUser.getUserId()).thenReturn(RandomStringUtils.randomAlphabetic(16));
  }
}
