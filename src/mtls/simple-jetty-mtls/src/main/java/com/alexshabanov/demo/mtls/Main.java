package com.alexshabanov.demo.mtls;

import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;

/**
 * Entry point to the app showing url connection (client) - to - jetty interaction.
 * Run this with these arg to demonstrate mTLS interaction: "-clientMode TLS -serverAuthMode REQUIRE".
 *
 * @author Alexander Shabanov
 */
public final class Main {
  private final Logger log = LoggerFactory.getLogger(getClass());
  private final int secureServerPort = 8443;
  private final Server server;

  public static void main(String[] args) {
    // reroute all the JUL log lines to SLF4J
    SLF4JBridgeHandler.install();

    // parse args
    ClientMode clientMode = ClientMode.SKIP;
    ServerAuthMode serverAuthMode = ServerAuthMode.SKIP;
    for (int i = 0; i < args.length; ++i) {
      final String argKey = args[i];
      final String argValue = (i + 1) == args.length ? "" : args[i + 1];

      if ("-clientMode".equals(argKey)) {
        clientMode = ClientMode.valueOf(argValue);
      } else if ("-serverAuthMode".equals(argKey)) {
        serverAuthMode = ServerAuthMode.valueOf(argValue);
      }
    }

    new Main().run(clientMode, serverAuthMode);
  }

  //
  // Private
  //

  private enum ClientMode {
    SKIP,
    SIMPLE_TLS,
    MUTUAL_TLS
  }

  private enum ServerAuthMode {
    SKIP,
    REQUIRE
  }

  private Main() {
    server = new Server();
  }

  private void run(ClientMode clientMode, ServerAuthMode serverAuthMode) {
    log.info(
        "Trying to start server at port={}, clientMode={}, serverAuthMode={}",
        secureServerPort,
        clientMode,
        serverAuthMode);

    configure(server, serverAuthMode == ServerAuthMode.REQUIRE);

    // start client thread
    new Thread(() -> {
      waitUntilStarted();
      log.info("Server started");

      if (clientMode == ClientMode.SIMPLE_TLS) {
        try {
          startSimpleTlsClient();
          server.stop();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      } else if (clientMode == ClientMode.MUTUAL_TLS) {
        try {
          startMutualTlsClient();
          server.stop();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    }).start();

    // start server
    try {
      log.info("About to start server");
      server.start();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void waitUntilStarted() {
    for (int i = 0; i < 100; ++i) {
      try {
        Thread.sleep(100L);
      } catch (InterruptedException e) {
        Thread.interrupted();
      }

      if (server.isRunning()) {
        return;
      }
    }

    throw new RuntimeException("Server is not running");
  }

  private void startSimpleTlsClient() throws GeneralSecurityException, IOException {
    // setup SSL socket factory
    final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    final String password = loadPasswordFromFile("./certgen/passwords/truststore-key.txt");
    tmf.init(loadPkcs12KeyStore(
        "./target/generated-certificates/root-ca/certs/truststore.p12",
        password));

    final SSLContext context = SSLContext.getInstance("TLS");
    context.init(null, tmf.getTrustManagers(), new SecureRandom());

    final URL url = new URL("https://localhost:" + secureServerPort + "/");
    final HttpsURLConnection httpsURLConnection = ((HttpsURLConnection) url.openConnection());

    log.info("Set up SSL Socket Factory");
    httpsURLConnection.setSSLSocketFactory(context.getSocketFactory());

    // fetch
    httpsURLConnection.connect();

    // display results
    final StringBuilder responseContentBuilder = new StringBuilder();
    try (final InputStreamReader in = new InputStreamReader((InputStream) httpsURLConnection.getContent())) {
      try (final BufferedReader reader = new BufferedReader(in)) {
        for (; ; ) {
          int ch = reader.read();
          if (ch < 0) {
            break;
          }
          responseContentBuilder.append(Character.valueOf((char) ch));
        }
      }
    }

    log.info("Retrieved data:\n{}", responseContentBuilder.toString());
  }

  private void startMutualTlsClient() throws GeneralSecurityException, IOException {
    // setup SSL socket factory
    final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    final String keystorePassword = loadPasswordFromFile("./certgen/passwords/client-key.txt");
    kmf.init(loadPkcs12KeyStore(
        "./target/generated-certificates/client/keystore.p12",
        keystorePassword), keystorePassword.toCharArray());

    final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    final String truststorePassword = loadPasswordFromFile("./certgen/passwords/truststore-key.txt");
    tmf.init(loadPkcs12KeyStore(
        "./target/generated-certificates/root-ca/certs/truststore.p12",
        truststorePassword));

    final SSLContext context = SSLContext.getInstance("TLS");
    context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

    final URL url = new URL("https://localhost:" + secureServerPort + "/");
    final HttpsURLConnection httpsURLConnection = ((HttpsURLConnection) url.openConnection());

    log.info("Set up SSL Socket Factory");
    httpsURLConnection.setSSLSocketFactory(context.getSocketFactory());

    // fetch
    httpsURLConnection.connect();

    // display results
    final StringBuilder responseContentBuilder = new StringBuilder();
    try (final InputStreamReader in = new InputStreamReader((InputStream) httpsURLConnection.getContent())) {
      try (final BufferedReader reader = new BufferedReader(in)) {
        for (; ; ) {
          int ch = reader.read();
          if (ch < 0) {
            break;
          }
          responseContentBuilder.append(Character.valueOf((char) ch));
        }
      }
    }

    log.info("Retrieved data:\n{}", responseContentBuilder.toString());
  }

  private void configure(Server server, boolean needClientAuth) {
    final SslContextFactory sslContextFactory = getServerContextFactory(needClientAuth);

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

  private SslContextFactory getServerContextFactory(boolean needClientAuth) {
    final SslContextFactory contextFactory = new SslContextFactory();

    final String serverKey = loadPasswordFromFile("./certgen/passwords/server-key.txt");

    contextFactory.setKeyStore(loadPkcs12KeyStore(
        "./target/generated-certificates/server/keystore.p12",
        serverKey));
    contextFactory.setKeyManagerPassword(serverKey);

    if (needClientAuth) {
      final String truststorePassword = loadPasswordFromFile("./certgen/passwords/truststore-key.txt");
      contextFactory.setTrustStore(loadPkcs12KeyStore(
          "./target/generated-certificates/root-ca/certs/truststore.p12",
          truststorePassword));

      // require client authorization
      contextFactory.setNeedClientAuth(true);
    }

    return contextFactory;
  }

  private static KeyStore loadPkcs12KeyStore(String filename, String password) {
    final KeyStore store;
    try {
      store = KeyStore.getInstance("PKCS12");
      try (final InputStream inputStream = new FileInputStream(new File(filename))) {
        store.load(inputStream, password.toCharArray());
        return store;
      }
    } catch (IOException | GeneralSecurityException e) {
      throw new RuntimeException(e);
    }
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
