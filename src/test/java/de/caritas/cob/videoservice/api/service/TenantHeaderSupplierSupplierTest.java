package de.caritas.cob.videoservice.api.service;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import de.caritas.cob.videoservice.api.tenant.TenantContext;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class TenantHeaderSupplierSupplierTest {

  private final TenantHeaderSupplier tenantHeaderSupplier = new TenantHeaderSupplier();

  @Test
  void addTenantHeader_Should_addTenantIdToHeaders_When_multiTenancyIsEnabled() {
    setField(tenantHeaderSupplier, "multitenancy", true);
    var httpHeaders = new HttpHeaders();
    TenantContext.setCurrentTenant(1L);

    tenantHeaderSupplier.addTenantHeader(httpHeaders);

    assertThat(httpHeaders.get("tenantId").get(0), is("1"));
  }

  @Test
  void addTenantHeader_Should_notAddTenantIdToHeaders_When_multiTenancyIsDisabled() {
    setField(tenantHeaderSupplier, "multitenancy", false);
    var httpHeaders = new HttpHeaders();

    tenantHeaderSupplier.addTenantHeader(httpHeaders);

    assertThat(httpHeaders.get("tenantId"), nullValue());
  }

  @Test
  void getTenantFromHeader_Should_returnTenantId_When_tenantIdExistsInHeader() {
    givenMockedServletRequestWithTenandIdHeaderValue("1");

    var result = tenantHeaderSupplier.getTenantFromHeader();

    assertThat(result.isPresent(), is(true));
    assertThat(result.get(), is(1L));
  }

  @Test
  void getTenantFromHeader_Should_returnOptionalEmpty_When_tenantIdIsNoNumber() {
    givenMockedServletRequestWithTenandIdHeaderValue("no number");

    var result = tenantHeaderSupplier.getTenantFromHeader();

    assertThat(result.isPresent(), is(false));
  }

  private void givenMockedServletRequestWithTenandIdHeaderValue(String tenantIdValue) {
    var mockedServletRequest = mock(HttpServletRequest.class);
    when(mockedServletRequest.getHeader("tenantId")).thenReturn(tenantIdValue);
    var requestAttributes = new ServletRequestAttributes(mockedServletRequest);
    RequestContextHolder.setRequestAttributes(requestAttributes);
  }

}
