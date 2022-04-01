package de.caritas.cob.videoservice.api.authorization;

import static java.util.Objects.nonNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * Representation of the via Keycloak authenticated user.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AuthenticatedUser {

  @NonNull
  private String userId;

  @NonNull
  private String username;

  @NonNull
  private String accessToken;

  private Set<String> roles;

  @JsonIgnore
  public boolean isConsultant() {
    return nonNull(roles) && roles.contains(Authority.CONSULTANT.name());
  }
}
