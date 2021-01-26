package de.caritas.cob.videoservice.api.authorization;

import static de.caritas.cob.videoservice.api.authorization.Authority.CONSULTANT;
import static de.caritas.cob.videoservice.api.testhelper.TestConstants.ROLE_CONSULTANT;
import static de.caritas.cob.videoservice.api.testhelper.TestConstants.ROLE_UNKNOWN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AuthorityTest {

  @Test
  public void fromRoleName_Should_ReturnCorrectAuthority_ForKeycloakRoleConsultant() {

    Authority result = Authority.fromRoleName(ROLE_CONSULTANT);

    assertNotNull(result);
    assertEquals(CONSULTANT, result);
  }

  @Test
  public void fromRoleName_Should_ReturnNull_ForUnknownKeycloakRole() {

    Authority result = Authority.fromRoleName(ROLE_UNKNOWN);

    assertNull(result);
  }
}
