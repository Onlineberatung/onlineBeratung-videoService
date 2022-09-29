package de.caritas.cob.videoservice.api.service.liveevent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.caritas.cob.videoservice.liveservice.generated.web.LiveControllerApi;
import de.caritas.cob.videoservice.liveservice.generated.web.model.LiveEventMessage;
import java.lang.management.ManagementFactory;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("testing")
@DirtiesContext
public class LiveEventNotificationServiceIT {

  @Autowired
  private LiveEventNotificationService underTest;

  @MockBean
  @Qualifier("liveControllerApi")
  @SuppressWarnings("unused")
  private LiveControllerApi liveControllerApi;

  @Test
  void sendVideoCallRequestLiveEventShouldRunInAnotherThread() {
    var threadCount = ManagementFactory.getThreadMXBean().getThreadCount();

    underTest.sendVideoCallRequestLiveEvent(new LiveEventMessage(), List.of());

    assertEquals(threadCount + 1, ManagementFactory.getThreadMXBean().getThreadCount());
  }
}
