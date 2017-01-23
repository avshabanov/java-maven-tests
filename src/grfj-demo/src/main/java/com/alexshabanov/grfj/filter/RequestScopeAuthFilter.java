package com.alexshabanov.grfj.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;

/**
 * @author Alexander Shabanov
 */
@Singleton
@WebFilter(urlPatterns = "/rest/*")
public class RequestScopeAuthFilter implements Filter {
  private final Logger log = LoggerFactory.getLogger(getClass());

  public RequestScopeAuthFilter() {
    log.info("RequestScopeAuthFilter created");
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {

  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
    filterChain.doFilter(servletRequest, servletResponse);
  }

  @Override
  public void destroy() {
    log.info("RequestScopeAuthFilter destroyed");
  }
}
