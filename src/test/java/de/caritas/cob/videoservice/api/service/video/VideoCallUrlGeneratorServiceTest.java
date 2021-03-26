package de.caritas.cob.videoservice.api.service.video;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import de.caritas.cob.videoservice.api.exception.httpresponse.InternalServerErrorException;
import de.caritas.cob.videoservice.api.service.UuidRegistry;
import de.caritas.cob.videoservice.api.service.video.jwt.TokenGeneratorService;
import de.caritas.cob.videoservice.api.service.video.jwt.model.VideoCallToken;
import de.caritas.cob.videoservice.api.service.video.jwt.model.VideoCallUrls;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class VideoCallUrlGeneratorServiceTest {

  private static final String FIELD_NAME_VIDEO_CALL_URL = "videoCallServerUrl";
  private static final String VIDEO_CALL_URL = "http://video.call";

  @InjectMocks
  private VideoCallUrlGeneratorService videoCallUrlGeneratorService;

  @Mock
  private UuidRegistry uuidRegistry;

  @Mock
  private TokenGeneratorService tokenGeneratorService;

  @Test
  public void generateVideoCallUrls_Should_generateExpectedVideoCallUrls_When_askerNameIsGiven() {
    setField(this.videoCallUrlGeneratorService, FIELD_NAME_VIDEO_CALL_URL, VIDEO_CALL_URL);
    when(this.uuidRegistry.generateUniqueUuid()).thenReturn("uniqueId");
    VideoCallToken videoCallToken = new EasyRandom().nextObject(VideoCallToken.class);
    when(this.tokenGeneratorService.generateToken(any(), any()))
        .thenReturn(videoCallToken);

    VideoCallUrls videoCallUrls = this.videoCallUrlGeneratorService
        .generateVideoCallUrls("asker123");

    assertThat(videoCallUrls.getGuestVideoUrl(),
        is(VIDEO_CALL_URL + "/uniqueId?jwt=" + videoCallToken.getGuestToken()));
    assertThat(videoCallUrls.getUserVideoUrl(),
        is(VIDEO_CALL_URL + "/uniqueId?jwt=" + videoCallToken.getUserRelatedToken()));
    assertThat(videoCallUrls.getModeratorVideoUrl(),
        is(VIDEO_CALL_URL + "/uniqueId?jwt=" + videoCallToken.getModeratorToken()));
  }

  @Test(expected = InternalServerErrorException.class)
  public void generateVideoCallUrls_Should_throwInternalServerErrorException_When_videoUrlIsInvalid() {
    VideoCallToken videoCallToken = new EasyRandom().nextObject(VideoCallToken.class);
    when(this.tokenGeneratorService.generateToken(any(), any()))
        .thenReturn(videoCallToken);

    this.videoCallUrlGeneratorService.generateVideoCallUrls("asker123");
  }

}
