/**
 * Copyright, 2015, Squarespace, Inc.
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

package com.squarespace.less.parse;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

import com.squarespace.less.HashMapLessLoader;
import com.squarespace.less.LessCompiler;
import com.squarespace.less.LessContext;
import com.squarespace.less.LessException;
import com.squarespace.less.LessLoader;
import com.squarespace.less.LessOptions;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.model.Import;


public class ImporterTest extends LessTestBase {

  private static final LessCompiler COMPILER = new LessCompiler();

  @Test
  public void testResolver() throws LessException {
    Path rootPath = Paths.get("/rootpath/foo");
    LessContext context = newContext(rootPath, "one.less");

    Import imp = newImport(anon("one.less"));
    imp.rootPath(rootPath);
    Path actual = context.importer().resolvePath(imp);
    assertEquals(actual, rootPath.resolve("one.less"));

    imp = newImport(anon("two.less"));
    actual = context.importer().resolvePath(imp);
    assertEquals(actual, null);
  }

  @Test
  public void testImportOnce() throws LessException {
    Path rootPath = Paths.get("/rootpath/foo");
    LessContext context = newContext(rootPath, "one.less");
    Importer importer = context.importer();

    Import imp = newImport(anon("one.less"), null, true);
    imp.rootPath(rootPath);

    Path actual = importer.resolvePath(imp);
    assertEquals(actual, rootPath.resolve("one.less"));
    assertFalse(importer.shouldSuppressImport(actual));

    importer.recordImport(imp, actual);
    assertTrue(importer.shouldSuppressImport(actual));
  }

  @Test
  public void testGlobalImportOnce() throws LessException {
    LessOptions options = new LessOptions();
    options.importOnce(true);
    Path rootPath = Paths.get("/rootpath/foo");
    LessContext context = newContext(options, rootPath, "one.less");
    Importer importer = context.importer();

    Import imp = newImport(anon("one.less"));
    imp.rootPath(rootPath);

    Path actual = importer.resolvePath(imp);
    assertEquals(actual, rootPath.resolve("one.less"));
    assertFalse(importer.shouldSuppressImport(actual));

    importer.recordImport(imp, actual);
    assertTrue(importer.shouldSuppressImport(actual));
  }

  @Test
  public void testImportRootPath() throws LessException {
    Path rootPath = Paths.get("/rootpath/foo");
    LessContext context = newContext(rootPath, "one.less", "bar/two.less");
    Importer importer = context.importer();

    Import imp = newImport(anon("one.less"));
    imp.rootPath(rootPath);

    Path actual = importer.resolvePath(imp);
    assertEquals(actual, rootPath.resolve("one.less"));

    imp = newImport(anon("two.less"));
    imp.rootPath(rootPath);
    actual = importer.resolvePath(imp);
    assertEquals(actual, null);

    imp = newImport(anon("bar/two.less"));
    imp.rootPath(rootPath);
    actual = importer.resolvePath(imp);
    assertEquals(actual, rootPath.resolve("bar/two.less"));
  }

  @Test
  public void testGlobalImportPaths() throws LessException {
    Path rootPath = Paths.get("/rootpath");
    Path fooPath = rootPath.resolve("foo");
    Path barPath = rootPath.resolve("bar");
    LessOptions options = new LessOptions();
    options.importPaths(Arrays.asList(fooPath.toString(), barPath.toString()));

    LessContext context = newContext(options, rootPath, "foo/one.less", "bar/two.less");
    Importer importer = context.importer();

    Import imp = newImport(anon("one.less"));
    imp.rootPath(rootPath);
    Path path = importer.resolvePath(imp);
    assertEquals(path, fooPath.resolve("one.less"));

    imp = newImport(anon("two.less"));
    imp.rootPath(rootPath);
    path = importer.resolvePath(imp);
    assertEquals(path, barPath.resolve("two.less"));

    imp = newImport(quoted('"', false, anon("two.less")));
    imp.rootPath(rootPath);
    path = importer.resolvePath(imp);
    assertEquals(path, barPath.resolve("two.less"));

    imp = newImport(anon("bar/../foo/one.less"));
    imp.rootPath(rootPath);
    path = importer.resolvePath(imp);
    assertEquals(path, fooPath.resolve("one.less"));

    imp = newImport(anon("missing.less"));
    imp.rootPath(rootPath);
    path = importer.resolvePath(imp);
    assertEquals(path, null);
  }

  @Test
  public void testImporter() throws LessException {
    LessLoader loader = new HashMapLessLoader(buildMap());
    LessOptions opts = buildOptions();
    LessContext ctx = new LessContext(opts, loader);
    ctx.setFunctionTable(COMPILER.functionTable());
    String source = "@import 'base.less'; .ruleset { color: @color; font-size: @size; }";
    String result = COMPILER.compile(source, ctx, path("foo.less"));

    assertEquals(result, ".child{font-size:12px}.ruleset{color:#abc;font-size:12px}");
  }

  private static Path path(String path) {
    return Paths.get(path).toAbsolutePath().normalize();
  }

  private static Map<Path, String> buildMap() {
    Map<Path, String> map = new HashMap<>();
    map.put(path("base.less"), "@color: #abc; @import 'child.less';");
    map.put(path("child.less"), ".child { font-size: 12px; }\n@size: 12px;");
    return map;
  }

  private static LessOptions buildOptions() {
    LessOptions opts = new LessOptions();
    opts.compress(true);
    opts.tracing(false);
    opts.indent(4);
    opts.importOnce(true);
    opts.strict(false);
    opts.hideWarnings(false);
    return opts;
  }

  private static LessContext newContext(Path rootPath, String ... paths) {
    return newContext(null, rootPath, paths);
  }

  private static LessContext newContext(LessOptions options, Path rootPath, String ... paths) {
    LessLoader loader = new TestLoader(rootPath, paths);
    if (options == null) {
      options = new LessOptions();
    }
    return new LessContext(options, loader);
  }

  private static class TestLoader implements LessLoader {

    private final Set<Path> pathSet = new HashSet<>();

    public TestLoader(Path root, String ... paths) {
      for (String path : paths) {
        pathSet.add(root.resolve(path).toAbsolutePath().normalize());
      }
    }

    @Override
    public boolean exists(Path path) {
      return pathSet.contains(path);
    }

    @Override
    public String load(Path path) throws LessException {
      return "expected";
    }

    @Override
    public Path normalize(Path path) {
      return (path == null) ? null : path.toAbsolutePath().normalize();
    }

  }

}
