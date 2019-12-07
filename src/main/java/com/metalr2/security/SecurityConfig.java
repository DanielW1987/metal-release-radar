package com.metalr2.security;

import com.metalr2.config.constants.Endpoints;
import com.metalr2.model.user.UserRole;
import com.metalr2.security.handler.CustomAccessDeniedHandler;
import com.metalr2.security.handler.CustomAuthenticationFailureHandler;
import com.metalr2.security.handler.CustomAuthenticationSuccessHandler;
import com.metalr2.security.handler.CustomLogoutSuccessHandler;
import com.metalr2.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Duration;

@EnableWebSecurity
@Configuration
@ConditionalOnProperty(
        name = "com.metalr2.security.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Value("${security.remember-me-secret}")
  private String REMEMBER_ME_SECRET;

  private final UserService userService;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;

  @Autowired
  public SecurityConfig(UserService userService, BCryptPasswordEncoder bCryptPasswordEncoder) {
    this.userService = userService;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
      .authorizeRequests()
        .antMatchers(Endpoints.AntPattern.ADMIN).hasRole(UserRole.ROLE_ADMINISTRATOR.getName())
        .antMatchers(Endpoints.AntPattern.RESOURCES).permitAll()
        .antMatchers(Endpoints.AntPattern.AUTH_PAGES).anonymous()
        .anyRequest().authenticated()
      .and()
      .formLogin()
        .loginPage(Endpoints.Guest.LOGIN)
        .loginProcessingUrl(Endpoints.Guest.LOGIN)
        .successHandler(new CustomAuthenticationSuccessHandler())
        .failureHandler(new CustomAuthenticationFailureHandler())
      .and()
      .rememberMe()
        .key(REMEMBER_ME_SECRET)
        .tokenValiditySeconds((int) Duration.ofDays(14).toSeconds())
      .and()
      .logout()
        .logoutUrl(Endpoints.Guest.LOGOUT).permitAll()
        .invalidateHttpSession(true)
        .clearAuthentication(true)
        .deleteCookies("JSESSIONID", "remember-me")
        .logoutSuccessHandler(new CustomLogoutSuccessHandler())
      .and()
      .exceptionHandling()
        .accessDeniedHandler(new CustomAccessDeniedHandler(() -> SecurityContextHolder.getContext().getAuthentication()));
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(userService).passwordEncoder(bCryptPasswordEncoder);
  }

}