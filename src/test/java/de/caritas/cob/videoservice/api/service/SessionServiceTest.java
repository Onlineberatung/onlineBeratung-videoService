package de.caritas.cob.videoservice.api.service;

import static de.caritas.cob.videoservice.api.testhelper.FieldConstants.FIELD_NAME_CSRF_TOKEN_COOKIE_PROPERTY;
import static de.caritas.cob.videoservice.api.testhelper.FieldConstants.FIELD_NAME_CSRF_TOKEN_HEADER_PROPERTY;
import static de.caritas.cob.videoservice.api.testhelper.FieldConstants.FIELD_VALUE_CSRF_TOKEN_COOKIE_PROPERTY;
import static de.caritas.cob.videoservice.api.testhelper.FieldConstants.FIELD_VALUE_CSRF_TOKEN_HEADER_PROPERTY;
import static de.caritas.cob.videoservice.api.testhelper.TestConstants.SESSION_ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.videoservice.api.service.httpheader.SecurityHeaderSupplier;
import de.caritas.cob.videoservice.api.service.httpheader.TenantHeaderSupplier;
import de.caritas.cob.videoservice.api.service.session.SessionService;
import de.caritas.cob.videoservice.api.service.session.UserServiceApiControllerFactory;
import de.caritas.cob.videoservice.userservice.generated.ApiClient;
import de.caritas.cob.videoservice.userservice.generated.web.UserControllerApi;
import de.caritas.cob.videoservice.userservice.generated.web.model.ConsultantSessionDTO;
import java.util.Enumeration;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RunWith(MockitoJUnitRunner.class)
public class SessionServiceTest {

  @InjectMocks private SessionService sessionService;
  @Mock private UserControllerApi userControllerApi;
  @Mock private SecurityHeaderSupplier serviceHelper;

  @Mock private HttpHeaders httpHeaders;

  @Mock private ServletRequestAttributes requestAttributes;

  @Mock private HttpServletRequest httpServletRequest;

  @Mock private TenantHeaderSupplier tenantHeaderSupplier;

  @Mock private Enumeration<String> headers;

  @Mock private UserServiceApiControllerFactory userControllerApiControllerFactory;

  @Test
  public void
      findSessionOfCurrentConsultant_Should_ReturnConsultantSessionDto_When_GetSessionIsSuccessful() {
    ConsultantSessionDTO consultantSessionDto = mock(ConsultantSessionDTO.class);
    when(userControllerApiControllerFactory.createControllerApi()).thenReturn(userControllerApi);

    when(serviceHelper.getKeycloakAndCsrfHttpHeaders()).thenReturn(httpHeaders);
    when(userControllerApi.fetchSessionForConsultant(SESSION_ID)).thenReturn(consultantSessionDto);

    ConsultantSessionDTO result = sessionService.findSessionOfCurrentConsultant(SESSION_ID);

    assertThat(result, instanceOf(ConsultantSessionDTO.class));
  }

  @Test
  public void findSessionOfCurrentConsultant_Should_AddKeycloakAndCsrfHttpHeaders() {
    HttpHeaders headers = new HttpHeaders();
    when(userControllerApiControllerFactory.createControllerApi()).thenReturn(userControllerApi);
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
    resetRequestAttributes();
  }

  private void resetRequestAttributes() {
    RequestContextHolder.setRequestAttributes(null);
  }
}
