package de.caritas.cob.videoservice.api.facade;

import static de.caritas.cob.videoservice.api.service.session.SessionStatus.IN_PROGRESS;
import static de.caritas.cob.videoservice.api.service.session.SessionStatus.NEW;
import static de.caritas.cob.videoservice.api.testhelper.TestConstants.SESSION_ID;
import static de.caritas.cob.videoservice.api.testhelper.TestConstants.USERNAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.videoservice.api.authorization.AuthenticatedUser;
import de.caritas.cob.videoservice.api.exception.httpresponse.BadRequestException;
import de.caritas.cob.videoservice.api.service.liveevent.LiveEventNotificationService;
import de.caritas.cob.videoservice.api.service.session.SessionService;
import de.caritas.cob.videoservice.api.service.video.VideoCallUrlGeneratorService;
import de.caritas.cob.videoservice.api.service.video.jwt.model.VideoCallUrlPair;
import de.caritas.cob.videoservice.liveservice.generated.web.model.LiveEventMessage;
import de.caritas.cob.videoservice.liveservice.generated.web.model.VideoCallRequestDTO;
import de.caritas.cob.videoservice.userservice.generated.web.model.ConsultantSessionDTO;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
  private VideoCallUrlGeneratorService videoCallUrlGeneratorService;
  @Mock
  private AuthenticatedUser authenticatedUser;

  @Test
  public void startVideoCall_Should_ReturnVideoCallUrl_When_UrlWasGeneratedSuccessfully() {

    ConsultantSessionDTO consultantSessionDto = mock(ConsultantSessionDTO.class);
    when(consultantSessionDto.getStatus()).thenReturn(IN_PROGRESS.getValue());
    VideoCallUrlPair videoCallUrlPair = new EasyRandom().nextObject(VideoCallUrlPair.class);

    when(sessionService.findSessionOfCurrentConsultant(SESSION_ID))
        .thenReturn(consultantSessionDto);
    when(videoCallUrlGeneratorService.generateVideoCallUrlPair(any())).thenReturn(videoCallUrlPair);

    String result = startVideoCallFacade.startVideoCall(SESSION_ID, "rcUserId");

    assertThat(result, is(videoCallUrlPair.getBasicVideoUrl()));
  }

  @Test
  public void startVideoCall_Should_CallLiveServiceAndBuildCorrectLiveEventMessageWithVideoCallRequestDto() {

    ConsultantSessionDTO consultantSessionDto =
        new EasyRandom().nextObject(ConsultantSessionDTO.class);
    consultantSessionDto.setStatus(IN_PROGRESS.getValue());
    VideoCallUrlPair videoCallUrlPair = new EasyRandom().nextObject(VideoCallUrlPair.class);

    when(sessionService.findSessionOfCurrentConsultant(SESSION_ID))
        .thenReturn(consultantSessionDto);
    when(videoCallUrlGeneratorService.generateVideoCallUrlPair(any()))
        .thenReturn(videoCallUrlPair);
    when(authenticatedUser.getUsername()).thenReturn(USERNAME);
    ArgumentCaptor<LiveEventMessage> argument = ArgumentCaptor.forClass(LiveEventMessage.class);

    startVideoCallFacade.startVideoCall(SESSION_ID, "rcUserId");

    verify(liveEventNotificationService).sendVideoCallRequestLiveEvent(argument.capture(), any());
    verify(liveEventNotificationService, times(1))
        .sendVideoCallRequestLiveEvent(any(), any());
    assertThat(argument.getValue(), instanceOf(LiveEventMessage.class));
    assertEquals(consultantSessionDto.getGroupId(),
        ((VideoCallRequestDTO) argument.getValue().getEventContent()).getRcGroupId());
    assertEquals("rcUserId",
        ((VideoCallRequestDTO) argument.getValue().getEventContent()).getInitiatorRcUserId());
    assertEquals(USERNAME,
        ((VideoCallRequestDTO) argument.getValue().getEventContent()).getInitiatorUsername());
  }

  @Test(expected = BadRequestException.class)
  public void startVideoCall_Should_throwBadRequestException_When_sessionIsNotInProgress() {

    ConsultantSessionDTO consultantSessionDto = mock(ConsultantSessionDTO.class);
    when(consultantSessionDto.getStatus()).thenReturn(NEW.getValue());
    when(sessionService.findSessionOfCurrentConsultant(SESSION_ID))
        .thenReturn(consultantSessionDto);

    startVideoCallFacade.startVideoCall(SESSION_ID, "");
  }

}
