package com.example.api.security;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.security.test.context.support.WithSecurityContext;

public class SpringSecurityUtil {
  @Retention(RetentionPolicy.RUNTIME)
  @WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
  public @interface WithCustomMockUser {

    String username() default "user";

    String password() default "password";

    String userId() default "userId";

    String role() default "role";

    String groupId() default "groupId";
  }
}
