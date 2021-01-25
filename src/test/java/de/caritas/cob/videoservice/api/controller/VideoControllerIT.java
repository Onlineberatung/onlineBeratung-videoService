package de.caritas.cob.videoservice.api.controller;

import static de.caritas.cob.videoservice.api.testhelper.PathConstants.PATH_START_VIDEO_CALL;
import static de.caritas.cob.videoservice.api.testhelper.RequestBodyConstants.VALID_START_VIDEO_CALL_BODY;
import static de.caritas.cob.videoservice.api.testhelper.TestConstants.SESSION_ID;
import static de.caritas.cob.videoservice.api.testhelper.TestConstants.VIDEO_CALL_URL;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.caritas.cob.videoservice.api.authorization.RoleAuthorizationAuthorityMapper;
import de.caritas.cob.videoservice.api.facade.StartVideoCallFacade;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@WebMvcTest(VideoController.class)
@AutoConfigureMockMvc(addFilters = false)
public class VideoControllerIT {

  @Autowired
  private MockMvc mvc;

  @MockBean
  private StartVideoCallFacade startVideoCallFacade;

  @MockBean
  private RoleAuthorizationAuthorityMapper roleAuthorizationAuthorityMapper;

  @Test
  public void getAgencies_Should_ReturnCreated_When_EverythingSucceeded() throws Exception {

    when(startVideoCallFacade.startVideoCall(SESSION_ID)).thenReturn(VIDEO_CALL_URL);

    mvc.perform(post(PATH_START_VIDEO_CALL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(VALID_START_VIDEO_CALL_BODY)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated());
  }

  @Test
  public void getAgencies_Should_ReturnBadRequest_When_SessionIdIsMissing() throws Exception {

    when(startVideoCallFacade.startVideoCall(SESSION_ID)).thenReturn(VIDEO_CALL_URL);

    mvc.perform(post(PATH_START_VIDEO_CALL)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }
}