package com.alexshabanov.fdbsample;

import com.apple.foundationdb.Database;
import com.apple.foundationdb.directory.DirectoryLayer;
import com.apple.foundationdb.directory.DirectorySubspace;
import com.apple.foundationdb.subspace.Subspace;
import com.apple.foundationdb.tuple.Tuple;
import com.google.common.collect.ImmutableList;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public final class SubstFdbApp {

  private static void directoryLayerDemo(Database db) {
    final DirectoryLayer dirLayer = new DirectoryLayer(
        new Subspace(Tuple.from("nodes")),
        new Subspace(Tuple.from("content"))
    );

    db.run((tx) -> {
      try {
        final DirectorySubspace demoDir = dirLayer.createOrOpen(tx, ImmutableList.of("etc", "demo")).get();
        tx.set(
            demoDir.get(Tuple.from("text.txt")).getKey(),
            Tuple.from("demo content").pack()
        );
      } catch (InterruptedException | ExecutionException e) {
        tx.cancel();
      }
      return null;
    });
  }

  public static void main(String[] args) {
    System.out.println("Subst FDB sample");
  }
}
