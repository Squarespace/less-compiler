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
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
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

  private static final Path suiteRootDir = LessSuiteBase.testSuiteRoot();

  private File tempFile;

  private ByteArrayOutputStream standardOut;

  private ByteArrayOutputStream standardErr;

  private PipedInputStream standardIn;

  private PrintStream savedOut;

  private PrintStream savedErr;

  private InputStream savedIn;

  private SecurityManager savedSecurityManager;

  @BeforeMethod
  private void setUp() {
    // Trap output for the duration of a single test.
    standardOut = new ByteArrayOutputStream();
    savedOut = System.out;
    System.setOut(new PrintStream(standardOut));

    standardErr = new ByteArrayOutputStream();
    savedErr = System.err;
    System.setErr(new PrintStream(standardErr));

    standardIn = new PipedInputStream();
    savedIn = System.in;
    System.setIn(standardIn);

    // Catch System.exit and report exit code.
    savedSecurityManager = System.getSecurityManager();
    System.setSecurityManager(new NoExitSecurityManager());
  }

  @AfterMethod
  private void tearDown() {
    restoreStreams();
  }

  private void restoreStreams() {
    // Revert stream / exit intercepts.
    System.setSecurityManager(savedSecurityManager);
    System.setOut(savedOut);
    System.setErr(savedErr);
    System.setIn(savedIn);

    // Clean up temp file / directory if its been used.
    if (tempFile != null) {
      tempFile.deleteOnExit();
      tempFile = null;
    }
  }

  @Test
  public void testCompileToStdout() throws LessException, IOException {
    String lessPath = suiteRootDir.resolve("less/directive.less").toString();
    Path cssPath = suiteRootDir.resolve("css/directive.css");
    String expected = LessUtils.readFile(cssPath);
    compile(lessPath);
    assertEquals(standardOut.toString(), expected);
  }

  @Test
  public void testCompileFromStdin() throws LessException, IOException {
    String source = ".ruleset { .child { color: red } }";
    String expected = ".ruleset .child{color:red}";
    compileStdin(source, "-", "--compress");
    assertEquals(standardOut.toString(), expected);
  }

  @Test
  public void testCompileToFile() throws LessException, IOException {
    Path lessPath = suiteRootDir.resolve("less/directive.less");
    Path expectedPath = suiteRootDir.resolve("css/directive.css");
    tempFile = Files.createTempFile("lessc-unit-test", ".css").toFile();
    compile(lessPath.toString(), tempFile.toString());
    assertFilesEqual(expectedPath, tempFile.toPath());
  }

  @Test
  public void testDebugParse() throws LessException, IOException {
    Path lessPath = suiteRootDir.resolve("less/directive.less");
    compile("--debug", "PARSETREE", lessPath.toString());
    assertTrue(standardOut.toString().contains("BLOCK_DIRECTIVE"));
  }

  @Test
  public void testDebugCanonical() throws LessException, IOException {
    Path lessPath = suiteRootDir.resolve("less/directive.less");
    compile("--debug", "CANONICALIZE", lessPath.toString());
    assertTrue(standardOut.toString().contains(".ruleset-font-face {"));
  }

  @Test
  public void testBatchCompile() throws LessException, IOException {
    Path lessPath = suiteRootDir.resolve("less");
    tempFile = Files.createTempDirectory("lessc-unit-test").toFile();
    compile("--batch", lessPath.toString(), tempFile.toString());

    // Compare contents of all files in expected and actual directories.
    Path cssPath = suiteRootDir.resolve("css");
    String pattern = "glob:*.css";
    for (Path expectedPath : LessUtils.getMatchingFiles(cssPath, pattern)) {
      Path actualPath = tempFile.toPath().resolve(expectedPath.getFileName());
      assertFilesEqual(expectedPath, actualPath);
    }
  }

  @Test
  public void testVersion() throws LessException {
    try {
      compile("-v");
      fail("Expected call to System.exit()");

    } catch (ExitException e) {
      assertEquals(e.status, 0);
      assertTrue(standardOut.toString().contains("lessc version"));
    }

    try {
      compile("-h");
      fail("Expected 'version' to call System.exit()");

    } catch (ExitException e) {
      assertEquals(e.status, 1);
      assertTrue(standardOut.toString().contains("usage: lessc"));
    }
  }

  private void assertFilesEqual(Path expectedPath, Path actualPath) throws IOException {
    String srcData = LessUtils.readFile(expectedPath);
    String dstData = LessUtils.readFile(actualPath);
    assertEquals(srcData, dstData, "Comparison for file " + expectedPath.getFileName() + " failed.");
  }

  private int compile(String ... args) {
    return LessC.process(args, new PrintStream(standardOut), new PrintStream(standardErr), System.in);
  }

  private int compileStdin(String source, String ... args) throws IOException {
    PipedOutputStream stream = new PipedOutputStream();
    standardIn.connect(stream);
    stream.write(source.getBytes("UTF-8"));
    stream.close();
    return LessC.process(args, new PrintStream(standardOut), new PrintStream(standardErr), standardIn);
  }

}
