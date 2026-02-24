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
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessTestBase;


public class LessImporterTest extends LessTestBase {

  private static final LessCompiler COMPILER = new LessCompiler();

  @Test
  public void testImporter() throws LessException {
    LessLoader loader = new HashMapLessLoader(buildMap());
    LessOptions opts = buildOptions();
    LessContext ctx = new LessContext(opts, loader);
    ctx.setCompiler(COMPILER);
    String source = "@import 'base.less'; .ruleset { color: @color; font-size: @size; }";
    String result = COMPILER.compile(source, ctx, Paths.get("."), null, true);

    assertEquals(result, ".child{font-size:12px}.ruleset{color:#abc;font-size:12px}");
  }

  @Test
  public void testImporterRecursionLimit() throws LessException {
    int imports = 100;
    Map<Path, String> map = new HashMap<>();
    for (int i = 0; i < imports; i++) {
      map.put(path(i + ".less"), "@import '" + (i + 1) + ".less';\n");
    }
    map.put(path(imports + ".less"), ".parent { color: red; }\n");

    int recursionLimit = 90;
    LessLoader loader = new HashMapLessLoader(map);
    LessOptions opts = buildOptions();
    opts.importRecursionLimit(recursionLimit);
    LessContext ctx = new LessContext(opts, loader);
    ctx.setCompiler(COMPILER);

    String source = "@import '1.less';";

    // Strict mode (safeMode=false): the recursion limit is a hard error.
    try {
      COMPILER.compile(source, ctx, path("."), path("foo.less"), false);
      fail("Expected import recursion limit exception");
    } catch (LessException e) {
      assertTrue(e.getMessage().contains("limit of " + recursionLimit + " exceeded"), e.getMessage());
    }

    // Recovery mode (safeMode=true): the failing import is dropped with a
    // warning, but the top sheet is *only* that import. Recovery rescues
    // nothing, so the empty-recovery outcome is a hard error even in
    // safe mode. A blank stylesheet must not ship on a green build.
    LessContext recovery = new LessContext(opts, loader);
    recovery.setCompiler(COMPILER);
    try {
      COMPILER.compile(source, recovery, path("."), path("foo.less"), true);
      fail("Expected empty-recovery hard error");
    } catch (LessException e) {
      assertTrue(e.getMessage().contains("produced no output"), e.getMessage());
    }
  }

  private static Path path(String path) {
    return Paths.get(path).toAbsolutePath().normalize();
  }

  @Test
  public void testFailedImportUnwindsDepth() throws LessException {
    // A failed import must unwind import depth. compile() resets depth
    // counters first, but parse() does not, so a failed parse leaks depth
    // into later parses on a reused context and trips a false recursion
    // limit error.
    LessOptions opts = buildOptions();
    opts.importRecursionLimit(3);
    LessContext ctx = new LessContext(opts, new HashMapLessLoader(buildMap()));
    ctx.setCompiler(COMPILER);

    // Six parses, each importing one missing file. The error must stay
    // file-not-found. Later parses must not falsely report recursion.
    for (int i = 0; i < 6; i++) {
      try {
        COMPILER.parse("@import 'missing.less';", ctx, path("."), null);
        fail("Expected file-not-found on parse " + (i + 1));
      } catch (LessException e) {
        assertTrue(e.getMessage().contains("File cannot be found"),
            "parse " + (i + 1) + ": " + e.getMessage());
      }
    }

    // Nested variant: the leaf import fails at depth 2. The error must
    // unwind fully and name the leaf file, not the chain head.
    Map<Path, String> map = new HashMap<>(buildMap());
    map.put(path("chain.less"), "@import 'missing.less';\n");
    LessContext nestedCtx = new LessContext(opts, new HashMapLessLoader(map));
    nestedCtx.setCompiler(COMPILER);
    for (int i = 0; i < 4; i++) {
      try {
        COMPILER.parse("@import 'chain.less';", nestedCtx, path("."), null);
        fail("Expected file-not-found on nested parse " + (i + 1));
      } catch (LessException e) {
        assertTrue(e.getMessage().contains("missing.less"),
            "nested parse " + (i + 1) + ": " + e.getMessage());
      }
    }
    // A fresh compile on the same context must still succeed.
    assertEquals(COMPILER.compile("@import 'base.less';", nestedCtx, path("."), null, true),
        ".child{font-size:12px}");
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

}
