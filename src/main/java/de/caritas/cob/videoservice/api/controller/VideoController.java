package de.caritas.cob.videoservice.api.controller;

import de.caritas.cob.videoservice.api.facade.VideoCallFacade;
import de.caritas.cob.videoservice.api.model.CreateVideoCallDTO;
import de.caritas.cob.videoservice.api.model.RejectVideoCallDTO;
import de.caritas.cob.videoservice.api.model.VideoCallInfoDTO;
import de.caritas.cob.videoservice.api.model.VideoCallResponseDTO;
import de.caritas.cob.videoservice.api.service.RejectVideoCallService;
import de.caritas.cob.videoservice.api.service.video.VideoCallUrlGeneratorService;
import de.caritas.cob.videoservice.api.tenant.TenantContext;
import de.caritas.cob.videoservice.generated.api.controller.VideocallsApi;
import io.swagger.annotations.Api;
import java.util.UUID;
import javax.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/** Controller for video call requests. */
@RestController
@RequiredArgsConstructor
@Api(tags = "video-controller")
public class VideoController implements VideocallsApi {

  private final @NonNull VideoCallFacade videoCallFacade;
  private final @NonNull RejectVideoCallService rejectVideoCallService;
  private final @NonNull VideoCallUrlGeneratorService videoCallUrlGeneratorService;

  @Value("${multitenancy.enabled}")
  private boolean multitenancy;

  /**
   * Starts a new video call.
   *
   * @param createVideoCallDto {@link CreateVideoCallDTO}
   * @return response entity with {@link VideoCallResponseDTO} body
   */
  @Override
  public ResponseEntity<VideoCallResponseDTO> createVideoCall(
      @RequestHeader String rcUserId, @Valid CreateVideoCallDTO createVideoCallDto) {
    var response = videoCallFacade.startVideoCall(createVideoCallDto, rcUserId);

    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @Override
  public ResponseEntity<VideoCallResponseDTO> joinGroupVideoCall(UUID roomId) {
    var response = videoCallFacade.joinGroupVideoCall(roomId.toString());

    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<Void> stopVideoCall(UUID roomId) {
    videoCallFacade.stopVideoCall(roomId.toString());

    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> handleVideoCallStoppedEvent(String roomId) {
    if (multitenancy) {
      // prosody events are tenant-unaware, therefore set the tenant to 0 to be able to send
      // messages
      TenantContext.setCurrentTenant(0L);
    }
    videoCallFacade.handleVideoCallStoppedEvent(roomId);
    return ResponseEntity.noContent().build();
  }

  /**
   * Rejects a video call.
   *
   * @param rejectVideoCallDto {@link RejectVideoCallDTO}
   * @return response entity
   */
  @Override
  public ResponseEntity<Void> rejectVideoCall(@Valid RejectVideoCallDTO rejectVideoCallDto) {
    this.rejectVideoCallService.rejectVideoCall(rejectVideoCallDto);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Override
  public ResponseEntity<VideoCallInfoDTO> getWebToken(String roomId) {
    var videoCallInfo = videoCallUrlGeneratorService.generateJwt(roomId);
    return ResponseEntity.ok(videoCallInfo);
  }
}
