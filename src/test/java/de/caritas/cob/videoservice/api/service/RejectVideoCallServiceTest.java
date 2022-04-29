package de.caritas.cob.videoservice.api.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.videoservice.api.model.RejectVideoCallDTO;
import de.caritas.cob.videoservice.api.service.securityheader.SecurityHeaderSupplier;
import de.caritas.cob.videoservice.messageservice.generated.ApiClient;
import de.caritas.cob.videoservice.messageservice.generated.web.MessageControllerApi;
import de.caritas.cob.videoservice.messageservice.generated.web.model.VideoCallMessageDTO;
import de.caritas.cob.videoservice.messageservice.generated.web.model.VideoCallMessageDTO.EventTypeEnum;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;

@RunWith(MockitoJUnitRunner.class)
public class RejectVideoCallServiceTest {

  @InjectMocks
  private RejectVideoCallService rejectVideoCallService;

  @Mock
  private MessageControllerApi messageControllerApi;

  @Mock
  private SecurityHeaderSupplier securityHeaderSupplier;

  @Mock
  private TenantHeaderSupplier tenantHeaderSupplier;

  @Mock
  private ApiClient apiClient;

  @Test
  public void rejectVideoCall_Should_useServicesCorrectly() {
    when(this.messageControllerApi.getApiClient()).thenReturn(this.apiClient);
    when(this.securityHeaderSupplier.getKeycloakAndCsrfHttpHeaders()).thenReturn(new HttpHeaders());
    RejectVideoCallDTO rejectVideoCallDto = new EasyRandom().nextObject(RejectVideoCallDTO.class);

    this.rejectVideoCallService.rejectVideoCall(rejectVideoCallDto);

    VideoCallMessageDTO expectedMessage = new VideoCallMessageDTO()
        .eventType(EventTypeEnum.IGNORED_CALL)
        .initiatorUserName(rejectVideoCallDto.getInitiatorUsername())
        .initiatorRcUserId(rejectVideoCallDto.getInitiatorRcUserId());
    verify(this.securityHeaderSupplier).getKeycloakAndCsrfHttpHeaders();
    verify(this.tenantHeaderSupplier).addTenantHeader(any());
    verify(this.messageControllerApi)
        .createVideoHintMessage(rejectVideoCallDto.getRcGroupId(), expectedMessage);
  }

}
