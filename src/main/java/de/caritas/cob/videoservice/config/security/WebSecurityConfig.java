package de.caritas.cob.videoservice.config.security;

import static de.caritas.cob.videoservice.api.authorization.Authority.CONSULTANT;
import static de.caritas.cob.videoservice.api.authorization.Authority.USER;

import de.caritas.cob.videoservice.api.authorization.RoleAuthorizationAuthorityMapper;
import de.caritas.cob.videoservice.config.SpringFoxConfig;
import de.caritas.cob.videoservice.filter.StatelessCsrfFilter;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.keycloak.adapters.springsecurity.filter.KeycloakAuthenticatedActionsFilter;
import org.keycloak.adapters.springsecurity.filter.KeycloakAuthenticationProcessingFilter;
import org.keycloak.adapters.springsecurity.filter.KeycloakPreAuthActionsFilter;
import org.keycloak.adapters.springsecurity.filter.KeycloakSecurityContextRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfFilter;

/** Configuration class to provide the Keycloak security configuration. */
@KeycloakConfiguration
public class WebSecurityConfig extends KeycloakWebSecurityConfigurerAdapter {

  private static final String UUID_PATTERN =
      "\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b";

  @Value("${csrf.cookie.property}")
  private String csrfCookieProperty;

  @Value("${csrf.header.property}")
  private String csrfHeaderProperty;

  /**
   * Configures the basic HTTP security behavior.
   *
   * @param http {@link HttpSecurity}
   */
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    super.configure(http);
    http.csrf()
        .disable()
        .addFilterBefore(
            new StatelessCsrfFilter(csrfCookieProperty, csrfHeaderProperty), CsrfFilter.class)
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .sessionAuthenticationStrategy(sessionAuthenticationStrategy())
        .and()
        .authorizeRequests()
        .antMatchers(SpringFoxConfig.WHITE_LIST)
        .permitAll()
        .antMatchers("/videocalls/new")
        .hasAuthority(CONSULTANT.getAuthority())
        .antMatchers("/videocalls/stop/{sessionId:" + UUID_PATTERN + "}")
        .hasAuthority(CONSULTANT.getAuthority())
        .antMatchers("/videocalls/reject")
        .hasAnyAuthority(USER.getAuthority())
        .antMatchers("/videocalls/*/jwt")
        .permitAll()
        .anyRequest()
        .denyAll()
        .and()
        .exceptionHandling()
        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));
  }

  /**
   * Uses the KeycloakSpringBootConfigResolver to be able to save the Keycloak settings in the
   * Spring application.properties.
   *
   * @return {@link KeycloakConfigResolver}
   */
  @Bean
  public KeycloakConfigResolver keyCloakConfigResolver() {
    return new KeycloakSpringBootConfigResolver();
  }

  /**
   * Changes springs authentication strategy to be stateless (no session is being created).
   *
   * @return {@link SessionAuthenticationStrategy}
   */
  @Bean
  @Override
  protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
    return new NullAuthenticatedSessionStrategy();
  }

  /**
   * Changes the default AuthenticationProvider to {@link KeycloakAuthenticationProvider} and
   * register it in the Spring security context. This maps the Keycloak roles to match the Spring
   * security roles (prefix ROLE_).
   *
   * @param auth {@link AuthenticationManagerBuilder}
   * @param authorityMapper custom {@link RoleAuthorizationAuthorityMapper}
   */
  @Autowired
  public void configureGlobal(
      AuthenticationManagerBuilder auth, RoleAuthorizationAuthorityMapper authorityMapper) {
    var keyCloakAuthProvider = keycloakAuthenticationProvider();
    keyCloakAuthProvider.setGrantedAuthoritiesMapper(authorityMapper);

    auth.authenticationProvider(keyCloakAuthProvider);
  }

  /**
   * From the Keycloak documentation: "Spring Boot attempts to eagerly register filter beans with
   * the web application context. Therefore, when running the Keycloak Spring Security adapter in a
   * Spring Boot environment, it may be necessary to add FilterRegistrationBeans to your security
   * configuration to prevent the Keycloak filters from being registered twice.": <a
   * href="https://github.com/keycloak/keycloak-documentation/blob/master/securing_apps/topics/oidc/java/spring-security-adapter.adoc">...</a>
   *
   * @param filter {@link KeycloakAuthenticationProcessingFilter}
   * @return {@link FilterRegistrationBean}
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  @Bean
  public FilterRegistrationBean keycloakAuthenticationProcessingFilterRegistrationBean(
      KeycloakAuthenticationProcessingFilter filter) {
    var registrationBean = new FilterRegistrationBean(filter);
    registrationBean.setEnabled(false);
    return registrationBean;
  }

  /**
   * See above method keycloakAuthenticationProcessingFilterRegistrationBean().
   *
   * @param filter {@link KeycloakPreAuthActionsFilter}
   * @return {@link FilterRegistrationBean}
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  @Bean
  public FilterRegistrationBean keycloakPreAuthActionsFilterRegistrationBean(
      KeycloakPreAuthActionsFilter filter) {
    var registrationBean = new FilterRegistrationBean(filter);
    registrationBean.setEnabled(false);
    return registrationBean;
  }

  /**
   * See above method keycloakAuthenticationProcessingFilterRegistrationBean().
   *
   * @param filter {@link KeycloakAuthenticatedActionsFilter}
   * @return {@link FilterRegistrationBean}
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  @Bean
  public FilterRegistrationBean keycloakAuthenticatedActionsFilterBean(
      KeycloakAuthenticatedActionsFilter filter) {
    var registrationBean = new FilterRegistrationBean(filter);
    registrationBean.setEnabled(false);
    return registrationBean;
  }

  /**
   * See above method keycloakAuthenticationProcessingFilterRegistrationBean().
   *
   * @param filter {@link KeycloakSecurityContextRequestFilter}
   * @return {@link FilterRegistrationBean}
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  @Bean
  public FilterRegistrationBean keycloakSecurityContextRequestFilterBean(
      KeycloakSecurityContextRequestFilter filter) {
    var registrationBean = new FilterRegistrationBean(filter);
    registrationBean.setEnabled(false);
    return registrationBean;
  }
}
