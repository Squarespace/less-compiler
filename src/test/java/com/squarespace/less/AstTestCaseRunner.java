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

