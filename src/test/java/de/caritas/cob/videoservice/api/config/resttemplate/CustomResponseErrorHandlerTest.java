package de.caritas.cob.videoservice.api.config.resttemplate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import de.caritas.cob.videoservice.api.exception.httpresponse.InternalServerErrorException;
import de.caritas.cob.videoservice.config.resttemplate.CustomResponseErrorHandler;
import java.net.URL;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.server.ResponseStatusException;

@RunWith(MockitoJUnitRunner.class)
public class CustomResponseErrorHandlerTest {

  @Mock
  private ClientHttpResponse httpResponse;

  private final CustomResponseErrorHandler errorHandler = new CustomResponseErrorHandler();

  @Test(expected = InternalServerErrorException.class)
  public void handleError_Should_throwInternalServerErrorException_When_responseStatusIsNotLoopThrough()
      throws Exception {
    URL url = new URL("http://test.de");

    errorHandler.handleError(url.toURI(), HttpMethod.GET, httpResponse);
  }

  @Test
  public void handleError_Should_throwExpectedResponseStatusException_When_responseStatusIsForbidden()
      throws Exception {
    URL url = new URL("http://test.de");
    when(httpResponse.getStatusCode()).thenReturn(HttpStatus.FORBIDDEN);

    try {
      errorHandler.handleError(url.toURI(), HttpMethod.GET, httpResponse);
      fail("Exception was not thrown");
    } catch (ResponseStatusException e) {
      assertThat(e.getStatus(), is(HttpStatus.FORBIDDEN));
      assertThat(e.getReason(), is("GET http://test.de"));
    }
  }

  @Test
  public void handleError_Should_throwExpectedResponseStatusException_When_responseStatusIsNotFound()
      throws Exception {
    URL url = new URL("http://test.de");
    when(httpResponse.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);

    try {
      errorHandler.handleError(url.toURI(), HttpMethod.GET, httpResponse);
      fail("Exception was not thrown");
    } catch (ResponseStatusException e) {
      assertThat(e.getStatus(), is(HttpStatus.NOT_FOUND));
      assertThat(e.getReason(), is("GET http://test.de"));
    }
  }

  @Test
  public void handleError_Should_throwExpectedResponseStatusException_When_responseStatusIsBadRequest()
      throws Exception {
    URL url = new URL("http://test.de");
    when(httpResponse.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);

    try {
      errorHandler.handleError(url.toURI(), HttpMethod.POST, httpResponse);
      fail("Exception was not thrown");
    } catch (ResponseStatusException e) {
      assertThat(e.getStatus(), is(HttpStatus.BAD_REQUEST));
      assertThat(e.getReason(), is("POST http://test.de"));
    }
  }

}
