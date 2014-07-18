package com.squarespace.less.exec;

import java.io.IOException;
import java.nio.file.Path;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.squarespace.less.LessContext;
import com.squarespace.less.LessException;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.ErrorUtils;
import com.squarespace.less.core.LessUtils;
import com.squarespace.less.model.Stylesheet;


public class LessRoundTripTest extends LessSuiteBase {

  @Test
  public void testSuite() throws IOException {
    LessContext ctx = new LessContext();
    Path lessRoot = testSuiteRoot().resolve("less");
    int failures = 0;
    for (Path lessPath : LessUtils.getMatchingFiles(lessRoot, GLOB_LESS)) {
      String fileName = "less/" + lessPath.getFileName();
      String source = LessUtils.readFile(lessPath);
      String result = null;
      try {
        result = process(source, lessRoot);
      } catch (LessException e) {
        String msg = ErrorUtils.formatError(ctx, lessPath, e, 4);
        System.err.println(msg);

      } catch (RuntimeException e) {
        logFailure("RoundTrip Test", ++failures, "Error processing", fileName);
        e.printStackTrace();
      }
      if (result != null) {
        logFailure("RoundTrip Test", ++failures, "Differences detected in roundtrip output for ", fileName);
      }
    }
    if (failures > 0) {
      Assert.fail(failures + " tests failed.");
    }
  }

  private String process(String source, Path importRoot) throws LessException {
    Stylesheet sheetOne = parse(source, importRoot);
    String sourceOne = render(sheetOne);

    Stylesheet sheetTwo = parse(sourceOne, importRoot);
    String sourceTwo = render(sheetTwo);

    return diff(sourceOne, sourceTwo);
  }

  private String render(Stylesheet sheet) {
    Buffer buf = new Buffer(4, false);
    sheet.repr(buf);
    return buf.toString();
  }

}
