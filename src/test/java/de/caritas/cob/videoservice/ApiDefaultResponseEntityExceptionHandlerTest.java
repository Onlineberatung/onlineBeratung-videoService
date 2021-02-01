package de.caritas.cob.videoservice;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.reflect.Whitebox.setInternalState;

import de.caritas.cob.videoservice.api.service.LogService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.springframework.web.context.request.WebRequest;

@RunWith(MockitoJUnitRunner.class)
public class ApiDefaultResponseEntityExceptionHandlerTest {

  @InjectMocks
  private ApiDefaultResponseEntityExceptionHandler exceptionHandler;

  @Mock
  private Logger logger;

  @Before
  public void setup() {
    setInternalState(LogService.class, "LOGGER", logger);
  }

  @Test
  public void handleInternal_Should_logInternalServerError_When_exceptionIsGiven() {
    RuntimeException exception = new RuntimeException("error");

    this.exceptionHandler.handleInternal(exception, mock(WebRequest.class));

    verify(this.logger, times(1))
        .error(eq("VideoService API: 500 Internal Server Error: {}"), anyString());
  }

}
