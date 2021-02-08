package de.caritas.cob.videoservice.api.service.video;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import de.caritas.cob.videoservice.api.exception.httpresponse.InternalServerErrorException;
import de.caritas.cob.videoservice.api.service.UuidRegistry;
import de.caritas.cob.videoservice.api.service.video.jwt.TokenGeneratorService;
import de.caritas.cob.videoservice.api.service.video.jwt.model.VideoCallTokenPair;
import de.caritas.cob.videoservice.api.service.video.jwt.model.VideoCallUrlPair;
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
  public void generateVideoCallUrlPair_Should_generateExpectedUrlPair_When_askerNameIsGiven() {
    setField(this.videoCallUrlGeneratorService, FIELD_NAME_VIDEO_CALL_URL, VIDEO_CALL_URL);
    when(this.uuidRegistry.generateUniqueUuid()).thenReturn("uniqueId");
    VideoCallTokenPair videoCallTokenPair = new EasyRandom().nextObject(VideoCallTokenPair.class);
    when(this.tokenGeneratorService.generateTokenPair(any(), any()))
        .thenReturn(videoCallTokenPair);

    VideoCallUrlPair videoCallUrlPair = this.videoCallUrlGeneratorService
        .generateVideoCallUrlPair("asker123");

    assertThat(videoCallUrlPair.getBasicVideoUrl(),
        is(VIDEO_CALL_URL + "/uniqueId?jwt=" + videoCallTokenPair.getBasicToken()));
    assertThat(videoCallUrlPair.getUserVideoUrl(),
        is(VIDEO_CALL_URL + "/uniqueId?jwt=" + videoCallTokenPair.getUserToken()));
  }

  @Test(expected = InternalServerErrorException.class)
  public void generateVideoCallUrlPair_Should_throwInternalServerErrorException_When_videoUrlIsInvalid() {
    VideoCallTokenPair videoCallTokenPair = new EasyRandom().nextObject(VideoCallTokenPair.class);
    when(this.tokenGeneratorService.generateTokenPair(any(), any()))
        .thenReturn(videoCallTokenPair);

    this.videoCallUrlGeneratorService.generateVideoCallUrlPair("asker123");
  }

}
