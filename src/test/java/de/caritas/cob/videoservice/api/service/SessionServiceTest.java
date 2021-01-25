package de.caritas.cob.videoservice.api.service;

import static de.caritas.cob.videoservice.api.testhelper.FieldConstants.FIELD_NAME_CSRF_TOKEN_COOKIE_PROPERTY;
import static de.caritas.cob.videoservice.api.testhelper.FieldConstants.FIELD_NAME_CSRF_TOKEN_HEADER_PROPERTY;
import static de.caritas.cob.videoservice.api.testhelper.FieldConstants.FIELD_VALUE_CSRF_TOKEN_COOKIE_PROPERTY;
import static de.caritas.cob.videoservice.api.testhelper.FieldConstants.FIELD_VALUE_CSRF_TOKEN_HEADER_PROPERTY;
import static de.caritas.cob.videoservice.api.testhelper.TestConstants.ERROR_MSG;
import static de.caritas.cob.videoservice.api.testhelper.TestConstants.SESSION_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.videoservice.api.authorization.AuthenticatedUser;
import de.caritas.cob.videoservice.api.exception.httpresponse.BadRequestException;
import de.caritas.cob.videoservice.api.exception.httpresponse.InternalServerErrorException;
import de.caritas.cob.videoservice.api.service.helper.ServiceHelper;
import de.caritas.cob.videoservice.userservice.generated.ApiClient;
import de.caritas.cob.videoservice.userservice.generated.web.UserControllerApi;
import de.caritas.cob.videoservice.userservice.generated.web.model.ConsultantSessionDTO;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

@RunWith(MockitoJUnitRunner.class)
public class SessionServiceTest {

  @InjectMocks
  private SessionService sessionService;
  @Mock
  private UserControllerApi userControllerApi;
  @Mock
  private ServiceHelper serviceHelper;
  @Mock
  private AuthenticatedUser authenticatedUser;

  private HttpHeaders httpHeaders;

  @Before
  public void setUp() {
    httpHeaders = mock(HttpHeaders.class);
  }

  @Test
  public void findSessionOfCurrentConsultant_Should_ReturnConsultantSessionDto_When_GetSessionIsSuccessful() {
    ConsultantSessionDTO consultantSessionDto = mock(ConsultantSessionDTO.class);

    when(serviceHelper.getKeycloakAndCsrfHttpHeaders()).thenReturn(httpHeaders);
    when(userControllerApi.fetchSessionForConsultant(SESSION_ID)).thenReturn(consultantSessionDto);

    ConsultantSessionDTO result = sessionService.findSessionOfCurrentConsultant(SESSION_ID);

    assertTrue(result instanceof ConsultantSessionDTO);
  }

  @Test(expected = InternalServerErrorException.class)
  public void findSessionOfCurrentConsultant_Should_ThrowInternalServerErrorException_When_GetSessionFails() {
    when(serviceHelper.getKeycloakAndCsrfHttpHeaders()).thenReturn(httpHeaders);
    when(userControllerApi.fetchSessionForConsultant(SESSION_ID))
        .thenThrow(new RestClientException(ERROR_MSG));

    sessionService.findSessionOfCurrentConsultant(SESSION_ID);
  }

  @Test(expected = BadRequestException.class)
  public void findSessionOfCurrentConsultant_Should_ThrowBadRequestException_When_GetSessionFailsBecauseOfClientForbiddenRequest() {
    HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.FORBIDDEN);

    when(serviceHelper.getKeycloakAndCsrfHttpHeaders()).thenReturn(httpHeaders);
    when(userControllerApi.fetchSessionForConsultant(SESSION_ID))
        .thenThrow(exception);

    sessionService.findSessionOfCurrentConsultant(SESSION_ID);
  }

  @Test(expected = BadRequestException.class)
  public void findSessionOfCurrentConsultant_Should_ThrowBadRequestException_When_GetSessionFailsBecauseOfClientNotFoundRequest() {
    HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.NOT_FOUND);

    when(serviceHelper.getKeycloakAndCsrfHttpHeaders()).thenReturn(httpHeaders);
    when(userControllerApi.fetchSessionForConsultant(SESSION_ID))
        .thenThrow(exception);

    sessionService.findSessionOfCurrentConsultant(SESSION_ID);
  }

  @Test(expected = BadRequestException.class)
  public void findSessionOfCurrentConsultant_Should_ThrowBadRequestException_When_GetSessionFailsBecauseOfClientUnauthorizedRequest() {
    HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.UNAUTHORIZED);

    when(serviceHelper.getKeycloakAndCsrfHttpHeaders()).thenReturn(httpHeaders);
    when(userControllerApi.fetchSessionForConsultant(SESSION_ID))
        .thenThrow(exception);

    sessionService.findSessionOfCurrentConsultant(SESSION_ID);
  }

  @Test
  public void findSessionOfCurrentConsultant_Should_AddKeycloakAndCsrfHttpHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.add(FIELD_NAME_CSRF_TOKEN_HEADER_PROPERTY, FIELD_VALUE_CSRF_TOKEN_HEADER_PROPERTY);
    headers.add(FIELD_NAME_CSRF_TOKEN_COOKIE_PROPERTY, FIELD_VALUE_CSRF_TOKEN_COOKIE_PROPERTY);
    ConsultantSessionDTO consultantSessionDto = mock(ConsultantSessionDTO.class);
    ApiClient apiClient = mock(ApiClient.class);

    when(serviceHelper.getKeycloakAndCsrfHttpHeaders()).thenReturn(httpHeaders);
    when(userControllerApi.fetchSessionForConsultant(SESSION_ID)).thenReturn(consultantSessionDto);
    when(serviceHelper.getKeycloakAndCsrfHttpHeaders()).thenReturn(headers);
    when(userControllerApi.getApiClient()).thenReturn(apiClient).thenReturn(apiClient);

    sessionService.findSessionOfCurrentConsultant(SESSION_ID);

    ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
    verify(apiClient, times(2)).addDefaultHeader(anyString(), argument.capture());
    List<String> headerValues = argument.getAllValues();
    assertEquals(FIELD_VALUE_CSRF_TOKEN_HEADER_PROPERTY, headerValues.get(0));
    assertEquals(FIELD_VALUE_CSRF_TOKEN_COOKIE_PROPERTY, headerValues.get(1));
  }
}
