package com.truward.brikar.web.resource;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * @author Alexander Shabanov
 */
@Path("rest/admin")
@ParametersAreNonnullByDefault
public final class ConfigResource {
  private Map<?, ?> appProperties;
  private boolean includeSystemProperties;

  public ConfigResource() {
    setAppProperties(null);
    setIncludeSystemProperties(true);
  }

  public void setAppProperties(@Nullable Map<?, ?> appProperties) {
    this.appProperties = appProperties;
  }

  public void setIncludeSystemProperties(boolean includeSystemProperties) {
    this.includeSystemProperties = includeSystemProperties;
  }

  @GET
  @Path("config")
  protected Response getConfig() {
    final StreamingOutput stream = os -> {
      final PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(os)));
      writer.append("Generated at ").append(new Date().toString()).append('\n').append('\n');
      appendProperties(writer);
      writer.flush();
    };

    return Response.ok(stream).build();
  }

  //
  // Private
  //

  private void appendProperties(PrintWriter writer) throws IOException {
    if (includeSystemProperties) {
      writeProperties(writer, "System", System.getProperties());
    }

    if (appProperties != null) {
      writeProperties(writer, "Application", appProperties);
    }
  }

  private static void writeProperties(PrintWriter writer, String propertyBlockName, Map<?, ?> properties) {
    writer.append(propertyBlockName).append(' ').append("properties:\n");
    for (final Map.Entry<?, ?> entry : properties.entrySet()) {
      writer
          .append(Objects.toString(entry.getKey()))
          .append('=')
          .append(Objects.toString(entry.getValue().toString()))
          .append('\n');
    }
    writer.append('\n');
  }
}
