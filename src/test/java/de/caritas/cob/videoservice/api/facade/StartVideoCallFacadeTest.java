package de.caritas.cob.videoservice.api.facade;

import static de.caritas.cob.videoservice.api.service.session.SessionStatus.IN_PROGRESS;
import static de.caritas.cob.videoservice.api.service.session.SessionStatus.NEW;
import static de.caritas.cob.videoservice.api.testhelper.TestConstants.CONSULTANT_ID;
import static de.caritas.cob.videoservice.api.testhelper.TestConstants.SESSION_ID;
import static de.caritas.cob.videoservice.api.testhelper.TestConstants.USERNAME;
import static de.caritas.cob.videoservice.api.testhelper.TestConstants.VIDEO_CALL_UUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.videoservice.api.authorization.VideoUser;
import de.caritas.cob.videoservice.api.exception.httpresponse.BadRequestException;
import de.caritas.cob.videoservice.api.model.CreateVideoCallDTO;
import de.caritas.cob.videoservice.api.model.CreateVideoCallResponseDTO;
import de.caritas.cob.videoservice.api.service.UuidRegistry;
import de.caritas.cob.videoservice.api.service.liveevent.LiveEventNotificationService;
import de.caritas.cob.videoservice.api.service.session.SessionService;
import de.caritas.cob.videoservice.api.service.statistics.StatisticsService;
import de.caritas.cob.videoservice.api.service.statistics.event.StartVideoCallStatisticsEvent;
import de.caritas.cob.videoservice.api.service.video.VideoCallUrlGeneratorService;
import de.caritas.cob.videoservice.api.service.video.jwt.model.VideoCallUrls;
import de.caritas.cob.videoservice.liveservice.generated.web.model.LiveEventMessage;
import de.caritas.cob.videoservice.liveservice.generated.web.model.VideoCallRequestDTO;
import de.caritas.cob.videoservice.statisticsservice.generated.web.model.UserRole;
import de.caritas.cob.videoservice.userservice.generated.web.model.ConsultantSessionDTO;
import java.util.Objects;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

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
  private VideoUser authenticatedUser;
  @Mock
  private UuidRegistry uuidRegistry;
  @Mock
  private StatisticsService statisticsService;

  @Test
  public void startVideoCall_Should_ReturnCorrectVideoCallUrl_When_UrlWasGeneratedSuccessfully() {

    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);
    when(uuidRegistry.generateUniqueUuid()).thenReturn(VIDEO_CALL_UUID);
    ConsultantSessionDTO consultantSessionDto = mock(ConsultantSessionDTO.class);
    when(consultantSessionDto.getStatus()).thenReturn(IN_PROGRESS.getValue());
    VideoCallUrls videoCallUrls = new EasyRandom().nextObject(VideoCallUrls.class);

    when(sessionService.findSessionOfCurrentConsultant(SESSION_ID))
        .thenReturn(consultantSessionDto);
    when(videoCallUrlGeneratorService.generateVideoCallUrls(any())).thenReturn(videoCallUrls);

    CreateVideoCallResponseDTO result = startVideoCallFacade
        .startVideoCall(new CreateVideoCallDTO().sessionId(SESSION_ID), "rcUserId");

    assertThat(result.getModeratorVideoCallUrl(), is(videoCallUrls.getModeratorVideoUrl()));
  }

  @Test
  public void startVideoCall_Should_CallLiveServiceAndBuildCorrectLiveEventMessageWithVideoCallRequestDto() {

    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);
    when(uuidRegistry.generateUniqueUuid()).thenReturn(VIDEO_CALL_UUID);
    ConsultantSessionDTO consultantSessionDto =
        new EasyRandom().nextObject(ConsultantSessionDTO.class);
    consultantSessionDto.setStatus(IN_PROGRESS.getValue());
    VideoCallUrls videoCallUrls = new EasyRandom().nextObject(VideoCallUrls.class);

    when(sessionService.findSessionOfCurrentConsultant(SESSION_ID))
        .thenReturn(consultantSessionDto);
    when(videoCallUrlGeneratorService.generateVideoCallUrls(any()))
        .thenReturn(videoCallUrls);
    when(authenticatedUser.getUsername()).thenReturn(USERNAME);
    ArgumentCaptor<LiveEventMessage> argument = ArgumentCaptor.forClass(LiveEventMessage.class);

    startVideoCallFacade.startVideoCall(new CreateVideoCallDTO().sessionId(SESSION_ID), "rcUserId");

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

    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);
    when(uuidRegistry.generateUniqueUuid()).thenReturn(VIDEO_CALL_UUID);
    ConsultantSessionDTO consultantSessionDto = mock(ConsultantSessionDTO.class);
    when(consultantSessionDto.getStatus()).thenReturn(NEW.getValue());
    when(sessionService.findSessionOfCurrentConsultant(SESSION_ID))
        .thenReturn(consultantSessionDto);

    startVideoCallFacade.startVideoCall(new CreateVideoCallDTO().sessionId(SESSION_ID), "");
  }

  @Test
  public void startVideoCall_Should_FireAssignSessionStatisticsEvent() {

    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);
    when(uuidRegistry.generateUniqueUuid()).thenReturn(VIDEO_CALL_UUID);
    ConsultantSessionDTO consultantSessionDto = mock(ConsultantSessionDTO.class);
    when(consultantSessionDto.getStatus()).thenReturn(IN_PROGRESS.getValue());
    VideoCallUrls videoCallUrls = new EasyRandom().nextObject(VideoCallUrls.class);

    when(sessionService.findSessionOfCurrentConsultant(SESSION_ID))
        .thenReturn(consultantSessionDto);
    when(videoCallUrlGeneratorService.generateVideoCallUrls(any())).thenReturn(videoCallUrls);

    CreateVideoCallResponseDTO result = startVideoCallFacade
        .startVideoCall(new CreateVideoCallDTO().sessionId(SESSION_ID), "rcUserId");

    ArgumentCaptor<StartVideoCallStatisticsEvent> captor = ArgumentCaptor.forClass(
        StartVideoCallStatisticsEvent.class);
    verify(statisticsService, times(1)).fireEvent(captor.capture());
    String userId = Objects.requireNonNull(
        ReflectionTestUtils.getField(captor.getValue(), "userId")).toString();
    assertThat(userId, is(CONSULTANT_ID));
    String userRole = Objects.requireNonNull(
        ReflectionTestUtils.getField(captor.getValue(), "userRole")).toString();
    assertThat(userRole, is(
        UserRole.CONSULTANT.toString()));
    Long sessionId = Long.valueOf(Objects.requireNonNull(
        ReflectionTestUtils.getField(captor.getValue(), "sessionId")).toString());
    assertThat(sessionId, is(SESSION_ID));
    String videoCallUuid = Objects.requireNonNull(
        ReflectionTestUtils.getField(captor.getValue(), "videoCallUuid")).toString();
    assertThat(videoCallUuid, is(VIDEO_CALL_UUID));
  }

  @Test
  public void startVideoCall_Should_FireAssignSessionStatisticsEventWithDisplayName_When_initiatorDisplayNameIsSet() {

    when(authenticatedUser.getUserId()).thenReturn(CONSULTANT_ID);
    when(uuidRegistry.generateUniqueUuid()).thenReturn(VIDEO_CALL_UUID);
    ConsultantSessionDTO consultantSessionDto = mock(ConsultantSessionDTO.class);
    when(consultantSessionDto.getStatus()).thenReturn(IN_PROGRESS.getValue());
    VideoCallUrls videoCallUrls = new EasyRandom().nextObject(VideoCallUrls.class);

    when(sessionService.findSessionOfCurrentConsultant(SESSION_ID))
        .thenReturn(consultantSessionDto);
    when(videoCallUrlGeneratorService.generateVideoCallUrls(any())).thenReturn(videoCallUrls);

    startVideoCallFacade
        .startVideoCall(new CreateVideoCallDTO().sessionId(SESSION_ID)
            .initiatorDisplayName("initiator display name"), "rcUserId");

    var argument = ArgumentCaptor.forClass(LiveEventMessage.class);
    verify(liveEventNotificationService).sendVideoCallRequestLiveEvent(argument.capture(), any());
    verify(liveEventNotificationService).sendVideoCallRequestLiveEvent(any(), any());
    assertThat(argument.getValue(), instanceOf(LiveEventMessage.class));
    assertThat(((VideoCallRequestDTO) argument.getValue().getEventContent()).getInitiatorUsername(),
        is("initiator display name"));
  }

}
