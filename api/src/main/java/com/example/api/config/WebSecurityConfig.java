package com.example.api.config;

import com.example.api.service.AuthenticationService;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
  @Autowired private AuthenticationService authenticationService;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.csrf().disable();
  }

  @Bean
  public AbstractPreAuthenticatedProcessingFilter preAuthenticatedProcessingFilter()
      throws Exception {

    MyPreAuthenticatedProcessingFilter preAuthenticatedProcessingFilter =
        new MyPreAuthenticatedProcessingFilter();
    preAuthenticatedProcessingFilter.setAuthenticationManager(authenticationManager());

    return preAuthenticatedProcessingFilter;
  }

  @Bean
  PreAuthenticatedAuthenticationProvider tokenProvider() {
    PreAuthenticatedAuthenticationProvider provider = new PreAuthenticatedAuthenticationProvider();
    provider.setPreAuthenticatedUserDetailsService(authenticationService);
    provider.setUserDetailsChecker(new AccountStatusUserDetailsChecker());
    return provider;
  }

  static class MyPreAuthenticatedProcessingFilter extends AbstractPreAuthenticatedProcessingFilter {
    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
      return Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION)).orElse("");
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
      return "";
    }
  }

  public boolean isMovieUser(List<String> movieIdList, String movieId) {
    return movieIdList.contains(movieId);
  }
}
