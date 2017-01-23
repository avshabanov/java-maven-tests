package com.alexshabanov.grfj.ftl;

import com.google.common.base.Strings;
import freemarker.template.*;
import freemarker.template.utility.ObjectWrapperWithAPISupport;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author Alexander Shabanov
 */
@Provider
@Consumes(MediaType.TEXT_HTML)
@Produces(MediaType.TEXT_HTML)
public class FreemarkerProvider implements MessageBodyWriter<ModelAndView> {
  private final Configuration configuration;
  private ObjectWrapperWithAPISupport objectWrapper;

  public Configuration getConfiguration() {
    return configuration;
  }

  public FreemarkerProvider(Configuration configuration) {
    this.configuration = configuration;
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return ModelAndView.class.equals(type) && MediaType.TEXT_HTML_TYPE.isCompatible(mediaType);
  }

  @Override
  public long getSize(ModelAndView modelAndView, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(ModelAndView modelAndView,
                      Class<?> type,
                      Type genericType,
                      Annotation[] annotations,
                      MediaType mediaType,
                      MultivaluedMap<String, Object> httpHeaders,
                      OutputStream entityStream) throws IOException, WebApplicationException {
    final DefaultMapAdapter mapAdapter = DefaultMapAdapter.adapt(modelAndView.getParameters(), getObjectWrapper());
    final Template template = getTemplate(modelAndView.getViewName(), annotations, mediaType, httpHeaders);
    try (final Writer writer = createWriter(mediaType, httpHeaders, entityStream)) {
      template.process(mapAdapter, writer);
    } catch (TemplateException e) {
      throw new WebApplicationException("Unable to process view template", e, Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  //
  // Protected
  //

  protected Writer createWriter(MediaType mediaType,
                                MultivaluedMap<String, Object> httpHeaders,
                                OutputStream entityStream) {
    Charset charset = StandardCharsets.UTF_8;
    if (mediaType.getParameters() != null && mediaType.getParameters().containsKey(MediaType.CHARSET_PARAMETER)) {
      final String proposedCharset = mediaType.getParameters().get(MediaType.CHARSET_PARAMETER);
      if (!Strings.isNullOrEmpty(proposedCharset)) {
        charset = Charset.forName(proposedCharset);
      }
    }

    return new OutputStreamWriter(entityStream, charset);
  }

  protected Template getTemplate(String viewName,
                                 Annotation[] annotations,
                                 MediaType mediaType,
                                 MultivaluedMap<String, Object> httpHeaders) throws IOException {
    return configuration.getTemplate(viewName);
  }

  /**
   * Return the configured FreeMarker {@link ObjectWrapper}, or the
   * {@link ObjectWrapper#DEFAULT_WRAPPER default wrapper} if none specified.
   * @see freemarker.template.Configuration#getObjectWrapper()
   */
  protected ObjectWrapperWithAPISupport getObjectWrapper() {
    if (objectWrapper == null) {
      final ObjectWrapper ow = getConfiguration().getObjectWrapper();
      if (ow == null) {
        objectWrapper = new SimpleObjectWrapper(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
      } else if (ow instanceof ObjectWrapperWithAPISupport) {
        objectWrapper = (ObjectWrapperWithAPISupport) ow;
      } else {
        throw new IllegalStateException("Can't get object wrapper");
      }
    }
    return objectWrapper;
  }
}
