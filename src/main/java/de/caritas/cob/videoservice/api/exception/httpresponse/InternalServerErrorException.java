package de.caritas.cob.videoservice.api.exception.httpresponse;

import de.caritas.cob.videoservice.api.service.LogService;
import java.util.function.Consumer;

public class InternalServerErrorException extends CustomHttpStatusException {

  /**
   * InternalServerError exception.
   *
   * @param message       the exception message
   * @param ex            the exception
   * @param loggingMethod the logging method
   */
  public InternalServerErrorException(String message, Exception ex,
      Consumer<Exception> loggingMethod) {
    super(message, ex, loggingMethod);
  }

  /**
   * InternalServerError exception.
   *
   * @param message an additional message
   */
  public InternalServerErrorException(String message) {
    super(message, LogService::logInternalServerError);
  }

}
