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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.squarespace.less.core.LessTestBase;


/**
 * Tests that the JailedFilesystemLessLoader never reads a file whose real
 * location is outside the jail root, even when a symlink inside the jail
 * points out of it.
 */
public class JailedFilesystemLessLoaderTest extends LessTestBase {

  private static final LessCompiler COMPILER = new LessCompiler();

  private static final String SECRET = "@secret: #123456; .outside { color: @secret; }";

  private final List<Path> tempDirs = new ArrayList<>();

  @AfterMethod
  public void cleanup() throws IOException {
    for (Path dir : tempDirs) {
      deleteTree(dir);
    }
    tempDirs.clear();
  }

  @Test
  public void testSymlinkFileEscapeBlocked() throws Exception {
    Path jail = tempDir("jail");
    Path outside = tempDir("outside");
    Path secret = outside.resolve("secret.less");
    Files.write(secret, SECRET.getBytes(StandardCharsets.UTF_8));
    Path leak = symlink(jail.resolve("leak.less"), secret);

    JailedFilesystemLessLoader loader = new JailedFilesystemLessLoader(jail);
    assertFalse(loader.exists(leak));
    try {
      loader.load(leak);
      fail("Expected load() of symlink escape to fail");
    } catch (LessException e) {
      // expected
    }

    // Compile must fail too; the outside file's content must never flow
    // into the output.
    assertCompileFails(loader, jail, "@import 'leak.less'; .main { color: @secret; }");
  }

  @Test
  public void testSymlinkDirectoryEscapeBlocked() throws Exception {
    Path jail = tempDir("jail");
    Path outside = tempDir("outside");
    Files.write(outside.resolve("secret.less"), SECRET.getBytes(StandardCharsets.UTF_8));
    symlink(jail.resolve("leakdir"), outside);

    JailedFilesystemLessLoader loader = new JailedFilesystemLessLoader(jail);
    assertCompileFails(loader, jail, "@import 'leakdir/secret.less'; .main { color: @secret; }");
  }

  @Test
  public void testBrokenSymlinkBlocked() throws Exception {
    Path jail = tempDir("jail");
    Path missing = tempDir("missing").resolve("gone.less");
    symlink(jail.resolve("dangling.less"), missing);

    JailedFilesystemLessLoader loader = new JailedFilesystemLessLoader(jail);
    assertFalse(loader.exists(jail.resolve("dangling.less")));
    try {
      loader.load(jail.resolve("dangling.less"));
      fail("Expected load() of a broken symlink to fail");
    } catch (LessException e) {
      // expected
    }
  }

  @Test
  public void testMissingFileBlocked() throws Exception {
    Path jail = tempDir("jail");
    Path missing = jail.resolve("nope.less");

    JailedFilesystemLessLoader loader = new JailedFilesystemLessLoader(jail);
    assertFalse(loader.exists(missing));
    try {
      loader.load(missing);
      fail("Expected load() of a missing file to fail");
    } catch (LessException e) {
      // expected
    }
  }

  @Test
  public void testLexicalEscapeBlocked() throws Exception {
    Path jail = tempDir("jail");
    Path outside = tempDir("outside");
    Files.write(outside.resolve("secret.less"), SECRET.getBytes(StandardCharsets.UTF_8));

    JailedFilesystemLessLoader loader = new JailedFilesystemLessLoader(jail);
    assertCompileFails(loader, jail, "@import '../outside/secret.less'; .main { color: @secret; }");

    // Absolute path outside the jail is rejected as well, and the
    // existence oracle does not leak for non-less or outside files.
    assertFalse(loader.exists(outside.resolve("secret.less")));
    assertFalse(loader.exists(outside.resolve("secret.css")));
  }

  @Test
  public void testJailRootBehindSymlink() throws Exception {
    Path realRoot = tempDir("root");
    Path linkDir = tempDir("links");
    Path rootLink = symlink(linkDir.resolve("rootlink"), realRoot);

    // The jail root itself is a symlink, so containment must compare real
    // paths on both sides: a normal file inside the real root still
    // imports through the symlinked root.
    Path ok = realRoot.resolve("ok.less");
    String source = "@color: #abc; .ok { color: @color; }";
    Files.write(ok, source.getBytes(StandardCharsets.UTF_8));

    JailedFilesystemLessLoader loader = new JailedFilesystemLessLoader(rootLink);
    Path importPath = rootLink.resolve("ok.less");
    assertTrue(loader.exists(importPath));
    assertEquals(loader.load(importPath), source);

    String result = compile(loader, rootLink, "@import 'ok.less'; .main { color: @color; }");
    assertTrue(result.contains(".ok{color:#abc}"), result);
    assertTrue(result.contains(".main{color:#abc}"), result);

    // Escape through the symlinked root is still blocked.
    Path outside = tempDir("outside");
    Files.write(outside.resolve("secret.less"), SECRET.getBytes(StandardCharsets.UTF_8));
    symlink(realRoot.resolve("leak.less"), outside.resolve("secret.less"));
    assertFalse(loader.exists(rootLink.resolve("leak.less")));
    assertCompileFails(loader, rootLink, "@import 'leak.less'; .main { color: @secret; }");
  }

  @Test
  public void testNormalImportWorks() throws Exception {
    Path jail = tempDir("jail");
    Path ok = jail.resolve("ok.less");
    String source = "@color: #abc; .ok { color: @color; }";
    Files.write(ok, source.getBytes(StandardCharsets.UTF_8));

    JailedFilesystemLessLoader loader = new JailedFilesystemLessLoader(jail);
    assertTrue(loader.exists(ok));
    // load() returns the file content, byte-identical to a direct read.
    assertEquals(loader.load(ok), new String(Files.readAllBytes(ok), StandardCharsets.UTF_8));

    String result = compile(loader, jail, "@import 'ok.less'; .main { color: @color; }");
    assertTrue(result.contains(".ok{color:#abc}"), result);
    assertTrue(result.contains(".main{color:#abc}"), result);
  }

  @Test
  public void testSymlinkInsideJailAllowed() throws Exception {
    Path jail = tempDir("jail");
    Files.write(jail.resolve("ok.less"),
        "@color: #abc; .ok { color: @color; }".getBytes(StandardCharsets.UTF_8));
    symlink(jail.resolve("alias.less"), jail.resolve("ok.less"));

    JailedFilesystemLessLoader loader = new JailedFilesystemLessLoader(jail);
    String result = compile(loader, jail, "@import 'alias.less'; .main { color: @color; }");

    assertTrue(result.contains(".ok{color:#abc}"), result);
  }

  @Test
  public void testRejectsNonLessExtension() throws Exception {
    Path jail = tempDir("jail");
    Files.write(jail.resolve("data.txt"), "x".getBytes(StandardCharsets.UTF_8));

    JailedFilesystemLessLoader loader = new JailedFilesystemLessLoader(jail);
    assertFalse(loader.exists(jail.resolve("data.txt")));
  }

  private void assertCompileFails(LessLoader loader, Path jail, String source) throws Exception {
    try {
      compile(loader, jail, source);
      fail("Expected compile to fail for import escaping the jail");
    } catch (LessException e) {
      assertTrue(e.getMessage().contains("File cannot be found"), e.getMessage());
    }
  }

  private String compile(LessLoader loader, Path jail, String source) throws LessException {
    LessOptions opts = new LessOptions();
    opts.compress(true);
    opts.tracing(false);
    opts.indent(4);
    opts.importOnce(true);
    opts.strict(false);
    opts.hideWarnings(false);
    LessContext ctx = new LessContext(opts, loader);
    ctx.setCompiler(COMPILER);
    // Default safeMode is false: recovery mode would drop a failed import
    // statement and let the compile succeed, masking an escaping import.
    return COMPILER.compile(source, ctx, jail, Paths.get("main.less"));
  }

  private Path tempDir(String name) throws IOException {
    Path dir = Files.createTempDirectory("jail-test-" + name);
    tempDirs.add(dir);
    return dir;
  }

  private Path symlink(Path link, Path target) throws IOException {
    try {
      Files.createSymbolicLink(link, target);
    } catch (UnsupportedOperationException | IOException e) {
      throw new SkipException("Cannot create symlinks on this platform: " + e.getMessage());
    }
    assertTrue(Files.isSymbolicLink(link), "fixture symlink missing: " + link);
    assertEquals(Files.readSymbolicLink(link), target);
    return link;
  }

  private static void deleteTree(Path root) throws IOException {
    if (root == null || !Files.exists(root)) {
      return;
    }
    try (java.util.stream.Stream<Path> stream = Files.walk(root)) {
      stream.sorted(java.util.Comparator.reverseOrder()).forEach(path -> {
        try {
          Files.deleteIfExists(path);
        } catch (IOException e) {
          // best effort cleanup
        }
      });
    }
  }

}
