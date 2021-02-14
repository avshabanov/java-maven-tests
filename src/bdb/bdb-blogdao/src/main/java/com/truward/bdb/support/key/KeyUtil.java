package com.truward.bdb.support.key;

import com.google.common.io.BaseEncoding;
import com.google.protobuf.ByteString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Utility class for operating with BDB keys.
 *
 * @author Alexander Shabanov
 */
public final class KeyUtil {
  public static final int DEFAULT_KEY_SIZE = 16;
  public static final int MAX_STRINGIFIED_BYTES_SIZE = 32;

  @Nonnull
  public static ByteString randomKeyWithPrefix(@Nonnull ByteString prefix, @Nonnull Random random, int keySize) {
    if (keySize <= 0) {
      throw new IllegalArgumentException("keySize is not greater than zero");
    }

    final byte[] key = new byte[prefix.size() + keySize];
    random.nextBytes(key);

    if (!prefix.isEmpty()) {
      System.arraycopy(key, 0, key, prefix.size(), keySize);
      prefix.copyTo(key, 0);
    }

    return ByteString.copyFrom(key);
  }

  @Nonnull
  public static ByteString randomKey(int keySize) {
    return randomKeyWithPrefix(ByteString.EMPTY, ThreadLocalRandom.current(), keySize);
  }

  @Nonnull
  public static ByteString randomKey() {
    return randomKey(DEFAULT_KEY_SIZE);
  }

  @Nonnull
  public static String toHexString(@Nullable ByteString bytes) {
    if (bytes == null) {
      return "<null>";
    }
    if (bytes.isEmpty()) {
      return "<empty>";
    }

    final byte[] arr = bytes.substring(0, Math.min(MAX_STRINGIFIED_BYTES_SIZE, bytes.size())).toByteArray();

    @SuppressWarnings("StringBufferReplaceableByString")
    final StringBuilder result = new StringBuilder(2 + arr.length * 2);
    result.append('<').append(BaseEncoding.base16().lowerCase().encode(arr)).append('>');
    return result.toString();
  }

  private KeyUtil() {} // hidden
}
