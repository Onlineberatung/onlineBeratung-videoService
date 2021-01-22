package de.caritas.cob.videoservice.api.controller;

import static de.caritas.cob.videoservice.api.testhelper.PathConstants.PATH_START_VIDEO_CALL;
import static de.caritas.cob.videoservice.api.testhelper.RequestBodyConstants.VALID_START_VIDEO_CALL_BODY;
import static de.caritas.cob.videoservice.api.testhelper.TestConstants.CSRF_COOKIE;
import static de.caritas.cob.videoservice.api.testhelper.TestConstants.CSRF_HEADER;
import static de.caritas.cob.videoservice.api.testhelper.TestConstants.CSRF_VALUE;
import static de.caritas.cob.videoservice.api.testhelper.TestConstants.ROLE_CONSULTANT;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.caritas.cob.videoservice.api.facade.StartVideoCallFacade;
import javax.servlet.http.Cookie;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@SpringBootTest
@AutoConfigureMockMvc
public class VideoControllerAuthorizationIT {

  @Autowired
  private MockMvc mvc;

  @MockBean
  private StartVideoCallFacade startVideoCallFacade;

  private Cookie csrfCookie;

  @Before
  public void setUp() {
    csrfCookie = new Cookie(CSRF_COOKIE, CSRF_VALUE);
  }

  @Test
  @WithMockUser()
  public void createVideoCall_Should_ReturnForbiddenAndCallNoMethods_WhenNoConsultantDefaultRole()
      throws Exception {

    mvc.perform(post(PATH_START_VIDEO_CALL)
        .cookie(csrfCookie)
        .header(CSRF_HEADER, CSRF_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(VALID_START_VIDEO_CALL_BODY)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(startVideoCallFacade);
  }

  @Test
  @WithMockUser(roles = {ROLE_CONSULTANT})
  public void createVideoCall_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(post(PATH_START_VIDEO_CALL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(VALID_START_VIDEO_CALL_BODY)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(startVideoCallFacade);
  }
}
