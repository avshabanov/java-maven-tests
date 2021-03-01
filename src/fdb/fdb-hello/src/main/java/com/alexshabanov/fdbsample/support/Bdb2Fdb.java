package com.alexshabanov.fdbsample.support;


import com.apple.foundationdb.DatabaseOptions;
import com.apple.foundationdb.FDBException;
import com.apple.foundationdb.KeySelector;
import com.apple.foundationdb.KeyValue;
import com.apple.foundationdb.MutationType;
import com.apple.foundationdb.Range;
import com.apple.foundationdb.ReadTransaction;
import com.apple.foundationdb.StreamingMode;
import com.apple.foundationdb.TransactionOptions;
import com.apple.foundationdb.async.AsyncIterable;
import com.apple.foundationdb.tuple.ByteArrayUtil;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;
import com.sleepycat.je.TransactionConfig;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * BDB-to-FDB adapter
 */
public final class Bdb2Fdb {

  @ParametersAreNonnullByDefault
  class DBTx implements com.apple.foundationdb.Transaction {
    final DBWrapper wrapper;
    final Transaction bdbTx;
    final Executor executor;

    public DBTx(DBWrapper wrapper, Transaction bdbTx, Executor executor) {
      this.wrapper = wrapper;
      this.bdbTx = bdbTx;
      this.executor = executor;
    }

    @Override
    public void addReadConflictRange(byte[] keyBegin, byte[] keyEnd) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void addReadConflictKey(byte[] key) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void addWriteConflictRange(byte[] keyBegin, byte[] keyEnd) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void addWriteConflictKey(byte[] key) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void set(byte[] key, byte[] value) {
      this.wrapper.bdb.put(this.bdbTx, new DatabaseEntry(key), new DatabaseEntry(value));
    }

    @Override
    public void clear(byte[] key) {
      this.wrapper.bdb.delete(this.bdbTx, new DatabaseEntry(key));
    }

    @Override
    public void clear(byte[] beginKey, byte[] endKey) {
      final CursorConfig cursorConfig = CursorConfig.DEFAULT;
      final Cursor cursor = this.wrapper.bdb.openCursor(this.bdbTx, cursorConfig);
      final DatabaseEntry key = new DatabaseEntry();
      final DatabaseEntry value = new DatabaseEntry();
      final LockMode lockMode = LockMode.DEFAULT;
      OperationStatus st = cursor.getFirst(key, value, lockMode);
      while (st == OperationStatus.KEYEXIST) {
        int compareResult = ByteArrayUtil.compareUnsigned(key.getData(), beginKey);
        if (compareResult >= 0) {
          compareResult = ByteArrayUtil.compareUnsigned(key.getData(), endKey);
          if (compareResult >= 0) {
            break;
          }
        }
        // cursor position matches source criteria
        cursor.delete();
        st = cursor.getNext(key, value, lockMode);
      }
    }

    @Override
    public void clear(Range range) {
      clear(range.begin, range.end);
    }

    @Override
    public void clearRangeStartsWith(byte[] prefix) {

    }

    @Override
    public void mutate(MutationType optype, byte[] key, byte[] param) {

    }

    @Override
    public CompletableFuture<Void> commit() {
      return null;
    }

    @Override
    public Long getCommittedVersion() {
      return null;
    }

    @Override
    public CompletableFuture<byte[]> getVersionstamp() {
      return null;
    }

    @Override
    public CompletableFuture<Long> getApproximateSize() {
      return null;
    }

    @Override
    public CompletableFuture<com.apple.foundationdb.Transaction> onError(Throwable e) {

      return null;
    }

    @Override
    public void cancel() {
      bdbTx.abort();
    }

    @Override
    public CompletableFuture<Void> watch(byte[] key) throws FDBException {
      throw new UnsupportedOperationException();
    }

    @Override
    public com.apple.foundationdb.Database getDatabase() {
      return this.wrapper;
    }

    @Override
    public <T> T run(Function<? super com.apple.foundationdb.Transaction, T> retryable) {
      return retryable.apply(this);
    }

    @Override
    public <T> CompletableFuture<T> runAsync(Function<? super com.apple.foundationdb.Transaction, ? extends CompletableFuture<T>> retryable) {
      final CompletableFuture<T> result = new CompletableFuture<>();
      this.executor.execute(() -> {
        @SuppressWarnings("unchecked") final T value = (T) retryable.apply(this);
        result.complete(value);
      });

      return result;
    }

    @Override
    public void close() {
      this.bdbTx.commit();
    }

    @Override
    public boolean isSnapshot() {
      return false;
    }

    @Override
    public ReadTransaction snapshot() {
      throw new UnsupportedOperationException();
    }

    @Override
    public CompletableFuture<Long> getReadVersion() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setReadVersion(long version) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean addReadConflictRangeIfNotSnapshot(byte[] keyBegin, byte[] keyEnd) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean addReadConflictKeyIfNotSnapshot(byte[] key) {
      throw new UnsupportedOperationException();
    }

    @Override
    public CompletableFuture<byte[]> get(byte[] key) {
      throw new UnsupportedOperationException();
    }

    @Override
    public CompletableFuture<byte[]> getKey(KeySelector selector) {
      throw new UnsupportedOperationException();
    }

    @Override
    public AsyncIterable<KeyValue> getRange(KeySelector begin, KeySelector end) {
      throw new UnsupportedOperationException();
    }

    @Override
    public AsyncIterable<KeyValue> getRange(KeySelector begin, KeySelector end, int limit) {
      throw new UnsupportedOperationException();
    }

    @Override
    public AsyncIterable<KeyValue> getRange(KeySelector begin, KeySelector end, int limit, boolean reverse) {
      throw new UnsupportedOperationException();
    }

    @Override
    public AsyncIterable<KeyValue> getRange(KeySelector begin, KeySelector end, int limit, boolean reverse, StreamingMode mode) {
      throw new UnsupportedOperationException();
    }

    @Override
    public AsyncIterable<KeyValue> getRange(byte[] begin, byte[] end) {
      throw new UnsupportedOperationException();
    }

    @Override
    public AsyncIterable<KeyValue> getRange(byte[] begin, byte[] end, int limit) {
      throw new UnsupportedOperationException();
    }

    @Override
    public AsyncIterable<KeyValue> getRange(byte[] begin, byte[] end, int limit, boolean reverse) {
      throw new UnsupportedOperationException();
    }

    @Override
    public AsyncIterable<KeyValue> getRange(byte[] begin, byte[] end, int limit, boolean reverse, StreamingMode mode) {
      throw new UnsupportedOperationException();
    }

    @Override
    public AsyncIterable<KeyValue> getRange(Range range) {
      throw new UnsupportedOperationException();
    }

    @Override
    public AsyncIterable<KeyValue> getRange(Range range, int limit) {
      throw new UnsupportedOperationException();
    }

    @Override
    public AsyncIterable<KeyValue> getRange(Range range, int limit, boolean reverse) {
      throw new UnsupportedOperationException();
    }

    @Override
    public AsyncIterable<KeyValue> getRange(Range range, int limit, boolean reverse, StreamingMode mode) {
      throw new UnsupportedOperationException();
    }

    @Override
    public TransactionOptions options() {
      throw new UnsupportedOperationException();
    }

    @Override
    public <T> T read(Function<? super ReadTransaction, T> retryable) {
      throw new UnsupportedOperationException();
    }

    @Override
    public <T> CompletableFuture<T> readAsync(Function<? super ReadTransaction, ? extends CompletableFuture<T>> retryable) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Executor getExecutor() {
      return this.executor;
    }
  }

  class DBWrapper implements com.apple.foundationdb.Database {
    private static final int DEFAULT_THREAD_COUNT = 10;

    private final DatabaseOptions fakeOptions = new DatabaseOptions((code, parameter) -> {/* do nothing */});
    private final Environment env;
    private final Database bdb;
    private final ExecutorService execSvc;

    public DBWrapper(@Nonnull Environment env, @Nonnull Database bdb) {
      this.env = env;
      this.bdb = bdb;
      this.execSvc = Executors.newFixedThreadPool(DEFAULT_THREAD_COUNT);
    }

    @Override
    public com.apple.foundationdb.Transaction createTransaction(Executor e) {
      final Transaction tx = this.env.beginTransaction(null, TransactionConfig.DEFAULT);
      return new DBTx(this, tx, e);
    }

    @Override
    public DatabaseOptions options() {
      return fakeOptions;
    }

    @Override
    public <T> T read(Function<? super com.apple.foundationdb.ReadTransaction, T> retryable, Executor e) {
      return null;
    }

    @Override
    public <T> CompletableFuture<T> readAsync(Function<? super com.apple.foundationdb.ReadTransaction, ? extends CompletableFuture<T>> retryable, Executor e) {
      return null;
    }

    @Override
    public <T> T run(Function<? super com.apple.foundationdb.Transaction, T> retryable, Executor e) {
      com.apple.foundationdb.Transaction t = this.createTransaction(e);
      try {
        while (true) {
          try {
            T returnVal = retryable.apply(t);
            t.commit().join();
            return returnVal;
          } catch (RuntimeException err) {
            t = t.onError(err).join();
          }
        }
      } finally {
        t.close();
      }
    }

    @Override
    public <T> CompletableFuture<T> runAsync(Function<? super com.apple.foundationdb.Transaction, ? extends CompletableFuture<T>> retryable, Executor e) {
      return null;
    }

    @Override
    public void close() {
      bdb.close();
    }

    @Override
    public Executor getExecutor() {
      return execSvc;
    }
  }


  void take(Database bdb) {

  }
}
