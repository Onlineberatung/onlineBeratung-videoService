package de.caritas.cob.videoservice.api.service.securityheader;


import static de.caritas.cob.videoservice.api.testhelper.FieldConstants.FIELD_NAME_CSRF_TOKEN_COOKIE_PROPERTY;
import static de.caritas.cob.videoservice.api.testhelper.FieldConstants.FIELD_NAME_CSRF_TOKEN_HEADER_PROPERTY;
import static de.caritas.cob.videoservice.api.testhelper.FieldConstants.FIELD_VALUE_CSRF_TOKEN_COOKIE_PROPERTY;
import static de.caritas.cob.videoservice.api.testhelper.FieldConstants.FIELD_VALUE_CSRF_TOKEN_HEADER_PROPERTY;
import static de.caritas.cob.videoservice.api.testhelper.TestConstants.BEARER_TOKEN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import de.caritas.cob.videoservice.api.authorization.AuthenticatedUser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@RunWith(MockitoJUnitRunner.class)
public class SecurityHeaderSupplierTest {

  @InjectMocks
  private SecurityHeaderSupplier securityHeaderSupplier;
  @Mock
  private AuthenticatedUser authenticatedUser;

  @Before
  public void setup() {
    setField(securityHeaderSupplier, FIELD_NAME_CSRF_TOKEN_HEADER_PROPERTY,
        FIELD_VALUE_CSRF_TOKEN_HEADER_PROPERTY);
    setField(securityHeaderSupplier, FIELD_NAME_CSRF_TOKEN_COOKIE_PROPERTY,
        FIELD_VALUE_CSRF_TOKEN_COOKIE_PROPERTY);
  }

  @Test
  public void getKeycloakAndCsrfHttpHeaders_Should_Return_HeaderWithCorrectContentType() {
    HttpHeaders result = securityHeaderSupplier.getKeycloakAndCsrfHttpHeaders();

    assertEquals(MediaType.APPLICATION_JSON, result.getContentType());
  }

  @Test
  public void getKeycloakAndCsrfHttpHeaders_Should_Return_CorrectCookiePropertyName() {
    HttpHeaders result = securityHeaderSupplier.getKeycloakAndCsrfHttpHeaders();

    assertTrue(
        result.get("Cookie").toString().startsWith("[" + FIELD_VALUE_CSRF_TOKEN_COOKIE_PROPERTY
            + "="));
  }

  @Test
  public void getKeycloakAndCsrfHttpHeaders_Should_Return_CorrectHeaderAndCookieValues() {
    HttpHeaders result = securityHeaderSupplier.getKeycloakAndCsrfHttpHeaders();
    String cookieValue = "[" + result.get("Cookie").toString()
        .substring(result.get("Cookie").toString().lastIndexOf("=") + 1);

    assertEquals(cookieValue,
        result.get(FIELD_VALUE_CSRF_TOKEN_HEADER_PROPERTY).toString());
  }

  @Test
  public void getRocketChatAndCsrfHttpHeaders_Should_ReturnHeaderWithKeycloakAuthToken() {
    when(authenticatedUser.getAccessToken()).thenReturn(BEARER_TOKEN);

    HttpHeaders result = securityHeaderSupplier.getKeycloakAndCsrfHttpHeaders();

    assertEquals("[Bearer " + BEARER_TOKEN + "]", result.get("Authorization").toString());
  }
}
