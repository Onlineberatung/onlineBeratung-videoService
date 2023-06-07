package de.caritas.cob.videoservice.api.service.message;

import de.caritas.cob.videoservice.api.model.VideoRoomEntity;
import de.caritas.cob.videoservice.api.service.httpheader.SecurityHeaderSupplier;
import de.caritas.cob.videoservice.api.service.httpheader.TenantHeaderSupplier;
import de.caritas.cob.videoservice.config.apiclient.MessageApiClient;
import de.caritas.cob.videoservice.messageservice.generated.web.MessageControllerApi;
import de.caritas.cob.videoservice.messageservice.generated.web.model.AliasMessageDTO;
import de.caritas.cob.videoservice.messageservice.generated.web.model.MessageType;
import de.caritas.cob.videoservice.messageservice.generated.web.model.VideoCallMessageDTO;
import de.caritas.cob.videoservice.messageservice.generated.web.model.VideoCallMessageDTO.EventTypeEnum;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class MessageService {
  @Autowired private SecurityHeaderSupplier securityHeaderSupplier;

  @Autowired private TenantHeaderSupplier tenantHeaderSupplier;

  @Value("${message.service.api.url}")
  private String messageServiceApiUrl;

  private void sendMessage(String rcGroupId, AliasMessageDTO message) {
    var messageControllerApi = getMessageControllerApi();
    addDefaultHeaders(messageControllerApi.getApiClient());
    messageControllerApi.saveAliasMessageWithContent(rcGroupId, message);
  }

  private void addDefaultHeaders(
      de.caritas.cob.videoservice.messageservice.generated.ApiClient apiClient) {
    var headers = this.securityHeaderSupplier.getKeycloakAndCsrfHttpHeaders();
    tenantHeaderSupplier.addTenantHeader(headers);
    headers.forEach((key, value) -> apiClient.addDefaultHeader(key, value.iterator().next()));
  }

  public MessageControllerApi getMessageControllerApi() {
    final RestTemplate restTemplate = new RestTemplate();
    final HttpComponentsClientHttpRequestFactory factory =
        new HttpComponentsClientHttpRequestFactory();
    final HttpClient httpClient =
        HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
    factory.setHttpClient(httpClient);
    restTemplate.setRequestFactory(factory);
    de.caritas.cob.videoservice.messageservice.generated.ApiClient apiClient =
        new MessageApiClient(restTemplate);
    apiClient.setBasePath(this.messageServiceApiUrl);
    return new de.caritas.cob.videoservice.messageservice.generated.web.MessageControllerApi(
        apiClient);
  }

  public void createAndSendVideoCallStartedMessage(
      String groupId,
      String username,
      VideoRoomEntity videoRoomEntity,
      String guestVideoUrl,
      String initiatorDisplayName,
      String initiatorRcUserId) {
    AliasMessageDTO message =
        createVideoChatMessage(
            username,
            videoRoomEntity,
            guestVideoUrl,
            "Videochat has started. Initiating moderator has joined the call.",
            EventTypeEnum.CALL_STARTED);
    message.getVideoCallMessageDTO().setInitiatorRcUserId(initiatorRcUserId);
    message.getVideoCallMessageDTO().setInitiatorUserName(initiatorDisplayName);
    sendMessage(groupId, message);
  }

  public void createAndSendVideoCallEndedMessage(
      String groupId, String username, VideoRoomEntity videoRoomEntity, String guestVideoUrl) {
    AliasMessageDTO message =
        createVideoChatMessage(
            username,
            videoRoomEntity,
            guestVideoUrl,
            "Videochat has ended. All moderators have left the call.",
            EventTypeEnum.CALL_ENDED);
    message.getVideoCallMessageDTO().setInitiatorRcUserId("");
    message.getVideoCallMessageDTO().setInitiatorUserName("");
    sendMessage(groupId, message);
  }

  public void createAndSendVideoCallEndedMessage(
      String groupId, String username, VideoRoomEntity videoRoomEntity) {
    createAndSendVideoCallEndedMessage(groupId, username, videoRoomEntity, "");
  }

  private AliasMessageDTO createVideoChatMessage(
      String username,
      VideoRoomEntity videoRoomEntity,
      String guestVideoUrl,
      String messageTitle,
      EventTypeEnum eventType) {
    AliasMessageDTO message = new AliasMessageDTO();

    message.setContent(
        getMessageContent(
                username, videoRoomEntity, guestVideoUrl, messageTitle, message, eventType)
            .toString());
    message.setVideoCallMessageDTO(new VideoCallMessageDTO().eventType(eventType));
    return message;
  }

  JSONObject getMessageContent(
      String username,
      VideoRoomEntity videoRoomEntity,
      String guestVideoUrl,
      String messageTitle,
      AliasMessageDTO message,
      EventTypeEnum eventType) {
    JSONObject messageContent = new JSONObject();
    messageContent.put("title", messageTitle);
    message.setMessageType(MessageType.VIDEOCALL);
    messageContent.put("date", videoRoomEntity.getCreateDate());
    messageContent.put("moderator_user", username);
    messageContent.put("note", guestVideoUrl);
    long calculateDurationInSecods = calculateDurationInSeconds(videoRoomEntity);
    messageContent.put("durationSeconds", calculateDurationInSecods);
    messageContent.put("eventType", eventType.getValue());
    return messageContent;
  }

  private long calculateDurationInSeconds(VideoRoomEntity videoRoomEntity) {
    if (videoRoomEntity.getCreateDate() != null) {
      return Duration.between(videoRoomEntity.getCreateDate(), LocalDateTime.now()).getSeconds();
    } else {
      log.warn("VideoRoomEntity.getCreateDate() is null, assuming duration seconds is zero");
      return 0;
    }
  }
}
