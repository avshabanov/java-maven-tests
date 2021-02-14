package com.alexshabanov.fdbsample;

import com.apple.foundationdb.Database;
import com.apple.foundationdb.FDB;
import com.apple.foundationdb.Transaction;
import com.apple.foundationdb.record.provider.foundationdb.FDBRecordStore;
import com.apple.foundationdb.record.provider.foundationdb.keyspace.DirectoryLayerDirectory;
import com.apple.foundationdb.record.provider.foundationdb.keyspace.KeySpace;
import com.apple.foundationdb.record.provider.foundationdb.keyspace.KeySpaceDirectory;
import com.apple.foundationdb.record.provider.foundationdb.keyspace.KeySpacePath;
import com.apple.foundationdb.tuple.Tuple;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Bytes;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HelloFdbApp implements Runnable {

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

  private void shallowRecordLayerDemo() {
    // NOTE: taken from
    // https://github.com/FoundationDB/fdb-record-layer/blob/main/examples/src/main/java/com/apple/foundationdb/record/sample/Main.java

    final KeySpace keySpace = new KeySpace(
        new DirectoryLayerDirectory("application")
            .addSubdirectory(new KeySpaceDirectory("environment", KeySpaceDirectory.KeyType.STRING))
    );

    // Create a path for the "record-layer-sample" application's demo environment.
    // Clear all existing data and then return the subspace associated with the key space path.
    final KeySpacePath path = keySpace.path("application", "record-layer-sample")
        .add("environment", "demo");

    log.info("p={}", path.toString(Tuple.from("test")));
  }

  private void runDb() {
    shallowRecordLayerDemo();

    final FDB fdb = FDB.selectAPIVersion(620);
    log.info("[0] start; fdbVersion={}", fdb.getAPIVersion());

    final Database db = fdb.open();
    db.run((Transaction tr) -> {
      tr.set(Tuple.from("hello").pack(), Tuple.from("world").pack());
      tr.set(Bytes.toArray(ImmutableList.of(1, 2, 3)), new byte[]{4, 5});
      return null;
    });
  }

  // app configuration
  public static final class AppModule extends AbstractModule {
    @Override
    protected void configure() {
      bind(Runnable.class).to(HelloFdbApp.class);
    }
  }

  public static void main(String[] args) {
    final Injector injector = Guice.createInjector(new AppModule());
    injector.getInstance(HelloFdbApp.class).run();
  }
}
