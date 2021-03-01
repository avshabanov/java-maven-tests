package com.alexshabanov.bdbsample;

import com.google.common.io.BaseEncoding;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public final class HelloBdbApp implements Runnable {
  private static final boolean USE_SHM = Boolean.parseBoolean(System.getenv("USE_SHM"));

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

  private void demo(Database db) {
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

  private void runDb() throws Exception {
    final File envHome;
    if (USE_SHM) {
      // use shared memory
      final List<Path> p = Files.list(Paths.get("/dev/shm")).collect(Collectors.toList());
      log.info("shm contents={}", p);
      envHome = new File("/dev/shm/tempbdb");
      if (!envHome.mkdir()) {
        throw new IllegalStateException("Unable to create shm directory to host BDB files");
      }
    } else {
      // use plain old temp dir
      envHome = Files.createTempDirectory("Test-BDB-").toFile();
    }

    log.info("Use env home={}", envHome.getAbsolutePath());

    final EnvironmentConfig environmentConfig = new EnvironmentConfig();
    environmentConfig.setAllowCreate(true);
    environmentConfig.setTransactional(true);

    final Environment env = new Environment(envHome, environmentConfig);
    final DatabaseConfig dbConfig = new DatabaseConfig();
    dbConfig.setTransactional(true);
    dbConfig.setAllowCreate(true);
    dbConfig.setSortedDuplicates(false);

    try (Database db = env.openDatabase(null, "mydatabase", dbConfig)) {
      demo(db);
    } finally {
      // delete envHome recursively
      //noinspection ResultOfMethodCallIgnored
      Files.walk(envHome.toPath()).map(Path::toFile).sorted((o1, o2) -> -o1.compareTo(o2)).forEach(File::delete);
    }
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
