package de.caritas.cob.videoservice.config.resttemplate;

import static java.util.Arrays.asList;

import de.caritas.cob.videoservice.api.exception.httpresponse.InternalServerErrorException;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import lombok.NonNull;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.server.ResponseStatusException;

/** Custom rest template error handler to deal with unexpected client errors. */
public class CustomResponseErrorHandler extends DefaultResponseErrorHandler {

  private static final List<HttpStatus> LOOP_THROUGH_STATUS_CODES =
      asList(HttpStatus.FORBIDDEN, HttpStatus.NOT_FOUND, HttpStatus.BAD_REQUEST);

  @Override
  public void handleError(@NonNull URI url, @NonNull HttpMethod method, ClientHttpResponse response)
      throws IOException {
    if (isLoopThroughStatusCode(response.getStatusCode())) {
      throw new ResponseStatusException(response.getStatusCode(), method.name() + " " + url);
    }
    throw new InternalServerErrorException(response.getStatusText());
  }

  private boolean isLoopThroughStatusCode(HttpStatus httpStatus) {
    return LOOP_THROUGH_STATUS_CODES.contains(httpStatus);
  }
}
