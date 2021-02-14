package com.alexshabanov.bdbsample;

import com.google.common.io.BaseEncoding;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sleepycat.je.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public final class HelloBdbApp implements Runnable {

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Override
  public void run() {
    log.info("Hello");
    try {
      runDb();
    } catch (Exception e) {
      log.error("RunDB error", e);
    }
  }

  private static String toHexString(byte[] bytes) {
    return BaseEncoding.base16().lowerCase().encode(bytes);
  }

  private void runDb() throws Exception {
    final File envHome = File.createTempFile("TestDB-", "-bdb");
    final EnvironmentConfig environmentConfig = new EnvironmentConfig();
    environmentConfig.setAllowCreate(true);
    environmentConfig.setTransactional(true);
    if (!envHome.delete()) {
      log.debug("Can't delete old file");
    }
    if (!envHome.mkdir()) {
      throw new IOException("Can't create " + envHome.getAbsolutePath());
    }

    final Environment env = new Environment(envHome, environmentConfig);
    final DatabaseConfig dbConfig = new DatabaseConfig();
    dbConfig.setTransactional(true);
    dbConfig.setAllowCreate(true);
    dbConfig.setSortedDuplicates(false);
    final Database db = env.openDatabase(null, "mydatabase", dbConfig);

    final DatabaseEntry entry = new DatabaseEntry("key".getBytes());

    // put 1
    DatabaseEntry value = new DatabaseEntry("Value".getBytes());
    db.put(null, entry, value);
    final DatabaseEntry out = new DatabaseEntry();
    db.get(null, entry, out, LockMode.READ_COMMITTED);
    log.info("[1] out={}", toHexString(out));

    // put 2
    value = new DatabaseEntry("AnotherValue".getBytes());
    db.put(null, entry, value);
    db.get(null, entry, out, LockMode.READ_COMMITTED);
    log.info("[2] out={} <-- Might still be an old value", toHexString(out));

    // put 3
    value = new DatabaseEntry("ABC".getBytes());
    db.put(null, entry, value);
    db.get(null, entry, out, LockMode.DEFAULT);
    log.info("[3] out={} <-- Updated to new value", toHexString(out));
  }

  private static String toHexString(DatabaseEntry entry) {
    return (entry != null && entry.getData() != null) ? ("0x" + toHexString(entry.getData())) :
        "<null>";
  }

  // app configuration
  public static final class AppModule extends AbstractModule {
    @Override
    protected void configure() {
      bind(HelloBdbApp.class);
    }
  }


  public static void main(String[] args) {
    final Injector injector = Guice.createInjector(new AppModule());
    final Runnable runnableApp = injector.getInstance(HelloBdbApp.class);
    runnableApp.run();
  }
}
