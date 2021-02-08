package de.caritas.cob.videoservice.api.authorization;

import java.util.stream.Stream;

/**
 * Definition of all authorities and of the role-authority-mapping.
 */
public enum Authority {

  CONSULTANT("consultant", "AUTHORIZATION_CONSULTANT_DEFAULT"),
  USER("user", "AUTHORIZATION_USER_DEFAULT");

  private final String roleName;
  private final String authorityName;

  Authority(final String roleName, final String authorityName) {
    this.roleName = roleName;
    this.authorityName = authorityName;
  }

  /**
   * Finds a {@link Authority} instance by given roleName.
   *
   * @param roleName the role name to search for
   * @return the {@link Authority} instance
   */
  public static Authority fromRoleName(String roleName) {
    return Stream.of(values())
        .filter(authority -> authority.roleName.equals(roleName))
        .findFirst()
        .orElse(null);
  }

  /**
   * Returns the authority name for the given {@link Authority}.
   * 
   * @return authority name for the given {@link Authority}
   **/
  public String getAuthority() {
    return this.authorityName;
  }
}
