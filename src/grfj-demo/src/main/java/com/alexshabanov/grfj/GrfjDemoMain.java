package com.alexshabanov.grfj;

import com.alexshabanov.grfj.config.ResourceModule;
import com.alexshabanov.grfj.filter.RequestScopeAuthFilter;
import com.alexshabanov.grfj.servlet.DummyServlet;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;
import org.jboss.resteasy.plugins.guice.ext.RequestScopeModule;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * @author Alexander Shabanov
 */
public class GrfjDemoMain {

  public static void main(String[] args) throws Exception {
    // route JUL to SLF4J
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();

    final Injector injector = Guice.createInjector(new AppModule(), new ResourceModule());

    final Server server = new Server(8080);

    // wait for 100ms to complete pending requests before final shutdown
    server.setStopTimeout(100);

    // stop server if SIGINT received
    server.setStopAtShutdown(true);

    final ServletContextHandler servletHandler = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);

    servletHandler.addServlet(DummyServlet.class, "/hello"); // TODO: remove

    servletHandler.addEventListener(injector.getInstance(GuiceResteasyBootstrapServletContextListener.class));

    final ServletHolder resteasyServletHolder = new ServletHolder(HttpServletDispatcher.class);
    //servletHandler.setInitParameter("resteasy.role.based.security", "true");

    // TODO: implement auth filter
    //servletHandler.addFilter(new FilterHolder(injector.getInstance(RequestScopeAuthFilter.class)), "/rest/*", null);
    servletHandler.addServlet(resteasyServletHolder, "/*");

    server.setHandler(servletHandler);
    server.start();
    server.join();

    LoggerFactory.getLogger(GrfjDemoMain.class).info("Closing...");
  }

  private static final class AppModule extends RequestScopeModule implements Module {

    @Override
    protected void configure() {
      super.configure();
      bind(RequestScopeAuthFilter.class);
    }
  }
}
