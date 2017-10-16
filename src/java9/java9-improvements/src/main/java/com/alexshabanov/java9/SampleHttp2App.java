package com.alexshabanov.java9;
/*
import jdk.incubator.http.HttpClient;
import jdk.incubator.http.HttpRequest;
import jdk.incubator.http.HttpResponse;

import java.io.IOException;
import java.net.URI;

public class SampleHttp2App {

  public static void main(String[] args) {
    demoHttp2();
  }


  private static void demoHttp2() {
    final HttpClient client = HttpClient.newHttpClient();
    final HttpRequest request = HttpRequest.newBuilder(URI.create("http://www.google.com/robots.txt"))
        .header("User-Agent", "Java/HTTP2")
        .GET()
        .build();

    try {
      final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandler.asString());
      final String body = response.body();
      System.out.println("[HTTP2] Result=" + body.substring(0, Math.min(30, body.length())));
    } catch (IOException | InterruptedException e) {
      System.out.println("[HTTP2] Failed to process request, e.message=" + e.getMessage());
    }
  }
}
*/

public class SampleHttp2App {

  // TODO: enable HTTP2 module (and class above)

  public static void main(String[] args) {
    System.out.println("TODO: enable HTTP2 module");
  }
}