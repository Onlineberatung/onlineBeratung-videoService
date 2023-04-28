package de.caritas.cob.videoservice.api.service.session;

import de.caritas.cob.videoservice.api.service.TenantHeaderSupplier;
import de.caritas.cob.videoservice.api.service.securityheader.SecurityHeaderSupplier;
import de.caritas.cob.videoservice.userservice.generated.web.UserControllerApi;
import de.caritas.cob.videoservice.userservice.generated.web.model.ChatInfoResponseDTO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

/** Service class to provide handle session methods of the UserService. */
@Service
@RequiredArgsConstructor
public class ChatService {

  private final @NonNull UserControllerApi userControllerApi;
  private final @NonNull SecurityHeaderSupplier securityHeaderSupplier;
  private final @NonNull TenantHeaderSupplier tenantHeaderSupplier;

  public ChatInfoResponseDTO findChatById(Long chatId) {
    addDefaultHeaders();

    return userControllerApi.getChat(chatId);
  }

  public de.caritas.cob.videoservice.userservice.generated.web.model.ChatMembersResponseDTO
      getChatMembers(Long chatId) {
    addDefaultHeaders();
    return userControllerApi.getChatMembers(chatId);
  }

  public void assertCanModerateChat(Long chatId) {
    addDefaultHeaders();
    userControllerApi.verifyCanModerateChat(chatId);
  }

  private void addDefaultHeaders() {
    HttpHeaders headers = this.securityHeaderSupplier.getKeycloakAndCsrfHttpHeaders();
    tenantHeaderSupplier.addTenantHeader(headers);
    headers.forEach(
        (key, value) ->
            this.userControllerApi.getApiClient().addDefaultHeader(key, value.iterator().next()));
  }
}
