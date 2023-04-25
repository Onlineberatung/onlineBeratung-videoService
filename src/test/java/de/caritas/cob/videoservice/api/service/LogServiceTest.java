package de.caritas.cob.videoservice.api.service;

import static de.caritas.cob.videoservice.api.service.LogService.STATISTICS_EVENT_PROCESSING_ERROR;
import static de.caritas.cob.videoservice.api.service.LogService.STATISTICS_EVENT_PROCESSING_WARNING;
import static javax.servlet.RequestDispatcher.ERROR_MESSAGE;
import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.reflect.Whitebox.setInternalState;

import java.io.PrintWriter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;

@RunWith(MockitoJUnitRunner.class)
public class LogServiceTest {

  @Mock Exception exception;

  @Mock private Logger logger;

  public static final Exception EXCEPTION = new Exception();

  @Before
  public void setup() {
    setInternalState(LogService.class, "LOGGER", logger);
  }

  @Test
  public void logInfo_Should_LogInfoMessage() {
    LogService.logInfo("info message");

    verify(logger, atLeastOnce()).info("info message");
  }

  @Test
  public void logWarning_Should_LogWarnMessage_When_onlyExceptionIsProvided() {
    LogService.logWarning(exception);

    verify(logger, atLeastOnce()).warn(eq("VideoService API: {}"), anyString());
    verify(exception, atLeastOnce()).printStackTrace(any(PrintWriter.class));
  }

  @Test
  public void logWarning_Should_LogWarnMessage_When_onlyExceptionAndStatusProvided() {
    LogService.logWarning(HttpStatus.MULTI_STATUS, exception);

    verify(logger, atLeastOnce())
        .warn(eq("VideoService API: {}: {}"), eq("Multi-Status"), anyString());
    verify(exception, atLeastOnce()).printStackTrace(any(PrintWriter.class));
  }

  @Test
  public void logInternalServerError_Should_LogError() {
    LogService.logInternalServerError(exception);

    verify(logger, atLeastOnce())
        .error(eq("VideoService API: 500 Internal Server Error: {}"), anyString());
    verify(exception, atLeastOnce()).printStackTrace(any(PrintWriter.class));
  }

  @Test
  public void logError_Should_LogError() {
    LogService.logError(exception);

    verify(logger, atLeastOnce()).error(eq("VideoService API: {}"), anyString());
    verify(exception, atLeastOnce()).printStackTrace(any(PrintWriter.class));
  }

  @Test
  public void logStatisticEventError_Should_LogExceptionStackTraceAndErrorMessage() {

    LogService.logStatisticsEventError(EXCEPTION);
    verify(logger, times(1))
        .error(anyString(), eq(STATISTICS_EVENT_PROCESSING_ERROR), eq(getStackTrace(EXCEPTION)));
  }

  @Test
  public void logStatisticEventWarning_Should_LogErrorMessageAsWarning() {

    LogService.logStatisticsEventWarning(ERROR_MESSAGE);
    verify(logger, times(1))
        .warn(anyString(), eq(STATISTICS_EVENT_PROCESSING_WARNING), eq(ERROR_MESSAGE));
  }
}
