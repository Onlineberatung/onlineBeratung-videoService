package de.caritas.cob.videoservice.api.service.liveevent;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import de.caritas.cob.videoservice.liveservice.generated.web.LiveControllerApi;
import de.caritas.cob.videoservice.liveservice.generated.web.model.LiveEventMessage;
import java.util.List;
import java.util.stream.Collectors;
import org.jeasy.random.EasyRandom;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LiveEventNotificationServiceTest {

  @InjectMocks
  private LiveEventNotificationService liveEventNotificationService;
  @Mock
  private LiveControllerApi liveControllerApi;

  private EasyRandom easyRandom;

  @Before
  public void setUp() {
    easyRandom = new EasyRandom();
  }

  @Test
  public void sendVideoCallRequestLiveEvent_Should_SendLiveEvent() {
    LiveEventMessage liveEventMessage = mock(LiveEventMessage.class);
    List<String> userIds = easyRandom
        .objects(String.class, 20)
        .collect(Collectors.toList());

    liveEventNotificationService.sendVideoCallRequestLiveEvent(liveEventMessage, userIds);

    verify(liveControllerApi, times(1))
        .sendLiveEvent(liveEventMessage.userIds(userIds));
  }
}
