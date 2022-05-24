package de.caritas.cob.videoservice.api.controller;

import static de.caritas.cob.videoservice.api.testhelper.PathConstants.PATH_GET_WEB_TOKEN;
import static de.caritas.cob.videoservice.api.testhelper.PathConstants.PATH_REJECT_VIDEO_CALL;
import static de.caritas.cob.videoservice.api.testhelper.PathConstants.PATH_START_VIDEO_CALL;
import static de.caritas.cob.videoservice.api.testhelper.RequestBodyConstants.VALID_START_VIDEO_CALL_BODY;
import static de.caritas.cob.videoservice.api.testhelper.TestConstants.AUTHORITY_CONSULTANT;
import static de.caritas.cob.videoservice.api.testhelper.TestConstants.AUTHORITY_USER;
import static de.caritas.cob.videoservice.api.testhelper.TestConstants.CREATE_VIDEO_CALL_RESPONSE_DTO;
import static de.caritas.cob.videoservice.api.testhelper.TestConstants.CSRF_COOKIE;
import static de.caritas.cob.videoservice.api.testhelper.TestConstants.CSRF_HEADER;
import static de.caritas.cob.videoservice.api.testhelper.TestConstants.CSRF_VALUE;
import static de.caritas.cob.videoservice.api.testhelper.TestConstants.RC_CHAT_ROOM_ID;
import static de.caritas.cob.videoservice.api.testhelper.TestConstants.RC_USER_ID_HEADER;
import static de.caritas.cob.videoservice.api.testhelper.TestConstants.RC_USER_ID_VALUE;
import static de.caritas.cob.videoservice.api.testhelper.TestConstants.SESSION_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.caritas.cob.videoservice.api.facade.StartVideoCallFacade;
import de.caritas.cob.videoservice.api.model.RejectVideoCallDTO;
import de.caritas.cob.videoservice.api.service.RejectVideoCallService;
import de.caritas.cob.videoservice.api.service.video.jwt.TokenGeneratorService;
import javax.servlet.http.Cookie;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
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

  @MockBean
  private RejectVideoCallService rejectVideoCallService;

  @MockBean
  private TokenGeneratorService tokenGeneratorService;

  private final Cookie csrfCookie = new Cookie(CSRF_COOKIE, CSRF_VALUE);


  @Test
  @WithMockUser(authorities = AUTHORITY_CONSULTANT)
  public void createVideoCall_Should_ReturnCreated_When_EverythingSucceeded() throws Exception {

    when(startVideoCallFacade.startVideoCall(eq(SESSION_ID), anyString())).thenReturn(
        CREATE_VIDEO_CALL_RESPONSE_DTO);

    mvc.perform(post(PATH_START_VIDEO_CALL)
            .cookie(csrfCookie)
            .header(CSRF_HEADER, CSRF_VALUE)
            .header(RC_USER_ID_HEADER, RC_USER_ID_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(VALID_START_VIDEO_CALL_BODY)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated());
  }

  @Test
  public void createVideoCall_Should_ReturnUnauthorized_When_AuthorizationIsMissing()
      throws Exception {

    when(startVideoCallFacade.startVideoCall(eq(SESSION_ID), anyString())).thenReturn(
        CREATE_VIDEO_CALL_RESPONSE_DTO);

    mvc.perform(post(PATH_START_VIDEO_CALL)
            .cookie(csrfCookie)
            .header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(VALID_START_VIDEO_CALL_BODY)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser()
  public void createVideoCall_Should_ReturnForbiddenAndCallNoMethods_WhenNoConsultantDefaultAuthority()
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
  @WithMockUser(authorities = AUTHORITY_CONSULTANT)
  public void createVideoCall_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {

    mvc.perform(post(PATH_START_VIDEO_CALL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(VALID_START_VIDEO_CALL_BODY)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(startVideoCallFacade);
  }

  @Test
  @WithMockUser(authorities = {AUTHORITY_USER})
  public void rejectVideoCall_Should_ReturnForbiddenAndCallNoMethods_WhenNoCsrfTokens()
      throws Exception {
    String content = new ObjectMapper().writeValueAsString(new RejectVideoCallDTO()
        .rcGroupId("rcGroupId")
        .initiatorUsername("username")
        .initiatorRcUserId("rcUserId"));

    mvc.perform(post(PATH_REJECT_VIDEO_CALL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(content)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(rejectVideoCallService);
  }

  @Test
  @WithMockUser(authorities = {"NO_AUTHORITY"})
  public void rejectVideoCall_Should_ReturnForbiddenAndCallNoMethods_WhenNoAuthority()
      throws Exception {
    String content = new ObjectMapper().writeValueAsString(new RejectVideoCallDTO()
        .rcGroupId("rcGroupId")
        .initiatorUsername("username")
        .initiatorRcUserId("rcUserId"));

    mvc.perform(post(PATH_REJECT_VIDEO_CALL)
            .cookie(csrfCookie)
            .header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(content)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verifyNoMoreInteractions(rejectVideoCallService);
  }

  @Test
  @WithMockUser(authorities = {AUTHORITY_USER})
  public void rejectVideoCall_Should_ReturnOkAndCallService_WhenUserRole()
      throws Exception {
    String content = new ObjectMapper().writeValueAsString(new RejectVideoCallDTO()
        .rcGroupId("rcGroupId")
        .initiatorUsername("username")
        .initiatorRcUserId("rcUserId"));

    mvc.perform(post(PATH_REJECT_VIDEO_CALL)
            .cookie(csrfCookie)
            .header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(content)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(rejectVideoCallService, times(1)).rejectVideoCall(any());
  }

  @Test
  @WithAnonymousUser
  public void getWebToken_should_generate_token_for_anonymous_user() throws Exception {
    mvc.perform(get(PATH_GET_WEB_TOKEN)
            .cookie(csrfCookie)
            .header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(tokenGeneratorService).generateToken(RC_CHAT_ROOM_ID);
  }

  @Test
  @WithMockUser(authorities = {AUTHORITY_USER})
  public void getWebToken_should_generate_token_for_user() throws Exception {
    mvc.perform(get(PATH_GET_WEB_TOKEN)
            .cookie(csrfCookie)
            .header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(tokenGeneratorService).generateToken(RC_CHAT_ROOM_ID);
  }

  @Test
  @WithMockUser(authorities = {AUTHORITY_CONSULTANT})
  public void getWebToken_should_generate_token_for_consultant() throws Exception {
    mvc.perform(get(PATH_GET_WEB_TOKEN)
            .cookie(csrfCookie)
            .header(CSRF_HEADER, CSRF_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(tokenGeneratorService).generateToken(RC_CHAT_ROOM_ID);
  }
}
