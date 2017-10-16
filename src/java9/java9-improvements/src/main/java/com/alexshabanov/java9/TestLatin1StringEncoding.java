package com.alexshabanov.java9;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Tests that string indeed stores chars as bytes.
 */
public final class TestLatin1StringEncoding {
  private static final int ITERATIONS = 1000;
  private static final int CHAR_LEN = 2000;

  public static void main(String[] args) {
    final List<String> strings = new ArrayList<>(ITERATIONS);
    final long total1 = Runtime.getRuntime().totalMemory();
    final long free1 = Runtime.getRuntime().freeMemory();
    final StrAllocator allocator = new StrAllocator();
    for (int i = 0; i < ITERATIONS; ++i) {
      strings.add(demo(allocator));
    }
    final long total2 = Runtime.getRuntime().totalMemory();
    final long free2 = Runtime.getRuntime().freeMemory();
    System.out.println("Total1=" + total1 + ", Free1=" + free1);
    System.out.println("Total2=" + total2 + ", Free2=" + free2);
    System.out.println("[End] strings.hc=" + strings.hashCode() + ", memDelta=" + (free1 - free2) +
        ", expected memDelta~=" + ITERATIONS * CHAR_LEN);
  }

  private static String demo(StrAllocator allocator) {
    allocator.fillChars();
    return allocator.allocString();
  }

  private static final class StrAllocator {
    final char[] chars = new char[CHAR_LEN];

    void fillChars() {
      final Random random = ThreadLocalRandom.current();
      for (int i = 0; i < chars.length; ++i) {
        chars[i] = (char) ('0' + random.nextInt(10));
      }
    }

    String allocString() {
      return new String(chars);
    }
  }
}
