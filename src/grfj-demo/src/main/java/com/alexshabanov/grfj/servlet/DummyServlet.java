package com.alexshabanov.grfj.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

/**
 * An Empty, "do nothing resource" to add to the context. Otherwise, the filters
 * will never kick in.
 */
public final class DummyServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN);

    try (final PrintWriter pw = new PrintWriter(resp.getOutputStream())) {
      pw.append("Hello from guice-resteasy-freemarker demo app").append('\n');
      pw.append("Generated at ").append(new Date().toString()).append('\n');
    }
  }
}