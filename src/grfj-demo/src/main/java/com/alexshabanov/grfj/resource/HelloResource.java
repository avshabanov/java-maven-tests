package com.alexshabanov.grfj.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * @author Alexander Shabanov
 */
@Path("rest/hello")
public final class HelloResource  {
  private final Logger log = LoggerFactory.getLogger(getClass());

  public HelloResource() {
    if (log.isDebugEnabled()) {
      log.info("HelloResource created");
    }
  }

  @GET
  @Path("{name}")
  public String hello(@PathParam("name") final String name) {
    if (log.isDebugEnabled()) {
      log.debug("HelloResource.hello(name={})", name);
    }

    return "Hello " + name + " @ "  + System.currentTimeMillis();
  }
}
