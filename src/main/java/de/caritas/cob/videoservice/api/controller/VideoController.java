package de.caritas.cob.videoservice.api.controller;

import de.caritas.cob.videoservice.api.facade.StartVideoCallFacade;
import de.caritas.cob.videoservice.api.model.CreateVideoCallDTO;
import de.caritas.cob.videoservice.api.model.CreateVideoCallResponseDTO;
import de.caritas.cob.videoservice.generated.api.controller.VideocallsApi;
import io.swagger.annotations.Api;
import javax.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for video call requests.
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "video-controller")
public class VideoController implements VideocallsApi {

  private final @NonNull StartVideoCallFacade startVideoCallFacade;

  /**
   * Starts a new video call.
   *
   * @param createVideoCallDto {@link CreateVideoCallDTO}
   * @return response entity with {@link CreateVideoCallResponseDTO} body
   */
  @Override
  public ResponseEntity<CreateVideoCallResponseDTO> createVideoCall(
      @Valid CreateVideoCallDTO createVideoCallDto) {

    CreateVideoCallResponseDTO response = new CreateVideoCallResponseDTO()
        .videoCallUrl(startVideoCallFacade.startVideoCall(createVideoCallDto.getSessionId()));

    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }
}