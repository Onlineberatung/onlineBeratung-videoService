package de.caritas.cob.videoservice.api.service;

import static de.caritas.cob.videoservice.api.testhelper.FieldConstants.FIELD_NAME_CSRF_TOKEN_COOKIE_PROPERTY;
import static de.caritas.cob.videoservice.api.testhelper.FieldConstants.FIELD_NAME_CSRF_TOKEN_HEADER_PROPERTY;
import static de.caritas.cob.videoservice.api.testhelper.FieldConstants.FIELD_VALUE_CSRF_TOKEN_COOKIE_PROPERTY;
import static de.caritas.cob.videoservice.api.testhelper.FieldConstants.FIELD_VALUE_CSRF_TOKEN_HEADER_PROPERTY;
import static de.caritas.cob.videoservice.api.testhelper.TestConstants.ERROR_MSG;
import static de.caritas.cob.videoservice.api.testhelper.TestConstants.SESSION_ID;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.videoservice.api.authorization.AuthenticatedUser;
import de.caritas.cob.videoservice.api.exception.httpresponse.InternalServerErrorException;
import de.caritas.cob.videoservice.api.service.securityheader.SecurityHeaderSupplier;
import de.caritas.cob.videoservice.api.service.session.SessionService;
import de.caritas.cob.videoservice.userservice.generated.ApiClient;
import de.caritas.cob.videoservice.userservice.generated.web.UserControllerApi;
import de.caritas.cob.videoservice.userservice.generated.web.model.ConsultantSessionDTO;
import java.util.List;
import org.assertj.core.api.Assertions;
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
import org.springframework.web.server.ResponseStatusException;

@RunWith(MockitoJUnitRunner.class)
public class SessionServiceTest {

  @InjectMocks
  private SessionService sessionService;
  @Mock
  private UserControllerApi userControllerApi;
  @Mock
  private SecurityHeaderSupplier serviceHelper;
  @Mock
  private AuthenticatedUser authenticatedUser;
  @Mock
  private HttpHeaders httpHeaders;

  @Test
  public void findSessionOfCurrentConsultant_Should_ReturnConsultantSessionDto_When_GetSessionIsSuccessful() {
    ConsultantSessionDTO consultantSessionDto = mock(ConsultantSessionDTO.class);

    when(serviceHelper.getKeycloakAndCsrfHttpHeaders()).thenReturn(httpHeaders);
    when(userControllerApi.fetchSessionForConsultant(SESSION_ID)).thenReturn(consultantSessionDto);

    ConsultantSessionDTO result = sessionService.findSessionOfCurrentConsultant(SESSION_ID);

    assertThat(result, instanceOf(ConsultantSessionDTO.class));
  }

  @Test(expected = InternalServerErrorException.class)
  public void findSessionOfCurrentConsultant_Should_ThrowInternalServerErrorException_When_GetSessionFails() {
    when(serviceHelper.getKeycloakAndCsrfHttpHeaders()).thenReturn(httpHeaders);
    when(userControllerApi.fetchSessionForConsultant(SESSION_ID))
        .thenThrow(new RestClientException(ERROR_MSG));

    sessionService.findSessionOfCurrentConsultant(SESSION_ID);
  }

  @Test
  public void findSessionOfCurrentConsultant_Should_ThrowResponseStatusExceptionWithSameForwardedStatusCode_When_GetSessionClientRequestFails() {
    HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.FORBIDDEN);

    when(serviceHelper.getKeycloakAndCsrfHttpHeaders()).thenReturn(httpHeaders);
    when(userControllerApi.fetchSessionForConsultant(SESSION_ID))
        .thenThrow(exception);

    final Throwable ex = catchThrowable(
        () -> sessionService.findSessionOfCurrentConsultant(SESSION_ID));

    Assertions.assertThat(ex).isInstanceOf(ResponseStatusException.class);
    assertEquals(exception.getStatusCode(), ((ResponseStatusException) ex).getStatus());
  }

  @Test
  public void findSessionOfCurrentConsultant_Should_ThrowResponseStatusExceptionWithStatusNotFound_When_GetSessionFailsBecauseOfClientNotFoundRequest() {
    HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.NOT_FOUND);

    when(serviceHelper.getKeycloakAndCsrfHttpHeaders()).thenReturn(httpHeaders);
    when(userControllerApi.fetchSessionForConsultant(SESSION_ID))
        .thenThrow(exception);

    final Throwable ex = catchThrowable(
        () -> sessionService.findSessionOfCurrentConsultant(SESSION_ID));

    Assertions.assertThat(ex).isInstanceOf(ResponseStatusException.class);
    assertEquals(HttpStatus.NOT_FOUND, ((ResponseStatusException) ex).getStatus());
  }

  @Test
  public void findSessionOfCurrentConsultant_Should_ThrowResponseStatusExceptionWithStatusForbidden_When_GetSessionFailsBecauseOfClientForbiddenRequest() {
    HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.FORBIDDEN);

    when(serviceHelper.getKeycloakAndCsrfHttpHeaders()).thenReturn(httpHeaders);
    when(userControllerApi.fetchSessionForConsultant(SESSION_ID))
        .thenThrow(exception);

    final Throwable ex = catchThrowable(
        () -> sessionService.findSessionOfCurrentConsultant(SESSION_ID));

    Assertions.assertThat(ex).isInstanceOf(ResponseStatusException.class);
    assertEquals(HttpStatus.FORBIDDEN, ((ResponseStatusException) ex).getStatus());
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
