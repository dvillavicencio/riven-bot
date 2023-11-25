package com.danielvm.destiny2bot.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.springframework.web.util.ContentCachingRequestWrapper;

/**
 * Filter intended to cache the request body's bytes before being bound using @RequestBody This is
 * needed as the annotation {@link com.danielvm.destiny2bot.annotation.ValidSignature} validates
 * signatures from requests as they are received, and the InputStream from the request body can only
 * be consumed once. This class caches it, so it can be used more than once, to have the request
 * body at hand, and to validate the request signature.
 */
public class CachingRequestBodyFilter implements Filter {

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    if (request instanceof HttpServletRequest httpRequest) {
      ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(httpRequest);
      chain.doFilter(wrappedRequest, response);
    } else {
      chain.doFilter(request, response);
    }
  }
}
