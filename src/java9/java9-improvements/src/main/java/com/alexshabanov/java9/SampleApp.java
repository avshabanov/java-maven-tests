package com.alexshabanov.java9;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class SampleApp {

  public static void main(String[] args) {
    System.out.println("Hello, Java9");
    demoCollectionFactoryMethods();
    demoStreamApi();
    demoPrivateInterfaceMethods();
  }

  //
  // Private
  //

  private static void demoCollectionFactoryMethods() {
    Set<String> strs = Set.of("A", "B", "CD");
    List<BigDecimal> decimals = List.of(BigDecimal.ONE, BigDecimal.valueOf(103L, 1));
    System.out.println("[Collection Factory] strs=" + strs + ", decimals=" + decimals);
  }

  private static void demoStreamApi() {
    List<Integer> result = new ArrayList<>(List.of(0));
    IntStream.iterate(1, i -> i < 5, i -> i + 1).forEach(i -> result.set(0, result.get(0) * 10 + i));
    System.out.println("[Stream Api] Accumulated result=" + result);

    Stream<Integer> a = Optional.of(1).stream().map(i -> i + 100);
    System.out.println("[Stream Api] Optional stream=" + a.collect(Collectors.toList()));
  }

  private static void demoPrivateInterfaceMethods() {
    Animal a = () -> "Eagle";
    System.out.println("[Private Interface Methods] Animal description=" + a.getDescription());
  }

  interface Animal {

    String getName();

    default String getDescription() {
      return getDescriptionPrefix() + getName();
    }

    private String getDescriptionPrefix() {
      return getClass() + " -- ";
    }
  }
}
