package de.caritas.cob.videoservice;

import de.caritas.cob.videoservice.api.exception.KeycloakException;
import de.caritas.cob.videoservice.api.exception.httpresponse.BadRequestException;
import de.caritas.cob.videoservice.api.exception.httpresponse.InternalServerErrorException;
import de.caritas.cob.videoservice.api.service.LogService;
import java.net.UnknownHostException;
import java.util.NoSuchElementException;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Customizes API error/exception handling to hide information and/or possible security
 * vulnerabilities.
 */
@NoArgsConstructor
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

  private static final Exception EMPTY_EXCEPTION = new Exception();

  /**
   * Custom BadRequest exception.
   *
   * @param request the invoking request
   * @param ex the thrown exception
   */
  @ExceptionHandler({BadRequestException.class})
  public ResponseEntity<Object> handleCustomBadRequest(
      final BadRequestException ex, final WebRequest request) {
    ex.executeLogging();

    return handleExceptionInternal(ex, null, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
  }

  @ExceptionHandler({NoSuchElementException.class})
  public ResponseEntity<Object> handleNoSuchElementException(
      final NoSuchElementException ex, final WebRequest request) {
    LogService.logWarning(HttpStatus.NOT_FOUND, ex);

    return handleExceptionInternal(ex, null, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
  }

  /**
   * Incoming request body could not be deserialized.
   *
   * @param ex the thrown exception
   * @param headers HTTP headers
   * @param status HTTP status
   * @param request web request
   * @return response entity
   */
  @Override
  @NonNull
  protected ResponseEntity<Object> handleHttpMessageNotReadable(
      final @NonNull HttpMessageNotReadableException ex,
      final @NonNull HttpHeaders headers,
      final @NonNull HttpStatus status,
      final @NonNull WebRequest request) {
    LogService.logWarning(status, ex);

    return handleExceptionInternal(ex, null, headers, status, request);
  }

  /**
   * ´@Valid´ on object fails validation.
   *
   * @param ex the thrown exception
   * @param headers http headers
   * @param status http status
   * @param request web request
   * @return response entity
   */
  @Override
  @NonNull
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      final @NonNull MethodArgumentNotValidException ex,
      final @NonNull HttpHeaders headers,
      final @NonNull HttpStatus status,
      final @NonNull WebRequest request) {
    LogService.logWarning(status, ex);

    return handleExceptionInternal(ex, null, headers, status, request);
  }

  /**
   * 500 - Internal Server Error.
   *
   * @param ex the thrown exception
   * @param request web request
   * @return response entity
   */
  @ExceptionHandler({
    NullPointerException.class,
    IllegalArgumentException.class,
    IllegalStateException.class,
    KeycloakException.class,
    UnknownHostException.class
  })
  public ResponseEntity<Object> handleInternal(
      final RuntimeException ex, final WebRequest request) {
    LogService.logInternalServerError(ex);

    return handleExceptionInternal(
        EMPTY_EXCEPTION, null, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
  }

  /**
   * 500 - Custom Internal Server Error.
   *
   * @param request the invoking request
   * @param ex the thrown exception
   * @return response entity
   */
  @ExceptionHandler({InternalServerErrorException.class})
  public ResponseEntity<Object> handleInternal(
      final InternalServerErrorException ex, final WebRequest request) {
    ex.executeLogging();

    return handleExceptionInternal(
        EMPTY_EXCEPTION, null, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
  }

  /**
   * Handles generic HTTP status.
   *
   * @param ex {@link ResponseStatusException}
   * @param request {@link WebRequest}
   * @return response entity
   */
  @ExceptionHandler({ResponseStatusException.class})
  public ResponseEntity<Object> handleInternal(
      final ResponseStatusException ex, final WebRequest request) {
    LogService.logWarning(ex);

    return handleExceptionInternal(
        EMPTY_EXCEPTION, null, new HttpHeaders(), ex.getStatus(), request);
  }

  /**
   * Handles generic HTTP client error status for generated apis.
   *
   * @param ex {@link HttpClientErrorException}
   * @param request {@link WebRequest}
   * @return response entity
   */
  @ExceptionHandler({HttpClientErrorException.class})
  public ResponseEntity<Object> handleInternal(
      final HttpClientErrorException ex, final WebRequest request) {
    LogService.logError(ex);

    return handleExceptionInternal(
        EMPTY_EXCEPTION, null, new HttpHeaders(), ex.getStatusCode(), request);
  }
}
