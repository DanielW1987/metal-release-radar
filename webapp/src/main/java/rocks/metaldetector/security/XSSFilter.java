package rocks.metaldetector.security;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class XSSFilter extends OncePerRequestFilter {

  @Override
  public void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain) throws ServletException, IOException {
    XSSRequestWrapper wrappedRequest = new XSSRequestWrapper(request);
    String body = IOUtils.toString(wrappedRequest.getReader());
    if (!StringUtils.isBlank(body)) {
      body = XSSUtils.stripXSS(body);
      wrappedRequest.resetInputStream(body.getBytes());
    }
    chain.doFilter(wrappedRequest, response);
  }
}
