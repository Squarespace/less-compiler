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

package com.squarespace.less.compat;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.squarespace.less.ExecuteErrorType;
import com.squarespace.less.HashMapLessLoader;
import com.squarespace.less.LessCompiler;
import com.squarespace.less.LessContext;
import com.squarespace.less.LessException;
import com.squarespace.less.LessOptions;
import com.squarespace.less.SyntaxErrorType;
import com.squarespace.less.exec.ExecEnv;
import com.squarespace.less.exec.FunctionTable;
import com.squarespace.less.model.Node;
import com.squarespace.less.parse.LessParser;
import com.squarespace.less.parse.LessSyntax;
import com.squarespace.less.plugins.ext.ExtStringFunctions;


/**
 * Both-behavior checks for the generation-2 patches: the default level
 * keeps the released behavior, the fully-fixed level applies the fix.
 */
public class CompatPatchTest {

  private static final LessCompiler COMPILER = new LessCompiler();

  private static final LessCompiler EXT_COMPILER;

  static {
    FunctionTable table = LessCompiler.defaultFunctionTable();
    table.register(new ExtStringFunctions());
    EXT_COMPILER = new LessCompiler(table);
  }

  private String compile(String raw, LessOptions opts) throws LessException {
    return COMPILER.compile(raw, new LessContext(opts));
  }

  private static LessOptions level(int level) {
    LessOptions opts = new LessOptions();
    opts.compatLevel(level);
    return opts;
  }

  /**
   * Evaluate a function-call fragment, mirroring the test harness, and
   * render the result through the context (compat-aware buffers).
   */
  private static String evalRender(String raw, LessOptions opts, LessCompiler compiler) throws LessException {
    LessContext ctx = new LessContext(opts);
    ctx.setCompiler(compiler);
    ExecEnv env = new ExecEnv(ctx);
    LessParser parser = new LessParser(ctx, raw);
    Node node = parser.parse(LessSyntax.FUNCTION_CALL).eval(env);
    return ctx.render(node);
  }

  private static String evalRender(String raw, LessOptions opts) throws LessException {
    return evalRender(raw, opts, COMPILER);
  }

  private static String evalRenderExt(String raw, LessOptions opts) throws LessException {
    return evalRender(raw, opts, EXT_COMPILER);
  }

  @Test
  public void testNonFiniteAsZero() throws LessException {
    // sqrt(-1) evaluates to a NaN dimension. The legacy default renders
    // it as 0, the fully-fixed level renders visible text.
    assertEquals(evalRender("sqrt(-1)", new LessOptions()), "0");
    assertEquals(evalRender("sqrt(-1)", level(Patch.maxThreshold())), "NaN");

    // A per-site override restores the legacy formatting at the fully
    // fixed level.
    LessOptions overridden = level(Patch.maxThreshold());
    overridden.compatPatch(Patch.NONFINITE_AS_ZERO);
    assertEquals(evalRender("sqrt(-1)", overridden), "0");
  }

  @Test
  public void testUnitConversionFactorsUngated() throws LessException {
    // The corrected conversion factors are ungated: they apply at every
    // compat level, so a sheet must compile identically at level 0 and at
    // the fully-fixed level. Guards against a future patch coupling the
    // conversion table to the level.
    String source = ".c {\n"
        + "  a: convert(1in, mm);\n"
        + "  b: convert(1cm, mm);\n"
        + "  c: convert(180deg, grad);\n"
        + "}\n";
    LessContext ctx0 = new LessContext(level(0));
    ctx0.setCompiler(COMPILER);
    String v0 = COMPILER.compile(source, ctx0);
    LessContext ctxMax = new LessContext(level(Patch.maxThreshold()));
    ctxMax.setCompiler(COMPILER);
    String vMax = COMPILER.compile(source, ctxMax);
    assertEquals(vMax, v0);
    assertTrue(v0.contains("25.4mm"), v0);
    assertTrue(v0.contains("10mm"), v0);
    assertTrue(v0.contains("200grad"), v0);
  }

  @Test
  public void testBadHexLengthUngated() throws LessException {
    // Non-3/6-digit hex runs parse as anonymous values and render verbatim.
    // Ungated: the old output and the new output are equally invalid CSS, so
    // no compat level accumulates a Patch for this and level 0 must agree
    // with the fully-fixed level.
    String source = ".c {\n"
        + "  a: #0000;\n"
        + "  b: #00000;\n"
        + "  c: #0000000;\n"
        + "}\n";
    LessContext ctx0 = new LessContext(level(0));
    ctx0.setCompiler(COMPILER);
    String v0 = COMPILER.compile(source, ctx0);
    LessContext ctxMax = new LessContext(level(Patch.maxThreshold()));
    ctxMax.setCompiler(COMPILER);
    String vMax = COMPILER.compile(source, ctxMax);
    assertEquals(vMax, v0);
    assertTrue(v0.contains("a: #0000;"), v0);
    assertTrue(v0.contains("b: #00000;"), v0);
    assertTrue(v0.contains("c: #0000000;"), v0);
  }

  @Test
  public void testFormatUnknownSpecifierUngated() throws LessException {
    // Unknown %X specifiers pass through format() literally and consume
    // no argument, so %('100% off', 5) renders '100% off' instead of
    // '1005off'. Ungated: no output could depend on the arg-mangling,
    // so level 0 must agree with the fully-fixed level.
    String source = ".c {\n"
        + "  a: %('100% off', 5);\n"
        + "  b: %('%x counts', 1);\n"
        + "  c: %('%s %x %d', one, 2);\n"
        + "}\n";
    LessContext ctx0 = new LessContext(level(0));
    ctx0.setCompiler(COMPILER);
    String v0 = COMPILER.compile(source, ctx0);
    LessContext ctxMax = new LessContext(level(Patch.maxThreshold()));
    ctxMax.setCompiler(COMPILER);
    String vMax = COMPILER.compile(source, ctxMax);
    assertEquals(vMax, v0);
    assertTrue(v0.contains("a: '100% off';\n"), v0);
    assertTrue(v0.contains("b: '%x counts';\n"), v0);
    assertTrue(v0.contains("c: 'one %x 2';\n"), v0);
  }

  @Test
  public void testImportFeaturesNumberFormatting() throws LessException {
    // The @import line renders its evaluated media features through a
    // scratch buffer built outside the buffer stack. That buffer must
    // inherit the context's compat level, so number formatting matches
    // the rest of the output. Regression: it once used the buffer's
    // fully-fixed default and rendered NaN while the sheet rendered 0.
    String source = "@w: sqrt(-1);\n"
        + "@import url(\"a.less\") screen and (max-width: @w);\n"
        + ".y { x: @w; }\n";

    LessContext ctx = new LessContext(new LessOptions());
    ctx.setCompiler(COMPILER);
    String css = COMPILER.compile(source, ctx, Paths.get("."), Paths.get("t.less"));
    assertTrue(css.contains("max-width: 0"), css);
    assertTrue(css.contains("x: 0"), css);
    assertTrue(!css.contains("NaN"), css);
  }

  @Test
  public void testSelectorComplexityOverflow() throws LessException {
    // 65 comma siblings at 64 nesting levels: the cartesian product of
    // combined selectors exceeds the complexity threshold.
    StringBuilder less = new StringBuilder();
    for (int i = 0; i < 65; i++) {
      if (i > 0) {
        less.append(", ");
      }
      less.append(".a").append(i);
    }
    less.append(" {\n");
    for (int d = 0; d < 63; d++) {
      less.append("  ").append(".b").append(d).append(" {\n");
    }
    less.append("    color: red;\n");
    for (int d = 0; d < 63; d++) {
      less.append("  }");
    }
    less.append("\n}\n");

    // Legacy default: the overflow is swallowed, compile succeeds.
    compile(less.toString(), new LessOptions());

    // Fully fixed: the compile fails with SELECTOR_TOO_COMPLEX.
    try {
      compile(less.toString(), level(Patch.maxThreshold()));
      fail("expected SELECTOR_TOO_COMPLEX at the fixed level");
    } catch (LessException e) {
      assertEquals(e.primaryError().type(), ExecuteErrorType.SELECTOR_TOO_COMPLEX);
    }
  }

  @Test
  public void testModZeroStrict() throws LessException {
    // Legacy default: mod by zero silently returns NaN (renders as 0).
    assertEquals(evalRender("mod(10, 0)", new LessOptions()), "0");

    // Fully fixed level, strict: fails like division.
    try {
      evalRender("mod(10, 0)", level(Patch.maxThreshold()));
      fail("expected DIVIDE_BY_ZERO at the fixed level");
    } catch (LessException e) {
      assertEquals(e.primaryError().type(), ExecuteErrorType.DIVIDE_BY_ZERO);
    }

    // Fully fixed level, lenient: warns, returns NaN, renders as text.
    LessOptions lenient = level(Patch.maxThreshold());
    lenient.strict(false);
    assertEquals(evalRender("mod(10, 0)", lenient), "NaN");

    // Non-zero divisors work at every level.
    assertEquals(evalRender("mod(11, 3)", level(Patch.maxThreshold())), "2");
  }

  @Test
  public void testImportUrlInline() throws LessException {
    // HashMapLessLoader keys must match resolvePath() output: absolute.
    Map<Path, String> files = new HashMap<>();
    files.put(Paths.get(".").resolve("a.less").toAbsolutePath().normalize(), ".a { color: red; }");
    String source = "@import url(\"a.less\");\n";

    // Legacy default: url() imports are emitted literally.
    LessContext legacy = new LessContext(new LessOptions(), new HashMapLessLoader(files));
    legacy.setCompiler(COMPILER);
    String css = COMPILER.compile(source, legacy, Paths.get("."), Paths.get("t.less"));
    assertTrue(css.contains("@import url(\"a.less\")"), css);

    // Fully fixed: the import is resolved and inlined.
    LessContext fixed = new LessContext(level(Patch.maxThreshold()), new HashMapLessLoader(files));
    fixed.setCompiler(COMPILER);
    css = COMPILER.compile(source, fixed, Paths.get("."), Paths.get("t.less"));
    assertTrue(css.contains("color: red"), css);
    assertTrue(!css.contains("@import url(\"a.less\")"), css);
  }

  @Test
  public void testConvertIncompatibleUnits() throws LessException {
    // Legacy: convert() to an incompatible unit silently emits 0.
    assertEquals(evalRender("convert(16px, em)", new LessOptions()), "0em");

    // Fixed: fails the compile with INCOMPATIBLE_UNITS.
    try {
      evalRender("convert(16px, em)", level(Patch.maxThreshold()));
      fail("expected INCOMPATIBLE_UNITS at the fixed level");
    } catch (LessException e) {
      assertEquals(e.primaryError().type(), ExecuteErrorType.INCOMPATIBLE_UNITS);
    }

    // Compatible conversions work at every level.
    assertEquals(evalRender("convert(1in, px)", level(Patch.maxThreshold())), "96px");
  }

  @Test
  public void testAttrSelectorUnterminated() throws LessException {
    String source = "a[href { color: red; }";

    // Legacy: the unterminated attribute is silently dropped and the
    // bare element is styled.
    assertTrue(compile(source, new LessOptions()).contains("a {"));

    // Fixed: the parse fails loudly.
    try {
      compile(source, level(Patch.maxThreshold()));
      fail("expected INCOMPLETE_PARSE at the fixed level");
    } catch (LessException e) {
      assertEquals(e.primaryError().type(), SyntaxErrorType.INCOMPLETE_PARSE);
    }
  }

  @Test
  public void testColorChannelPrecision() throws LessException {
    // Legacy: fractional scalar operands truncate before channel math.
    assertTrue(compile("x { c: #fff * 0.5; }", new LessOptions()).contains("#000"));

    // Fixed: fractional values survive until the final round.
    assertTrue(compile("x { c: #fff * 0.5; }", level(Patch.maxThreshold())).contains("grey"));
    assertTrue(compile("x { c: #000 + 0.5; }", level(Patch.maxThreshold())).contains("#010101"));
    assertTrue(compile("x { c: #808080 / 3; }", level(Patch.maxThreshold())).contains("#2b2b2b"));
  }

  @Test
  public void testColorBlendAlpha() throws LessException {
    String raw = "multiply(rgba(255, 0, 0, 0.5), rgba(0, 0, 255, 0.25))";

    // Legacy: the blend result is opaque.
    assertEquals(evalRender(raw, new LessOptions()), "#000");

    // Fixed: the result keeps the larger input alpha.
    assertEquals(evalRender(raw, level(Patch.maxThreshold())), "rgba(0, 0, 0, .5)");
  }

  @Test
  public void testImportOnceSuppress() throws LessException {
    Map<Path, String> files = new HashMap<>();
    files.put(Paths.get(".").resolve("f.less").toAbsolutePath().normalize(), ".f { color: red; }");
    String source = "@import 'f.less';\n@import-once 'f.less';\n";

    // Legacy: the plain import cached the file first, so import-once
    // does not suppress the second inline.
    LessContext legacy = new LessContext(new LessOptions(), new HashMapLessLoader(files));
    legacy.setCompiler(COMPILER);
    String css = COMPILER.compile(source, legacy, Paths.get("."), Paths.get("t.less"));
    assertEquals(css.split("color: red").length - 1, 2, css);

    // Fixed: import-once suppresses regardless of cache order.
    LessContext fixed = new LessContext(level(Patch.maxThreshold()), new HashMapLessLoader(files));
    fixed.setCompiler(COMPILER);
    css = COMPILER.compile(source, fixed, Paths.get("."), Paths.get("t.less"));
    assertEquals(css.split("color: red").length - 1, 1, css);
  }

  @Test
  public void testImportExtCase() throws LessException {
    Map<Path, String> files = new HashMap<>();
    files.put(Paths.get(".").resolve("A.LESS").toAbsolutePath().normalize(), ".a { color: red; }");
    String source = "@import \"A.LESS\";\n";

    // Legacy: uppercase extensions are not matched, ".less" is appended
    // and the file cannot be resolved.
    LessContext legacy = new LessContext(new LessOptions(), new HashMapLessLoader(files));
    legacy.setCompiler(COMPILER);
    try {
      COMPILER.compile(source, legacy, Paths.get("."), Paths.get("t.less"));
      fail("expected IMPORT_ERROR at the default level");
    } catch (LessException e) {
      // expected
    }

    // Fixed: extensions match case-insensitively.
    LessContext fixed = new LessContext(level(Patch.maxThreshold()), new HashMapLessLoader(files));
    fixed.setCompiler(COMPILER);
    String css = COMPILER.compile(source, fixed, Paths.get("."), Paths.get("t.less"));
    assertTrue(css.contains("color: red"), css);
  }

  @Test
  public void testGuardCompareUncomparable() throws LessException {
    String source = ".m(@a) when (@a != 10px) { p: 1; }\n.x { .m(red); }\n";

    // Legacy: an uncomparable guard operand compares as -1, != is true.
    assertTrue(compile(source, new LessOptions()).contains("p: 1"));

    // Fixed: no ordering or equality exists, != is false.
    assertTrue(!compile(source, level(Patch.maxThreshold())).contains("p: 1"));
  }

  @Test
  public void testArgumentsOrder() throws LessException {
    String source = ".m(@a, @b) { p: @arguments; }\n.x { .m(@b: 2, @a: 1); }\n";

    // Legacy: @arguments follows binding insertion order.
    assertTrue(compile(source, new LessOptions()).contains("p: 2 1"));

    // Fixed: @arguments follows parameter declaration order.
    assertTrue(compile(source, level(Patch.maxThreshold())).contains("p: 1 2"));
  }

  @Test
  public void testVariadicNamedArg() throws LessException {
    String source = ".m(@b...) { p: @b; }\n.x { .m(@b: 1); }\n";

    // Legacy: a named arg targeting the variadic parameter fails.
    try {
      compile(source, new LessOptions());
      fail("expected ARG_NAMED_NOTFOUND at the default level");
    } catch (LessException e) {
      assertEquals(e.primaryError().type(), ExecuteErrorType.ARG_NAMED_NOTFOUND);
    }

    // Fixed: the named arg binds to the variadic parameter.
    assertTrue(compile(source, level(Patch.maxThreshold())).contains("p: 1"));
  }

  @Test
  public void testGuardCompareFullTable() throws LessException {
    // Measured contract (1.7.2 = the released -1 semantics):
    //   released:  < T  <= T  == F  != T  > F  >= F
    //   fixed:     < T  <= F  == F  != F  > F  >= F   (only '<' true)
    //   override at fixed: released table again.
    String[] ops = { "<", "<=", "=", "!=", ">", ">=" };
    boolean[] released = { true, true, false, true, false, false };
    boolean[] fixed = { true, false, false, false, false, false };
    for (int i = 0; i < ops.length; i++) {
      String source = ".m(@a) when (@a " + ops[i] + " 10px) { p: 1; }\n.x { .m(red); }\n";
      String css = compile(source, new LessOptions());
      assertEquals(css.contains("p: 1"), released[i], ops[i] + " at the released level");

      LessOptions opts = new LessOptions();
      opts.compatLevel(Patch.maxThreshold());
      css = compile(source, opts);
      assertEquals(css.contains("p: 1"), fixed[i], ops[i] + " at the fixed level");

      LessOptions overridden = new LessOptions();
      overridden.compatLevel(Patch.maxThreshold());
      overridden.compatPatch(Patch.GUARD_COMPARE_UNCOMPARABLE);
      css = compile(source, overridden);
      assertEquals(css.contains("p: 1"), released[i], ops[i] + " with the per-site override");
    }
  }

  @Test
  public void testReplaceRegexGroups() throws LessException {
    String raw = "replace(\"abc 123\", \"([a-z]+) ([0-9]+)\", \"$2 $1\")";

    // Legacy: the replacement is a regex replacement, group refs work.
    String legacy = evalRenderExt(raw, new LessOptions());
    assertTrue(legacy.contains("123 abc"), legacy);

    // Fixed: the replacement is inserted literally.
    String fixed = evalRenderExt(raw, level(Patch.maxThreshold()));
    assertTrue(fixed.contains("$2 $1"), fixed);
  }

  @Test
  public void testNumberExpo() throws LessException {
    // Corpus evidence (coyote-beagle-sfhy.less, chasewild.less): the
    // release tokenizer stops a number at 'e'/'E' and the exponent
    // becomes identifier(+number) noise; the fixed grammar reads a
    // single CSS number. em/ex units must be unaffected at every level.
    String less = ".a { z-index: 9.999999999999999e+31; }\n"
        + ".b { colour: 7E705E !important; }\n"
        + ".c { width: 1e3; }\n"
        + ".d { width: 5em; letter-spacing: 1.5ex; }\n";

    // Level 0 (released surface): number + stray identifier tokens.
    String legacy = compile(less, level(0));
    assertTrue(legacy.contains("z-index: 10 e 31;"), legacy);
    assertTrue(legacy.contains("colour: 7 E705E !important;"), legacy);
    assertTrue(legacy.contains("width: 1 e3;"), legacy);
    assertTrue(legacy.contains("width: 5em;"), legacy);
    assertTrue(legacy.contains("letter-spacing: 1.5ex;"), legacy);

    // Level 1: the exponent fix is a threshold-2 patch, so tokenization
    // still matches the release.
    String mid = compile(less, level(1));
    assertTrue(mid.contains("z-index: 10 e 31;"), mid);
    assertTrue(mid.contains("colour: 7 E705E !important;"), mid);

    // Fully fixed: single numbers. 7E705 overflows to +Infinity and the
    // NONFINITE_AS_ZERO legacy patch is lifted at this level too, so it
    // renders visibly.
    String fixed = compile(less, level(Patch.maxThreshold()));
    assertTrue(fixed.contains("z-index: 99999999999999990000000000000000;"), fixed);
    assertTrue(fixed.contains("colour: Infinity E !important;"), fixed);
    assertTrue(fixed.contains("width: 1000;"), fixed);
    assertTrue(fixed.contains("width: 5em;"), fixed);
    assertTrue(fixed.contains("letter-spacing: 1.5ex;"), fixed);

    // A per-site override restores the released tokenization at the
    // fully fixed level.
    LessOptions overridden = level(Patch.maxThreshold());
    overridden.compatPatch(Patch.NUMBER_EXPO);
    String relegacy = compile(less, overridden);
    assertTrue(relegacy.contains("z-index: 10 e 31;"), relegacy);
    assertTrue(relegacy.contains("width: 1 e3;"), relegacy);
  }
}
