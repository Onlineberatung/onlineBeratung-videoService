package de.caritas.cob.videoservice.api.service;

import de.caritas.cob.videoservice.api.tenant.TenantContext;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@RequiredArgsConstructor
@Slf4j
public class TenantHeaderSupplier {

  @Value("${multitenancy.enabled}")
  private boolean multitenancy;

  /**
   * Adds tenant header to given list of headers.
   *
   * @param headers given list of headers
   */
  public void addTenantHeader(HttpHeaders headers) {
    if (multitenancy) {
      headers.add("tenantId", TenantContext.getCurrentTenant().toString());
    }
  }

  /**
   * Resolve tenantID from current request.
   *
   * @return the id of the tenant
   */
  public Optional<Long> getTenantFromHeader() {
    var request =
        ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    try {
      return Optional.of(Long.parseLong(request.getHeader("tenantId")));
    } catch (NumberFormatException exception) {
      log.debug("No tenantId provided via headers.");
      return Optional.empty();
    }
  }
}
