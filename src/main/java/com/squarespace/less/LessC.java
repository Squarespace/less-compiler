package com.squarespace.less;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * Execute the compiler from the command-line.
 */
public class LessC {

  public static void main(String[] args) {
    LessCImpl impl = new LessCImpl(System.out, System.err);
    if (!impl.initialize(args)) {
      System.exit(1);
    }

    if (impl.waitForUser()) {
      waitForUser();
    }

    int code = impl.run();

    if (impl.waitForUser()) {
      waitForUser();
    }
    System.exit(code);
  }

  /**
   * Wait for a newline at the prompt before executing / exiting. Assists with
   * debugging / profiling at the command line.
   */
  private static void waitForUser() {
    BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
    try {
      buffer.readLine();
    } catch (IOException e) {
      System.exit(1);
    }
  }

}
