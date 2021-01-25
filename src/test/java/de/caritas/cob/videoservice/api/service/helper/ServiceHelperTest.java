package de.caritas.cob.videoservice.api.service.helper;


import static de.caritas.cob.videoservice.api.testhelper.FieldConstants.FIELD_NAME_CSRF_TOKEN_COOKIE_PROPERTY;
import static de.caritas.cob.videoservice.api.testhelper.FieldConstants.FIELD_NAME_CSRF_TOKEN_HEADER_PROPERTY;
import static de.caritas.cob.videoservice.api.testhelper.FieldConstants.FIELD_VALUE_CSRF_TOKEN_COOKIE_PROPERTY;
import static de.caritas.cob.videoservice.api.testhelper.FieldConstants.FIELD_VALUE_CSRF_TOKEN_HEADER_PROPERTY;
import static de.caritas.cob.videoservice.api.testhelper.TestConstants.BEARER_TOKEN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import de.caritas.cob.videoservice.api.authorization.AuthenticatedUser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@RunWith(MockitoJUnitRunner.class)
public class ServiceHelperTest {

  @InjectMocks
  private ServiceHelper serviceHelper;
  @Mock
  private AuthenticatedUser authenticatedUser;

  /**
   * Set up private fields.
   *
   * @throws NoSuchFieldException {@link NoSuchFieldException}
   * @throws SecurityException    {@link SecurityException}
   */
  @Before
  public void setup() throws NoSuchFieldException, SecurityException {
    FieldSetter.setField(serviceHelper,
        serviceHelper.getClass().getDeclaredField(FIELD_NAME_CSRF_TOKEN_HEADER_PROPERTY),
        FIELD_VALUE_CSRF_TOKEN_HEADER_PROPERTY);
    FieldSetter.setField(serviceHelper,
        serviceHelper.getClass().getDeclaredField(FIELD_NAME_CSRF_TOKEN_COOKIE_PROPERTY),
        FIELD_VALUE_CSRF_TOKEN_COOKIE_PROPERTY);
  }

  @Test
  public void getKeycloakAndCsrfHttpHeaders_Should_Return_HeaderWithCorrectContentType() {
    HttpHeaders result = serviceHelper.getKeycloakAndCsrfHttpHeaders();

    assertEquals(MediaType.APPLICATION_JSON, result.getContentType());
  }

  @Test
  public void getKeycloakAndCsrfHttpHeaders_Should_Return_CorrectCookiePropertyName() {
    HttpHeaders result = serviceHelper.getKeycloakAndCsrfHttpHeaders();

    assertTrue(
        result.get("Cookie").toString().startsWith("[" + FIELD_VALUE_CSRF_TOKEN_COOKIE_PROPERTY
            + "="));
  }

  @Test
  public void getKeycloakAndCsrfHttpHeaders_Should_Return_CorrectHeaderAndCookieValues() {
    HttpHeaders result = serviceHelper.getKeycloakAndCsrfHttpHeaders();
    String cookieValue = "[" + result.get("Cookie").toString()
        .substring(result.get("Cookie").toString().lastIndexOf("=") + 1);

    assertEquals(cookieValue,
        result.get(FIELD_VALUE_CSRF_TOKEN_HEADER_PROPERTY).toString());
  }

  @Test
  public void getRocketChatAndCsrfHttpHeaders_Should_ReturnHeaderWithKeycloakAuthToken() {
    when(authenticatedUser.getAccessToken()).thenReturn(BEARER_TOKEN);

    HttpHeaders result = serviceHelper.getKeycloakAndCsrfHttpHeaders();

    assertEquals("[Bearer " + BEARER_TOKEN + "]", result.get("Authorization").toString());
  }
}
