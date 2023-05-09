package de.caritas.cob.videoservice.api.service.message;

import de.caritas.cob.videoservice.api.model.VideoRoomEntity;
import de.caritas.cob.videoservice.api.service.httpheader.SecurityHeaderSupplier;
import de.caritas.cob.videoservice.api.service.httpheader.TenantHeaderSupplier;
import de.caritas.cob.videoservice.config.apiclient.MessageApiClient;
import de.caritas.cob.videoservice.messageservice.generated.web.model.AliasMessageDTO;
import de.caritas.cob.videoservice.messageservice.generated.web.model.MessageType;
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
public class MessageService {
  @Autowired private SecurityHeaderSupplier securityHeaderSupplier;

  @Autowired private TenantHeaderSupplier tenantHeaderSupplier;

  @Value("${message.service.api.url}")
  private String messageServiceApiUrl;

  public AliasMessageDTO createAndSendMessage(
      String rcGroupId, String title, VideoRoomEntity videoRoomEntity) {
    AliasMessageDTO message = createMessage(title, videoRoomEntity);
    sendMessage(rcGroupId, message);
    return message;
  }

  private AliasMessageDTO createMessage(String title, VideoRoomEntity videoRoomEntity) {
    AliasMessageDTO message = new AliasMessageDTO();
    JSONObject messageContent = new JSONObject();
    messageContent.put("title", title);
    message.setMessageType(MessageType.VIDEOCALL);
    messageContent.put("date", videoRoomEntity.getCreateDate());
    messageContent.put(
        "initiatinguser", messageContent.put("note", videoRoomEntity.getVideolink()));
    message.setContent(messageContent.toString());
    return message;
  }

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

  public de.caritas.cob.videoservice.messageservice.generated.web.MessageControllerApi
      getMessageControllerApi() {
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

  public void createAndSendVideoChatStartedMessage(
      String groupId, String username, VideoRoomEntity videoRoomEntity) {
    AliasMessageDTO message =
        createVideoChatMessage(
            username,
            videoRoomEntity,
            "Videochat has started. Initiating moderator has joined the call.");
    sendMessage(groupId, message);
  }

  public void createAndSendVideoChatJoinedMessage(
      String groupId, String username, VideoRoomEntity videoRoomEntity) {
    AliasMessageDTO message =
        createVideoChatMessage(username, videoRoomEntity, "Moderator joined the videochat");
    sendMessage(groupId, message);
  }

  private static AliasMessageDTO createVideoChatMessage(
      String username, VideoRoomEntity videoRoomEntity, String messageTitle) {
    AliasMessageDTO message = new AliasMessageDTO();
    JSONObject messageContent = new JSONObject();
    messageContent.put("title", messageTitle);
    message.setMessageType(MessageType.VIDEOCALL);
    messageContent.put("date", videoRoomEntity.getCreateDate());
    messageContent.put("moderator_user", username);
    messageContent.put("note", videoRoomEntity.getVideolink());
    message.setContent(messageContent.toString());
    return message;
  }
}
