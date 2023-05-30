package de.caritas.cob.videoservice;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.reflect.Whitebox.setInternalState;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import de.caritas.cob.videoservice.api.exception.httpresponse.BadRequestException;
import de.caritas.cob.videoservice.api.exception.httpresponse.InternalServerErrorException;
import de.caritas.cob.videoservice.api.service.LogService;
import java.util.NoSuchElementException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

@RunWith(MockitoJUnitRunner.class)
public class ApiResponseEntityExceptionHandlerTest {

  @InjectMocks private ApiResponseEntityExceptionHandler exceptionHandler;

  @Mock private Logger logger;

  @Before
  public void setup() {
    setInternalState(LogService.class, "LOGGER", logger);
  }

  @Test
  public void handleCustomBadRequest_Should_executeLogging_When_badRequestIsGiven() {
    BadRequestException badRequestException =
        new BadRequestException("test", LogService::logWarning);

    this.exceptionHandler.handleCustomBadRequest(badRequestException, mock(WebRequest.class));

    verify(logger, times(1)).warn(eq("VideoService API: {}"), anyString());
  }

  @Test
  public void
      handleNoSuchElementException_Should_executeLogging_When_noSuchElementExceptionRequestIsGiven() {
    NoSuchElementException noSuchElementException = new NoSuchElementException("test");

    this.exceptionHandler.handleNoSuchElementException(
        noSuchElementException, mock(WebRequest.class));

    verify(logger, times(1)).warn(eq("VideoService API: {}: {}"), eq("Not Found"), anyString());
  }

  @Test
  public void handleMethodArgumentNotValid_Should_executeLogging() {
    this.exceptionHandler.handleMethodArgumentNotValid(
        mock(MethodArgumentNotValidException.class),
        new HttpHeaders(),
        NOT_FOUND,
        mock(WebRequest.class));

    verify(logger, times(1)).warn(eq("VideoService API: {}: {}"), eq("Not Found"), anyString());
  }

  @Test
  public void handleInternal_Should_executeLogging_When_RuntimeExceptionIsGiven() {
    RuntimeException runtimeException = new RuntimeException("test");

    this.exceptionHandler.handleInternal(runtimeException, mock(WebRequest.class));

    verify(logger, times(1))
        .error(eq("VideoService API: 500 Internal Server Error: {}"), anyString());
  }

  @Test
  public void handleInternal_Should_executeLogging_When_InternalServerErrorIsGiven() {
    InternalServerErrorException exception = new InternalServerErrorException("error");

    this.exceptionHandler.handleInternal(exception, mock(WebRequest.class));

    verify(logger, times(1))
        .error(eq("VideoService API: 500 Internal Server Error: {}"), anyString());
  }

  @Test
  public void handleInternal_Should_executeLogging_When_ResponseStatusExceptionIsGiven() {
    ResponseStatusException exception = new ResponseStatusException(NOT_FOUND);

    this.exceptionHandler.handleInternal(exception, mock(WebRequest.class));

    verify(logger, times(1)).warn(eq("VideoService API: {}"), anyString());
  }

  @Test
  public void handleInternal_Should_executeLogging_When_HttpClientErrorExceptionIsGiven() {
    HttpClientErrorException exception = new HttpClientErrorException(NOT_FOUND);

    this.exceptionHandler.handleInternal(exception, mock(WebRequest.class));

    verify(logger, times(1)).error(eq("VideoService API: {}"), anyString());
  }
}
