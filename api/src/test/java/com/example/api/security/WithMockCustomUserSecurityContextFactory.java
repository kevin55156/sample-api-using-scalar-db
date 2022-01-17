package com.example.api.security;

import com.example.api.security.SpringSecurityUtil.WithCustomMockUser;
import com.example.api.service.AuthenticationService.AccountUser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockCustomUserSecurityContextFactory
    implements WithSecurityContextFactory<WithCustomMockUser> {

  @Override
  public SecurityContext createSecurityContext(WithCustomMockUser user) {
    List<GrantedAuthority> authorities =
        new ArrayList<GrantedAuthority>(Arrays.asList(new SimpleGrantedAuthority(user.role())));
    List<String> groupIdList = new ArrayList<String>(Arrays.asList(user.groupId()));
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    AccountUser principal =
        new AccountUser(user.username(), user.password(), authorities, user.userId(), groupIdList);
    Authentication authentication =
        new UsernamePasswordAuthenticationToken(
            principal, principal.getPassword(), principal.getAuthorities());
    context.setAuthentication(authentication);
    return context;
  }
}
