package de.caritas.cob.videoservice.api.facade;

import static de.caritas.cob.videoservice.api.testhelper.FieldConstants.FIELD_NAME_VIDEO_CALL_URL_SUFFIX;
import static de.caritas.cob.videoservice.api.testhelper.FieldConstants.FIELD_VALUE_VIDEO_CALL_URL_SUFFIX;
import static de.caritas.cob.videoservice.api.testhelper.TestConstants.SESSION_ID;
import static de.caritas.cob.videoservice.api.testhelper.TestConstants.USERNAME;
import static de.caritas.cob.videoservice.api.testhelper.TestConstants.UUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.videoservice.api.authorization.AuthenticatedUser;
import de.caritas.cob.videoservice.api.exception.httpresponse.InternalServerErrorException;
import de.caritas.cob.videoservice.api.service.SessionService;
import de.caritas.cob.videoservice.api.service.UuidRegistry;
import de.caritas.cob.videoservice.api.service.liveevent.LiveEventNotificationService;
import de.caritas.cob.videoservice.liveservice.generated.web.model.LiveEventMessage;
import de.caritas.cob.videoservice.liveservice.generated.web.model.VideoCallRequestDTO;
import de.caritas.cob.videoservice.userservice.generated.web.model.ConsultantSessionDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.FieldSetter;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class StartVideoCallFacadeTest {

  @InjectMocks
  private StartVideoCallFacade startVideoCallFacade;
  @Mock
  private SessionService sessionService;
  @Mock
  private LiveEventNotificationService liveEventNotificationService;
  @Mock
  private UuidRegistry uuidRegistry;
  @Mock
  private AuthenticatedUser authenticatedUser;


  @Test
  public void startVideoCall_Should_ReturnVideoCallUrl_When_UrlWasGeneratedSuccessfully()
      throws NoSuchFieldException {

    FieldSetter.setField(startVideoCallFacade,
        startVideoCallFacade.getClass().getDeclaredField(FIELD_NAME_VIDEO_CALL_URL_SUFFIX),
        FIELD_VALUE_VIDEO_CALL_URL_SUFFIX);
    ConsultantSessionDTO consultantSessionDto = mock(ConsultantSessionDTO.class);

    when(sessionService.findSessionOfCurrentConsultant(SESSION_ID))
        .thenReturn(consultantSessionDto);
    when(uuidRegistry.generateUniqueUuid()).thenReturn(UUID);

    String result = startVideoCallFacade.startVideoCall(SESSION_ID);

    assertEquals(FIELD_VALUE_VIDEO_CALL_URL_SUFFIX + "/" + UUID, result);
  }

  @Test(expected = InternalServerErrorException.class)
  public void startVideoCall_Should_ThrowInternalServerError_When_UrlGenerationFails() {

    ConsultantSessionDTO consultantSessionDto = mock(ConsultantSessionDTO.class);

    when(sessionService.findSessionOfCurrentConsultant(SESSION_ID))
        .thenReturn(consultantSessionDto);
    when(uuidRegistry.generateUniqueUuid()).thenReturn(UUID);

    startVideoCallFacade.startVideoCall(SESSION_ID);
  }

  @Test
  public void startVideoCall_Should_CallLiveServiceAndBuildCorrectLiveEventMessageWithVideoCallRequestDto()
      throws NoSuchFieldException {

    FieldSetter.setField(startVideoCallFacade,
        startVideoCallFacade.getClass().getDeclaredField(FIELD_NAME_VIDEO_CALL_URL_SUFFIX),
        FIELD_VALUE_VIDEO_CALL_URL_SUFFIX);
    ConsultantSessionDTO consultantSessionDto = mock(ConsultantSessionDTO.class);
    when(sessionService.findSessionOfCurrentConsultant(SESSION_ID))
        .thenReturn(consultantSessionDto);
    when(uuidRegistry.generateUniqueUuid()).thenReturn(UUID);
    when(authenticatedUser.getUsername()).thenReturn(USERNAME);
    ArgumentCaptor<LiveEventMessage> argument = ArgumentCaptor.forClass(LiveEventMessage.class);

    startVideoCallFacade.startVideoCall(SESSION_ID);

    verify(liveEventNotificationService).sendVideoCallRequestLiveEvent(argument.capture(), any());
    verify(liveEventNotificationService, times(1))
        .sendVideoCallRequestLiveEvent(any(), any());
    assertTrue(argument.getValue() instanceof LiveEventMessage);
    assertEquals(consultantSessionDto.getGroupId(),
        ((VideoCallRequestDTO) argument.getValue().getEventContent()).getRcGroupId());
    assertEquals(consultantSessionDto.getConsultantRcId(),
        ((VideoCallRequestDTO) argument.getValue().getEventContent()).getRcUserId());
    assertEquals(USERNAME,
        ((VideoCallRequestDTO) argument.getValue().getEventContent()).getUsername());
  }
}
