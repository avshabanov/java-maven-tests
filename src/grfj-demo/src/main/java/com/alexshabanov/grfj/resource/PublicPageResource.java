package com.alexshabanov.grfj.resource;

import com.alexshabanov.grfj.ftl.ModelAndView;
import org.jboss.resteasy.annotations.GZIP;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author Alexander Shabanov
 */
@Path("g")
@Produces(MediaType.TEXT_HTML)
@GZIP
public class PublicPageResource {

  @GET
  @Path("index")
  public ModelAndView index() {
    return ModelAndView.of("index.ftl", "currentTime", System.currentTimeMillis());
  }
}
