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

package com.squarespace.less.exec;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.squarespace.less.ExecuteErrorType;
import com.squarespace.less.LessContext;
import com.squarespace.less.LessErrorType;
import com.squarespace.less.LessException;
import com.squarespace.less.SyntaxErrorType;
import com.squarespace.less.core.ErrorUtils;
import com.squarespace.less.core.LessUtils;


/**
 * Runs the on-disk test suite.
 */
public class LessSuiteTest extends LessSuiteBase {

  private static final boolean VERBOSE = true;

  @Test
  public void testSuite() throws IOException {
    Path rootPath = testSuiteRoot();
    Path lessRoot = rootPath.resolve("less");
    Path cssRoot = rootPath.resolve("css");
    int failures = 0;
    for (Path lessPath : LessUtils.getMatchingFiles(lessRoot, GLOB_LESS)) {
      String fileName = "less/" + lessPath.getFileName();


      if (VERBOSE) {
        System.err.println("Processing: " + fileName);
      }

      // Read and compile the .less source
      String source = LessUtils.readFile(lessPath);
      String lessCompiled = null;
      try {
        lessCompiled = compile(source, lessRoot);
      } catch (LessException | RuntimeException e) {
        logFailure("Test Suite", ++failures, "Error compiling", fileName);
        e.printStackTrace();
        continue;
      }

      // Compare with expected CSS result.
      String[] parts = lessPath.getFileName().toString().split("\\.(?=[^\\.]+$)");
      Path cssPath = cssRoot.resolve(parts[0] + ".css").normalize();
      String cssData = LessUtils.readFile(cssPath);

      String result = diff(cssData, lessCompiled);
      if (result != null) {
        logFailure("Test Suite", ++failures, "Differences detected in compiled output for ", fileName, "\n", result);
      }
    }

    if (failures > 0) {
      Assert.fail(failures + " tests failed.");
    }
  }

  @Test
  public void testCanonical() throws IOException {
    Path rootPath = testSuiteRoot();
    Path lessRoot = rootPath.resolve("less");
    Path cssRoot = rootPath.resolve("canon");
    int failures = 0;
    for (Path lessPath : LessUtils.getMatchingFiles(lessRoot, GLOB_LESS)) {
      String fileName = "less/" + lessPath.getFileName();


      if (VERBOSE) {
        System.err.println("Processing: " + fileName);
      }

      // Read and compile the .less source
      String source = LessUtils.readFile(lessPath);
      String lessCanonical = null;
      try {
        lessCanonical = canonicalize(source, lessRoot, 2);
      } catch (LessException | RuntimeException e) {
        logFailure("Test Suite", ++failures, "Error compiling", fileName);
        e.printStackTrace();
        continue;
      }

      // Compare with expected CSS result.
      Path expectedPath = cssRoot.resolve(lessPath.getFileName()).normalize();
      String expected = LessUtils.readFile(expectedPath);

      String result = diff(expected, lessCanonical);
      if (result != null) {
        logFailure("Test Suite", ++failures, "Differences detected in compiled output for ", fileName, "\n", result);
      }
    }

    if (failures > 0) {
      Assert.fail(failures + " tests failed.");
    }

  }

  @Test
  public void testErrorSuite() throws IOException {
    Path rootPath = testSuiteRoot();
    Path errorRoot = rootPath.resolve("error");
    LessContext ctx = new LessContext();
    int failures = 0;
    for (Path lessPath : LessUtils.getMatchingFiles(errorRoot, GLOB_LESS)) {
      String source = LessUtils.readFile(lessPath);
      List<ErrorCase> errorCases = parseErrorCases(source);
      for (ErrorCase errorCase : errorCases) {
        try {
          compile(errorCase.source, errorRoot);
          logFailure("Error Suite", ++failures, "Expected a LessException for error test case '"
              + errorCase.failMessage + "' processing:\n", errorCase.source);

        } catch (LessException e) {
          // Force generation of the error message, to cover that code
          if (VERBOSE) {
            String msg = ErrorUtils.formatError(ctx, lessPath, e, 4);
            System.err.println("\n==============================================\nIgnore the following error as it is expected:\n" + msg);
          }
          if (!errorCase.errorType.equals(e.primaryError().type())) {
            logFailure("Error Suite", ++failures, "Expected ", errorCase.errorType, " found ", e.primaryError().type(),
                " processing error test case '" + errorCase.failMessage + "'");
          }
        } catch (RuntimeException e) {
          logFailure("Error Suite", ++failures, "Unexpected runtime exception thrown processing error test case '"
              + errorCase.failMessage + "'");
          e.printStackTrace();
        }
      }
    }
    if (failures > 0) {

      Assert.fail(failures + " tests failed.");
    }
  }

  /**
   * Parse the error cases from the source, converting them into a list.
   */
  private List<ErrorCase> parseErrorCases(String source) {
    List<ErrorCase> errorCases = new ArrayList<>();
    String[] lines = source.split("\n");
    int size = lines.length;
    for (int i = 0; i < size; i++) {
      if (lines[i].startsWith("//:")) {
        String[] parts = lines[i].substring(3).split("\\s+", 2);
        if (parts.length != 2) {
          throw new InvalidTestException("Bad test case definition: " + lines[i]);
        }

        // Read the test case's source
        LessErrorType errorType = resolveErrorType(parts[0]);
        StringBuilder buf = new StringBuilder();
        i++;
        while (i < size) {
          if (lines[i].startsWith("//:")) {
            i--;
            break;
          }
          buf.append(lines[i]).append('\n');
          i++;
        }
        errorCases.add(new ErrorCase(errorType, parts[1], buf.toString()));
      }
    }
    return errorCases;
  }

  private LessErrorType resolveErrorType(String name) {
    int index = name.indexOf('.');
    if (index == -1) {
      throw new InvalidTestException("The ErrorType name must be of the form CLASS.MEMBER");
    }
    String cls = name.substring(0, index);
    String member = name.substring(index + 1);
    switch (cls) {
      case "ExecuteErrorType":
        return ExecuteErrorType.valueOf(member);
      case "SyntaxErrorType":
        return SyntaxErrorType.valueOf(member);
      default:
        throw new InvalidTestException("Unknown ErrorType-derived class: " + cls);
    }
  }

  private static class ErrorCase {

    private LessErrorType errorType;

    private String failMessage;

    private String source;

    ErrorCase(LessErrorType errorType, String failMessage, String source) {
      this.errorType = errorType;
      this.failMessage = failMessage;
      this.source = source;
    }

  }

}
