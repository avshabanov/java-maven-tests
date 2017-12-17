package com.alexshabanov.demo.mtls;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Simple server.
 */
public final class MyServlet extends HttpServlet {
  public static final String MY_NAME_PARAM = "myServlet.name";
  private String name = "<unknown>";

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    final String name = config.getInitParameter(MY_NAME_PARAM);
    if (name != null && name.length() > 0) {
      this.name = name;
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setHeader("Content-Type", "text/plain");
    try (final Writer writer = new OutputStreamWriter(resp.getOutputStream())) {
      writer.append("Hello from secured jetty!").append('\n');
      writer.append("Your request is proudly served by servlet ").append(name).append('\n');
      writer.append("Current Time: ").append(Long.toString(System.currentTimeMillis())).append('\n');
    }
  }
}
