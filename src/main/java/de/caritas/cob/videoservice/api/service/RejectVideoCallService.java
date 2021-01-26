package de.caritas.cob.videoservice.api.service;

import de.caritas.cob.videoservice.api.model.RejectVideoCallDTO;
import de.caritas.cob.videoservice.api.service.securityheader.SecurityHeaderSupplier;
import de.caritas.cob.videoservice.messageservice.generated.ApiClient;
import de.caritas.cob.videoservice.messageservice.generated.web.MessageControllerApi;
import de.caritas.cob.videoservice.messageservice.generated.web.model.VideoCallMessageDTO;
import de.caritas.cob.videoservice.messageservice.generated.web.model.VideoCallMessageDTO.EventTypeEnum;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

/**
 * Facade to encapsulate logic for the rejection of a video call.
 */
@Service
@RequiredArgsConstructor
public class RejectVideoCallService {

  private final @NonNull MessageControllerApi messageControllerApi;
  private final @NonNull SecurityHeaderSupplier securityHeaderSupplier;

  /**
   * Sends a system message with rejection type to the message service.
   *
   * @param rejectVideoCallDto {@link RejectVideoCallDTO} containing all necessary reject
   *                           information
   */
  public void rejectVideoCall(RejectVideoCallDTO rejectVideoCallDto) {
    addDefaultHeaders(this.messageControllerApi.getApiClient());
    this.messageControllerApi.createVideoHintMessage(rejectVideoCallDto.getRcGroupId(),
        fromRejectVideoCallDto(rejectVideoCallDto));
  }

  private void addDefaultHeaders(ApiClient apiClient) {
    HttpHeaders headers = this.securityHeaderSupplier.getKeycloakAndCsrfHttpHeaders();
    headers.forEach((key, value) -> apiClient.addDefaultHeader(key, value.iterator().next()));
  }

  private VideoCallMessageDTO fromRejectVideoCallDto(RejectVideoCallDTO rejectVideoCallDto) {
    return new VideoCallMessageDTO()
        .eventType(EventTypeEnum.IGNORED_CALL)
        .rcUserId(rejectVideoCallDto.getRcUserId())
        .initiatorUserName(rejectVideoCallDto.getInitiatorUsername());
  }

}
