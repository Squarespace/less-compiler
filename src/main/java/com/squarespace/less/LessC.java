/**
 * Copyright (c) 2014 SQUARESPACE, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
