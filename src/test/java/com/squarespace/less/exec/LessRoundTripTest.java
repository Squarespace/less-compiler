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

import org.testng.Assert;
import org.testng.annotations.Test;

import com.squarespace.less.LessContext;
import com.squarespace.less.LessException;
import com.squarespace.less.core.ErrorUtils;
import com.squarespace.less.core.LessUtils;
import com.squarespace.less.model.Stylesheet;


/**
 * Ensures that a parsed LESS file that is repr()-ed back into LESS produces the
 * equivalent syntax tree and LESS output (round-tripping).
 */
public class LessRoundTripTest extends LessSuiteBase {

  @Test
  public void testSuite() throws IOException {
    LessContext ctx = new LessContext();
    Path lessRoot = testSuiteRoot().resolve("less");
    int failures = 0;
    for (Path lessPath : LessUtils.getMatchingFiles(lessRoot, GLOB_LESS)) {
      String fileName = "less/" + lessPath.getFileName();
      String source = LessUtils.readFile(lessPath);
      boolean result = false;
      try {
        result = process(source, lessRoot);
      } catch (LessException e) {
        String msg = ErrorUtils.formatError(ctx, lessPath, e, 4);
        System.err.println(msg);

      } catch (RuntimeException e) {
        logFailure("RoundTrip Test", ++failures, "Error processing", fileName);
        e.printStackTrace();
      }
      if (!result) {
        logFailure("RoundTrip Test", ++failures, "Differences detected in roundtrip output for ", fileName);
      }
    }
    if (failures > 0) {
      Assert.fail(failures + " tests failed.");
    }
  }

  private boolean process(String source, Path importRoot) throws LessException {
    Stylesheet original = parse(source, importRoot);

    String sourceOne = original.repr();
    Stylesheet sheetOne = parse(sourceOne, importRoot);

    String sourceTwo = sheetOne.repr();
    Stylesheet sheetTwo = parse(sourceTwo, importRoot);

    if (!sheetOne.equals(sheetTwo)) {
      System.err.println("sheets not equal");
      return false;
    }
    if (diff(sourceOne, sourceTwo) != null) {
      System.err.println("diff not equal");
      return false;
    }
    return true;
  }

}
