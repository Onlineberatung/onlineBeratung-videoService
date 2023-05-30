package de.caritas.cob.videoservice.api.service;

import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

/** Service for logging. */
public class LogService {

  private static final Logger LOGGER = LoggerFactory.getLogger(LogService.class);
  public static final String STATISTICS_EVENT_PROCESSING_ERROR =
      "StatisticsEventProcessing error: ";
  public static final String STATISTICS_EVENT_PROCESSING_WARNING =
      "StatisticsEventProcessing warning: ";

  private LogService() {}

  /**
   * Logs an info message.
   *
   * @param msg The message
   */
  public static void logInfo(String msg) {
    LOGGER.info(msg);
  }

  /**
   * Logs a warning message.
   *
   * @param exception The exception
   */
  public static void logWarning(Exception exception) {
    LOGGER.warn("VideoService API: {}", getStackTrace(exception));
  }

  /**
   * Logs a warning message.
   *
   * @param httpStatus http status
   * @param exception The exception
   */
  public static void logWarning(HttpStatus httpStatus, Exception exception) {
    LOGGER.warn("VideoService API: {}: {}", httpStatus.getReasonPhrase(), getStackTrace(exception));
  }

  /**
   * Logs a error message.
   *
   * @param exception The exception
   */
  public static void logError(Exception exception) {
    LOGGER.error("VideoService API: {}", getStackTrace(exception));
  }

  /**
   * Logs an internal server error.
   *
   * @param exception the exception to be logged
   */
  public static void logInternalServerError(Exception exception) {
    LOGGER.error("VideoService API: 500 Internal Server Error: {}", getStackTrace(exception));
  }

  /**
   * Error while processing statistics event.
   *
   * @param exception Exception
   */
  public static void logStatisticsEventError(Exception exception) {
    LOGGER.error("{}{}", STATISTICS_EVENT_PROCESSING_ERROR, getStackTrace(exception));
  }

  /**
   * Warning while processing statistics event.
   *
   * @param message error message
   */
  public static void logStatisticsEventWarning(String message) {
    LOGGER.warn("{}{}", STATISTICS_EVENT_PROCESSING_WARNING, message);
  }
}
