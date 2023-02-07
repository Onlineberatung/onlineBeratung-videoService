package de.caritas.cob.videoservice.api.controller;

import static de.caritas.cob.videoservice.api.testhelper.TestConstants.AUTHORITY_CONSULTANT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.caritas.cob.videoservice.api.authorization.VideoUser;
import de.caritas.cob.videoservice.api.service.session.SessionStatus;
import de.caritas.cob.videoservice.userservice.generated.web.model.ConsultantSessionDTO;
import java.net.URI;
import java.util.UUID;
import javax.servlet.http.Cookie;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplateHandler;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("testing")
public class VideoControllerE2eIT {

  private static final EasyRandom easyRandom = new EasyRandom();
  private static final String CSRF_HEADER = "csrfHeader";
  private static final String CSRF_VALUE = "test";
  private static final Cookie CSRF_COOKIE = new Cookie("csrfCookie", CSRF_VALUE);

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  @SuppressWarnings("unused")
  private VideoUser videoUser;

  @MockBean
  @Qualifier("restTemplate")
  private RestTemplate restTemplate;

  private Long sessionId;

  private ConsultantSessionDTO consultantSessionDto;

  @AfterEach
  void reset() {
    sessionId = null;
    consultantSessionDto = null;
  }

  @Test
  @WithMockUser(authorities = AUTHORITY_CONSULTANT)
  public void stopVideoCallShouldReturnNoContent() throws Exception {
    givenASessionId();
    givenAValidSessionResponse();

    mockMvc.perform(
        post("/videocalls/stop/" + sessionId)
            .cookie(CSRF_COOKIE)
            .header(CSRF_HEADER, CSRF_VALUE)
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(authorities = AUTHORITY_CONSULTANT)
  public void stopVideoCallShouldReturnNotFoundIfSessionIdUnknown() throws Exception {
    givenASessionId();
    givenAnUnknownSessionResponse();

    mockMvc.perform(
            post("/videocalls/stop/" + sessionId)
                .cookie(CSRF_COOKIE)
                .header(CSRF_HEADER, CSRF_VALUE)
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNotFound());
  }

  @SuppressWarnings("unchecked")
  private void givenAValidSessionResponse() {
    consultantSessionDto = new ConsultantSessionDTO();
    consultantSessionDto.setStatus(SessionStatus.IN_PROGRESS.getValue());
    consultantSessionDto.setId(sessionId);
    consultantSessionDto.setAskerId(UUID.randomUUID().toString());

    var uriTemplateHandler = mock(UriTemplateHandler.class);
    when(uriTemplateHandler.expand(anyString(), anyMap())).thenReturn(easyRandom.nextObject(URI.class));
    when(restTemplate.getUriTemplateHandler()).thenReturn(uriTemplateHandler);
    when(restTemplate.exchange(any(RequestEntity.class), any(ParameterizedTypeReference.class)))
        .thenReturn(ResponseEntity.ok(consultantSessionDto));
  }

  @SuppressWarnings("unchecked")
  private void givenAnUnknownSessionResponse() {
    var uriTemplateHandler = mock(UriTemplateHandler.class);
    when(uriTemplateHandler.expand(anyString(), anyMap())).thenReturn(easyRandom.nextObject(URI.class));
    when(restTemplate.getUriTemplateHandler()).thenReturn(uriTemplateHandler);
    when(restTemplate.exchange(any(RequestEntity.class), any(ParameterizedTypeReference.class)))
        .thenReturn(ResponseEntity.notFound().build());
  }

  private void givenASessionId() {
    sessionId = Math.abs(easyRandom.nextLong());
  }
}
