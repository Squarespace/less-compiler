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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.model.Stylesheet;


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


  @Test
  public void testPreCacheSharesParsedImportsAcrossCompiles() throws LessException {
    // A shared preCache means each imported stylesheet is parsed once
    // across compiles (the harness scan / batch pattern). Every compile
    // receives a fresh deep copy, so outputs are byte-identical to
    // fresh-parse compiles and one compile can never taint the next.
    Map<Path, String> files = new HashMap<>();
    files.put(path("lib.less"),
        "@size: 5px;\n" +
        ".lib-mixin(@v) when (@v > 0px) {\n" +
        "  width: @v;\n" +
        "  .inner(@v) when (default()) {\n" +
        "    margin: @v * 2;\n" +
        "    color: @size;\n" +
        "  }\n" +
        "  .inner(@v);\n" +
        "}\n" +
        ".def { color: red; }\n");
    CountingLoader counting = new CountingLoader(new HashMapLessLoader(files));
    Map<Path, Stylesheet> preCache = new HashMap<>();

    LessOptions opts1 = buildOptions();
    LessContext ctx1 = new LessContext(opts1, counting, preCache);
    ctx1.setCompiler(COMPILER);
    // Compile 1 expands the imported mixin (splicing its copy).
    String css1 = COMPILER.compile(".x { .lib-mixin(10px); }\n@import 'lib.less';\n",
        ctx1, Paths.get("."), null, true);

    LessOptions opts2 = buildOptions();
    LessContext ctx2 = new LessContext(opts2, counting, preCache);
    ctx2.setCompiler(COMPILER);
    // Compile 2 imports the same library with different mixin arguments:
    // splice-sensitive. Leaked expansion state from compile 1 would
    // show up as wrong margin/color values here. Must reuse the cached
    // parse.
    String css2 = COMPILER.compile("@import 'lib.less';\n.y { .lib-mixin(20px); height: @size; }\n",
        ctx2, Paths.get("."), null, true);
    assertEquals(1, counting.count(),
        "lib.less must be parsed exactly once across both compiles");

    // The cached parse must be pristine after two consuming compiles:
    // content-identical to an independent fresh parse of the same file.
    LessContext pristine = new LessContext(buildOptions(), new HashMapLessLoader(files));
    pristine.setCompiler(COMPILER);
    Stylesheet freshLib = COMPILER.parse(files.get(path("lib.less")), pristine,
        Paths.get("."), null);
    Stylesheet cachedLib = preCache.get(path("lib.less"));
    assertNotNull(cachedLib, "lib.less must have been cached");
    assertEquals(cachedLib.toString(), freshLib.toString(),
        "cached parse tree must be unmutated by consuming compiles");

    // Parity: outputs with the shared cache equal outputs with fresh
    // per-compile caches (no cross-compile taint).
    Map<Path, Stylesheet> fresh1 = new HashMap<>();
    LessContext ctx3 = new LessContext(buildOptions(), counting, fresh1);
    ctx3.setCompiler(COMPILER);
    String css3 = COMPILER.compile(".x { .lib-mixin(10px); }\n@import 'lib.less';\n",
        ctx3, Paths.get("."), null, true);
    assertEquals(css1, css3);

    Map<Path, Stylesheet> fresh2 = new HashMap<>();
    LessContext ctx4 = new LessContext(buildOptions(), counting, fresh2);
    ctx4.setCompiler(COMPILER);
    String css4 = COMPILER.compile("@import 'lib.less';\n.y { .lib-mixin(20px); height: @size; }\n",
        ctx4, Paths.get("."), null, true);
    assertEquals(css2, css4);
  }

  @Test
  public void testSharedPreCacheConcurrency() throws Exception {
    // The exact failure mode that forced the harness to a thread-local
    // cache: one shared parsed-import map consumed concurrently produced
    // nondeterministic spurious VAR_CIRCULAR_REFERENCE errors and silently
    // corrupted output bytes. With deepCopy at the consume handoff, any
    // number of compiles may share the cache: zero errors and byte
    // parity with fresh-parse compiles.
    Map<Path, String> files = new HashMap<>();
    files.put(path("core.less"),
        "@core-hue: 200;\n@core-color: hsl(@core-hue, 50%, 50%);\n");
    files.put(path("lib.less"),
        "@import 'core.less';\n" +
        "@lib-accent: @core-color;\n" +
        ".outer(@p, @c) when (@p > 0px) {\n" +
        "  border: @p solid @c;\n" +
        "  .inner(@p, @c) when (default()) {\n" +
        "    margin: @p * 2;\n" +
        "    padding: @p;\n" +
        "    color: @lib-accent;\n" +
        "  }\n" +
        "  .inner(@p, @c);\n" +
        "}\n");

    int threads = 8;
    int filesPerThread = 20;
    int rounds = 5;

    // Reference: every file compiled alone, no shared cache at all.
    Map<Integer, String> reference = new HashMap<>();
    for (int i = 0; i < filesPerThread; i++) {
      String source = "@import 'lib.less';\n"
          + ".page-" + i + " { .outer(" + (i + 1) + "px, blue); }\n";
      LessContext ctx = new LessContext(buildOptions(),
          new HashMapLessLoader(files));
      ctx.setCompiler(COMPILER);
      reference.put(i, COMPILER.compile(source, ctx, Paths.get("."), null, true));
    }

    final Map<Path, Stylesheet> sharedCache = new java.util.concurrent.ConcurrentHashMap<>();
    for (int round = 0; round < rounds; round++) {
      final List<Future<String>> futures = new ArrayList<>();
      ExecutorService pool = Executors.newFixedThreadPool(threads);
      try {
        for (int i = 0; i < filesPerThread; i++) {
          final int idx = i;
          futures.add(pool.submit(() -> {
            String source = "@import 'lib.less';\n"
                + ".page-" + idx + " { .outer(" + (idx + 1) + "px, blue); }\n";
            LessContext ctx = new LessContext(buildOptions(),
                new HashMapLessLoader(files), sharedCache);
            ctx.setCompiler(COMPILER);
            return COMPILER.compile(source, ctx, Paths.get("."), null, true);
          }));
        }
        for (int i = 0; i < filesPerThread; i++) {
          // A LessException here is the spurious VAR_CIRCULAR_REFERENCE
          // (or similar) mode. A mismatch is the silent-corruption mode.
          String out = futures.get(i).get();
          assertEquals(out, reference.get(i),
              "round " + round + ": file " + i + " output diverged from fresh-parse reference");
        }
      } finally {
        pool.shutdown();
      }
    }

    // After all rounds, every cached tree must still be pristine.
    LessContext pristine = new LessContext(buildOptions(), new HashMapLessLoader(files));
    pristine.setCompiler(COMPILER);
    for (String name : new String[] { "lib.less", "core.less" }) {
      Stylesheet cached = sharedCache.get(path(name));
      assertNotNull(cached, name + " must have been cached");
      Stylesheet fresh = COMPILER.parse(files.get(path(name)), pristine,
          Paths.get("."), null);
      assertEquals(cached.toString(), fresh.toString(),
          name + ": cached parse tree must stay unmutated under concurrency");
    }
  }

  /** Wraps a loader, counting distinct load() invocations (tests only). */
  private static final class CountingLoader implements LessLoader {

    private final LessLoader delegate;
    private int count = 0;

    CountingLoader(LessLoader delegate) {
      this.delegate = delegate;
    }

    int count() {
      return count;
    }

    @Override
    public boolean exists(Path path) {
      return delegate.exists(path);
    }

    @Override
    public String load(Path path) throws LessException {
      count++;
      return delegate.load(path);
    }
  }
}
