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

package com.squarespace.less.cli;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.squarespace.less.LessException;
import com.squarespace.less.core.LessUtils;
import com.squarespace.less.exec.LessSuiteBase;


public class LessCTest {

  private File tempFile;

  private ByteArrayOutputStream out;

  private ByteArrayOutputStream err;

  private PrintStream savedOut;

  private PrintStream savedErr;

  private SecurityManager savedSecurityManager;

  @BeforeMethod
  private void setUp() {
    // Trap output for the duration of a single test.
    out = new ByteArrayOutputStream();
    savedOut = System.out;
    System.setOut(new PrintStream(out));

    err = new ByteArrayOutputStream();
    savedErr = System.err;
    System.setErr(new PrintStream(err));

    // Catch System.exit and report exit code.
    savedSecurityManager = System.getSecurityManager();
    System.setSecurityManager(new NoExitSecurityManager());
  }

  @AfterMethod
  private void tearDown() {
    // Revert stream / exit intercepts.
    System.setSecurityManager(savedSecurityManager);
    System.setOut(savedOut);
    System.setErr(savedErr);

    // Clean up temp file / directory if its been used.
    if (tempFile != null) {
      tempFile.deleteOnExit();
      tempFile = null;
    }
  }

  @Test
  public void testCompileToStdout() throws LessException, IOException {
    Path root = LessSuiteBase.testSuiteRoot();
    String lessPath = root.resolve("less/directive.less").toString();
    Path cssPath = root.resolve("css/directive.css");
    String expected = LessUtils.readFile(cssPath);
    compile(lessPath);
    assertEquals(out.toString(), expected);
  }

  @Test
  public void testBatchCompile() throws LessException, IOException {
    Path root = LessSuiteBase.testSuiteRoot();
    Path lessPath = root.resolve("less");
    tempFile = Files.createTempDirectory("lessc-unit-test").toFile();
    compile("--batch", lessPath.toString(), tempFile.toString());

    // Compare contents of expected and actual directories.
    Path cssPath = root.resolve("css");
    String pattern = "glob:*.css";
    for (Path srcPath : LessUtils.getMatchingFiles(cssPath, pattern)) {
      Path fileName = srcPath.getFileName();
      Path dstPath = tempFile.toPath().resolve(fileName);
      String srcData = LessUtils.readFile(srcPath);
      String dstData = LessUtils.readFile(dstPath);
      assertEquals(srcData, dstData, "Comparison for file " + fileName + " failed.");
    }
  }

  @Test
  public void testVersion() throws LessException {
    try {
      compile("-v");
      fail("Expected call to System.exit()");

    } catch (ExitException e) {
      assertEquals(e.status, 0);
      assertTrue(out.toString().contains("lessc version"));
    }

    try {
      compile("-h");
      fail("Expected 'version' to call System.exit()");

    } catch (ExitException e) {
      assertEquals(e.status, 1);
      assertTrue(out.toString().contains("usage: lessc"));
    }
  }

  private int compile(String ... args) {
    return LessC.process(args, new PrintStream(out), new PrintStream(err));
  }

}
