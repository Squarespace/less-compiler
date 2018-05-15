/**
 * Copyright (c) 2018 SQUARESPACE, Inc.
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

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.squarespace.less.AstTestCaseParser.AstTestCase;

public class AstTestCaseRunner {

  private final Set<String> filesSeen = new HashSet<>();
  private final Class<?> resourceClass;
  private final LessMessages messages = new LessMessages(2, 6);


  public AstTestCaseRunner(Class<?> cls) {
    this.resourceClass = cls;
  }

  public void run(String... paths) {
    Map<String, Throwable> errors = new HashMap<>();
    for (String path : paths) {
      if (filesSeen.contains(path)) {
        throw new AssertionError("Already processed file " + path);
      }
      runOne(path, errors);
      filesSeen.add(path);
    }
    assertPass(errors);
  }


  private void runOne(String path, Map<String, Throwable> errors) {
    try {
      AstTestCase testCase = AstTestCaseParser.load(resourceClass, path);
      testCase.execute();

    } catch (AssertionError | IOException | LessException e) {
      errors.put(path, e);
    }
  }

  private void assertPass(Map<String, Throwable> errors) {
    if (!errors.isEmpty()) {
      for (Map.Entry<String, Throwable> entry : errors.entrySet()) {
        System.err.println("Case: " + entry.getKey());
        Throwable value = entry.getValue();
        if (value instanceof LessException) {
          System.err.println(messages.formatError((LessException)value));
        }
        value.printStackTrace();

      }
      throw new AssertionError("Failed!");
    }
  }

}

// private final Set<String> filesSeen = new HashSet<>();
//
// private final TestCaseParser parser = new TestCaseParser();
//
// private final Compiler compiler;
//
// private final Class<?> resourceClass;

// public TestSuiteRunner(Compiler compiler, Class<?> resourceClass) {
// this.compiler = compiler;
// this.resourceClass = resourceClass;
// }

