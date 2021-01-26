package de.caritas.cob.videoservice.api.service;

import static de.caritas.cob.videoservice.api.testhelper.FieldConstants.FIELD_NAME_GENERATED_UUIDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.reflect.Whitebox.setInternalState;

import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class UuidRegistryTest {

  @InjectMocks
  private UuidRegistry uuidRegistry;
  @Mock
  private List<UUID> uuidList;

  @Before
  public void setUp() {
    setInternalState(UuidRegistry.class, FIELD_NAME_GENERATED_UUIDS, uuidList);
    uuidRegistry.cleanUpUuidRegistry();
  }

  @Test
  public void generateUniqueUuid_Should_ReturnValidUuid() {
    String response = uuidRegistry.generateUniqueUuid();

    assertThat(UUID.fromString(response), instanceOf(UUID.class));
  }

  @Test
  public void generateUniqueUuid_Should_AddGeneratedUuidToRegistry() {
    String response = uuidRegistry.generateUniqueUuid();

    assertThat(UUID.fromString(response), instanceOf(UUID.class));
    verify(uuidList, times(1)).add(UUID.fromString(response));
  }

  @Test
  public void cleanUpEntireList_Should_cleanListOfUuids() {
    uuidList.add(UUID.randomUUID());

    uuidRegistry.cleanUpUuidRegistry();

    assertEquals(0, uuidList.size());
  }
}
