package com.alexshabanov.demo.mtls;

import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.*;
import java.nio.CharBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Entry point to the app showing url connection (client) - to - jetty interaction.
 *
 * @author Alexander Shabanov
 */
public final class Main {
  private final Logger log = LoggerFactory.getLogger(getClass());
  private final int secureServerPort = 8443;
  private final ExecutorService executorService = Executors.newFixedThreadPool(2);
  private final Server server;

//  private final String keyStorePath = "./src/main/x509/"
//  private final char[] keyStorePassword = "ks-1234567".toCharArray();
//  private final char[] trustStorePassword = "ts-9876543".toCharArray();

  public static void main(String[] args) {
    // reroute all the JUL log lines to SLF4J
    SLF4JBridgeHandler.install();

    new Main().run();
  }

  //
  // Private
  //

  private Main() {
    server = new Server();
  }

  private void run() {
    log.info("Trying to start server at port={}", secureServerPort);

    configure(server);

    try {
      server.start();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void configure(Server server) {
    final SslContextFactory sslContextFactory = getServerContextFactory();

    final HttpConfiguration https = new HttpConfiguration();
    https.addCustomizer(new SecureRequestCustomizer());

    final ServerConnector httpsConnector = new ServerConnector(
        server,
        new SslConnectionFactory(sslContextFactory, "http/1.1"),
        new HttpConnectionFactory(https)
    );
    httpsConnector.setPort(secureServerPort);

    server.setConnectors(new Connector[] { httpsConnector });

    final ServletContextHandler root = new ServletContextHandler(
        server,
        "/",
        ServletContextHandler.NO_SESSIONS);

    final ServletHolder servletHolder = root.addServlet(MyServlet.class, "/*");
    servletHolder.setInitParameter(MyServlet.MY_NAME_PARAM, "dogey");
  }

  private SslContextFactory getServerContextFactory() {
    SslContextFactory contextFactory = new SslContextFactory();

    contextFactory.setKeyStorePath("./target/generated-certificates/server/keystore.jks");
    contextFactory.setKeyStorePassword(loadPasswordFromFile("./certgen/passwords/server-key.txt"));
    contextFactory.setKeyManagerPassword(loadPasswordFromFile("./certgen/passwords/server-key.txt"));
    //contextFactory.setTrustStorePath("");
    //contextFactory.setTrustStorePassword("");

    return contextFactory;
  }

  private static String loadPasswordFromFile(String filename) {
    try (final FileInputStream fileInputStream = new FileInputStream(new File(filename))) {
      try (final BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream))) {
        return reader.readLine();
      }
    } catch (IOException e) {
      throw new RuntimeException("Can't load file=" + filename, e);
    }
  }
}
