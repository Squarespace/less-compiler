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
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
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

import com.squarespace.less.LessException;
import com.squarespace.less.LessOptions;
import com.squarespace.less.compat.Patch;
import com.squarespace.less.core.FlexList;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.exec.SelectorUtils;
import com.squarespace.less.model.Block;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.NodeType;
import com.squarespace.less.model.Ruleset;
import com.squarespace.less.model.Selector;
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

  @Test
  public void testMixinClosurePerCompile() throws LessException {
    // The mixin definition lives in a cached import. The definition-site
    // closure must be captured per compile, not cached on the shared tree.
    Map<Path, String> map = new HashMap<>();
    map.put(path("lib.less"), ".use-leak() { p: @leak; }\n");
    LessLoader loader = new HashMapLessLoader(map);
    LessOptions opts = buildOptions();
    opts.importOnce(false); // compile 2 must re-resolve the import from cache
    LessContext ctx = new LessContext(opts, loader);
    ctx.setCompiler(COMPILER);

    String first = COMPILER.compile("@leak: red; @import 'lib.less'; .a { .use-leak(); }", ctx, path("."), null, true);
    assertEquals(first, ".a{p:red}");

    // Same context, second compile: must see this compile's @leak value.
    String second = COMPILER.compile("@leak: blue; @import 'lib.less'; .a { .use-leak(); }", ctx, path("."), null, true);
    assertEquals(second, ".a{p:blue}");
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
    opts.compatLevel(Patch.maxThreshold());
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
  public void testReusedContextResetsImportTakeBetweenCompiles() throws LessException {
    // A LessContext (and the LessImporter it owns) may be reused for
    // several compiles against the same shared preCache. Each compile
    // must take its own copy of an imported path instead of reusing the
    // previous compile's already rendered tree. Tracing makes a leaked
    // take() visible directly: a leaked copy already carries the first
    // compile's import markers, so the second compile's markers would
    // wrap around them instead of standing alone.
    Map<Path, String> files = new HashMap<>();
    files.put(path("lib.less"), ".lib { color: red; }\n");

    LessOptions opts = new LessOptions();
    opts.tracing(true);
    opts.importOnce(false);
    Map<Path, Stylesheet> preCache = new HashMap<>();
    LessContext ctx = new LessContext(opts, new HashMapLessLoader(files), preCache);
    ctx.setCompiler(COMPILER);

    String source = "@import 'lib.less';\n.page { width: 1px; }\n";
    COMPILER.compile(source, ctx, Paths.get("."), null, true);
    String second = COMPILER.compile(source, ctx, Paths.get("."), null, true);

    LessContext fresh = new LessContext(opts, new HashMapLessLoader(files));
    fresh.setCompiler(COMPILER);
    String independent = COMPILER.compile(source, fresh, Paths.get("."), null, true);

    assertEquals(second, independent,
        "a compile on a reused context must match an independent compile of the same source");
    // A leaked take() from the first compile already carries an import
    // marker pair; the second compile would then wrap its own pair
    // around it, doubling the "start"/"end" import markers even though
    // the source only names the import once.
    assertEquals(countOccurrences(second, "start   @import"), 1,
        "the second compile must carry only its own import markers, not the first compile's: " + second);
  }

  @Test
  public void testSharedTakeImportMarkersArePerSite() throws LessException {
    // The take() memo hands every import site of a path the same
    // per-compile copy. When tracing is on, each site's marker pair must
    // go onto a fresh per-site block over that copy's rules; a site must
    // not render an earlier site's TRACE pair inside its own content.
    Map<Path, String> files = new HashMap<>();
    files.put(path("lib.less"), ".lib { color: red; }\n");

    LessOptions opts = new LessOptions();
    opts.tracing(true);
    opts.importOnce(false);
    Map<Path, Stylesheet> preCache = new HashMap<>();
    LessContext ctx = new LessContext(opts, new HashMapLessLoader(files), preCache);
    ctx.setCompiler(COMPILER);

    String source = "@import 'lib.less';\n.page { width: 1px; }\n@import 'lib.less';\n";
    String css = COMPILER.compile(source, ctx, Paths.get("."), null, true);

    // Two import statements, so the output must carry exactly two
    // start/end pairs. A second site taking the same memoized copy would
    // wrap this site's pair around the first site's pair, doubling the
    // counts.
    assertEquals(countOccurrences(css, "start   @import"), 2,
        "each import site must render exactly one start marker: " + css);
    assertEquals(countOccurrences(css, "end   @import"), 2,
        "each import site must render exactly one end marker: " + css);
  }

  @Test
  public void testParseResetsImportTakeOnReusedContext() throws LessException {
    // The take() memo is per-compile, and parse() consumes imports
    // through the same take(). A reused context must reset the memo
    // before parsing too, not only at compile() time; otherwise the
    // second parse receives the first parse's memoized deep copy and
    // the two parses share node instances.
    Map<Path, String> files = new HashMap<>();
    files.put(path("lib.less"), ".lib { color: red; }\n");

    LessOptions opts = new LessOptions();
    opts.tracing(true);
    opts.importOnce(false);
    Map<Path, Stylesheet> preCache = new HashMap<>();
    LessContext ctx = new LessContext(opts, new HashMapLessLoader(files), preCache);
    ctx.setCompiler(COMPILER);

    String source = "@import 'lib.less';\n";
    Stylesheet first = COMPILER.parse(source, ctx, Paths.get("."), null);
    Stylesheet second = COMPILER.parse(source, ctx, Paths.get("."), null);

    // The imported ruleset comes from the memoized take() of the shared
    // preCache. The second parse must hold a fresh copy, not the first
    // parse's instance.
    assertNotSame(findFirstRuleset(first), findFirstRuleset(second),
        "the second parse must take a fresh copy of the imported sheet");
  }

  @Test
  public void testTakeMemoSurvivesDistinctImportsInOneCompile() throws LessException {
    // parse0 resets the take() memo on entry, but it is also re-entered
    // recursively for every @import not already in the shared preCache.
    // A compile importing two distinct uncached paths wiped the memo
    // when the second path was read, dropping the first path's copy, so
    // the later site of the first path took a fresh deep copy instead
    // of the same instance. Both sites of one path must alias within
    // the same top-level parse (which parse() and compile() share).
    Map<Path, String> files = new HashMap<>();
    files.put(path("a.less"), ".from-a { a: 1; }\n");
    files.put(path("b.less"), ".from-b { b: 2; }\n");

    LessOptions opts = new LessOptions();
    opts.tracing(true);
    opts.importOnce(false);
    Map<Path, Stylesheet> preCache = new HashMap<>();
    LessContext ctx = new LessContext(opts, new HashMapLessLoader(files), preCache);
    ctx.setCompiler(COMPILER);

    String source = "@import 'a.less';\n@import 'b.less';\n@import 'a.less';\n";
    Stylesheet sheet = COMPILER.parse(source, ctx, Paths.get("."), null);

    List<Ruleset> fromA = findRulesetsNamed(sheet, ".from-a");
    assertEquals(fromA.size(), 2, "a.less is imported at two sites");
    assertSame(fromA.get(0), fromA.get(1),
        "both a.less sites must share the same instance within one parse");
  }

  private static List<Ruleset> findRulesetsNamed(Stylesheet sheet, String name) {
    List<Ruleset> found = new ArrayList<>();
    collectRulesetsNamed(sheet.block(), name, found);
    return found;
  }

  private static void collectRulesetsNamed(Block block, String name, List<Ruleset> found) {
    FlexList<Node> rules = block.rules();
    int size = rules.size();
    for (int i = 0; i < size; i++) {
      Node node = rules.get(i);
      if (node == null) {
        continue;
      }
      if (node.type() == NodeType.RULESET) {
        if (rulesetName((Ruleset) node).equals(name)) {
          found.add((Ruleset) node);
        }
      } else if (node.type() == NodeType.BLOCK) {
        collectRulesetsNamed((Block) node, name, found);
      }
    }
  }

  private static String rulesetName(Ruleset ruleset) {
    List<Selector> selectors = ruleset.selectors().selectors();
    if (selectors.isEmpty()) {
      return "";
    }
    List<String> parts = SelectorUtils.renderSelector(selectors.get(0));
    return parts == null ? "" : String.join(" ", parts);
  }

  private static Ruleset findFirstRuleset(Stylesheet sheet) {
    FlexList<Node> rules = sheet.block().rules();
    int size = rules.size();
    for (int i = 0; i < size; i++) {
      Node node = rules.get(i);
      if (node != null && node.type() == NodeType.RULESET) {
        return (Ruleset) node;
      }
    }
    throw new AssertionError("expected an imported ruleset in tree: " + sheet);
  }

  private static int countOccurrences(String text, String needle) {
    int count = 0;
    int from = 0;
    int at;
    while ((at = text.indexOf(needle, from)) >= 0) {
      count++;
      from = at + needle.length();
    }
    return count;
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
