package de.caritas.cob.videoservice.api.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;

import java.util.UUID;
import org.junit.Before;
import org.junit.Test;

public class UuidRegistryTest {

  private final UuidRegistry uuidRegistry = new UuidRegistry();

  @Before
  public void cleanup() {
    uuidRegistry.cleanUpEntireList();
  }

  @Test
  public void generateUniqueUuid_Should_ReturnUuidString() {
    String response = uuidRegistry.generateUniqueUuid();

    assertThat(UUID.fromString(response), instanceOf(UUID.class));
  }
}
