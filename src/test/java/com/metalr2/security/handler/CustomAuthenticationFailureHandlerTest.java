package com.metalr2.security.handler;

import com.metalr2.config.constants.Endpoints;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

class CustomAuthenticationFailureHandlerTest implements WithAssertions {

  private AuthenticationFailureHandler authenticationFailureHandler;
  private MockHttpServletRequest       httpServletRequest;
  private MockHttpServletResponse      httpServletResponse;

  @BeforeEach
  void setup() {
    authenticationFailureHandler = new CustomAuthenticationFailureHandler();
    httpServletRequest           = new MockHttpServletRequest();
    httpServletResponse          = new MockHttpServletResponse();
  }

  @Test
  void locateToLoginPageIfUserIsDisabled() throws Exception {
    authenticationFailureHandler.onAuthenticationFailure(httpServletRequest, httpServletResponse, new DisabledException("dummy"));

    assertThat(httpServletResponse.getStatus()).isEqualTo(HttpStatus.MOVED_TEMPORARILY.value());
    assertThat(httpServletResponse.getHeader(HttpHeaders.LOCATION)).isEqualTo(Endpoints.Guest.LOGIN + "?disabled");
  }

  @Test
  void locateToLoginPageIfUserHasBadCredentials() throws Exception {
    authenticationFailureHandler.onAuthenticationFailure(httpServletRequest, httpServletResponse, new BadCredentialsException("dummy"));

    assertThat(httpServletResponse.getStatus()).isEqualTo(HttpStatus.MOVED_TEMPORARILY.value());
    assertThat(httpServletResponse.getRedirectedUrl()).isEqualTo(Endpoints.Guest.LOGIN + "?badCredentials");
  }

}
