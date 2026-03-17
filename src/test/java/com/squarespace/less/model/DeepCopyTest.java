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

package com.squarespace.less.model;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.squarespace.less.HashMapLessLoader;
import com.squarespace.less.LessCompiler;
import com.squarespace.less.LessException;
import com.squarespace.less.LessContext;
import com.squarespace.less.LessOptions;
import com.squarespace.less.core.LessTestBase;


/**
 * Contract tests for {@link Node#deepCopy()}: the deep copy of a parsed
 * stylesheet must be content-identical to a fresh parse, and must share no
 * mutable node with the source, so a shared parse-tree cache can never be
 * tainted by a consuming compile.
 */
public class DeepCopyTest extends LessTestBase {

  private static final LessCompiler COMPILER = new LessCompiler();

  /**
   * A parse-level soup that exercises every container node type:
   * definitions (incl. indirect and curly), rules, a guarded mixin with
   * nested mixin calls, media, block directives, quoted strings with
   * interpolation, function calls, operations, shorthands, urls, alpha,
   * assignments, attribute selectors, and every value type.
   */
  private static final String RICH_SOURCE =
      "@charset \"utf-8\";\n" +
      "@base: #ff8000;\n" +
      "@half: 50%;\n" +
      "@indirect: base;\n" +
      "@str: \"text-@{base}\";\n" +
      ".wrap {\n" +
      "  .lib-mixin(@w) when (@w > 0px) {\n" +
      "    width: @w;\n" +
      "    .inner(@pad) when (default()) {\n" +
      "      margin: @pad;\n" +
      "      border: 1px solid rgb(1, 2, 3);\n" +
      "    }\n" +
      "    .inner(4px);\n" +
      "  }\n" +
      "}\n" +
      ".media {\n" +
      "  @media (max-width: 600px) {\n" +
      "    color: hsl(120, 50%, 50%);\n" +
      "  }\n" +
      "}\n" +
      "@keyframes spin {\n" +
      "  from { transform: rotate(0deg); }\n" +
      "  to { transform: rotate(360deg); }\n" +
      "}\n" +
      "a[href=\"external\"] {\n" +
      "  text: @str;\n" +
      "  background: url(\"foo.png\");\n" +
      "  font: 12px/1.5 sans-serif;\n" +
      "  opacity: alpha(opacity=100);\n" +
      "  filter: progid:DXImageTransform.Microsoft.Alpha(opacity=@half);\n" +
      "  color: transparent;\n" +
      "  width: 100px + 2em;\n" +
      "  padding: 1px 2px 3px 4px;\n" +
      "}\n" +
      ".u { unicode-range: U+0025-00FF; }\n" +
      "// single line comment\n" +
      "/* block comment */\n" +
      ".imp { border: 1px solid red !important; }\n";

  /**
   * The soup plus imports: a resolvable LESS import (inlined at parse
   * time, exercising the preCache consume path during parse) and a CSS
   * import (left as an Import node in the parse tree, which the mutation
   * probe below needs).
   */
  private static final String IMPORT_SOURCE =
      "@import \"sub.less\";\n" +
      "@import \"sub.css\";\n" +
      RICH_SOURCE;

  /**
   * Build the loader map for the import tests: sub.less is resolvable,
   * sub.css is not (and never needs to be).
   */
  private static Map<Path, String> importFiles() {
    Map<Path, String> map = new HashMap<>();
    map.put(Paths.get("sub.less").toAbsolutePath().normalize(),
        "@sub: 7px;\n.sub-rule { width: @sub; }\n");
    return map;
  }

  /**
   * Node classes that are structurally immutable (no mutable fields, no
   * Node children) and are therefore deliberately shared by deepCopy()
   * rather than re-allocated.
   */
  private static boolean isShareable(Node node) {
    return node instanceof Keyword
        || node instanceof Anonymous
        || node instanceof Dimension
        || node instanceof Ratio
        || node instanceof UnicodeRange
        || node instanceof Property
        || node instanceof Variable
        || node instanceof Comment
        || node instanceof TextElement
        || node instanceof HSLColor;
  }

  /**
   * Invokes {@code visit} on each direct child node of {@code node}.
   */
  private static void forChildren(Node node, ChildVisitor visit) {
    if (node instanceof Block) {
      Block block = (Block)node;
      visit.visit(block.charset());
      for (int i = 0; i < block.rules().size(); i++) {
        visit.visit(block.rules().get(i));
      }
    } else if (node instanceof BlockNode) {
      visit.visit(((BlockNode)node).block());
    } else if (node instanceof Mixin) {
      visit.visit(((Mixin)node).params());
      visit.visit(((Mixin)node).guard());
    } else if (node instanceof Media) {
      visit.visit(((Media)node).features());
    } else if (node instanceof Import) {
      visit.visit(((Import)node).path());
      visit.visit(((Import)node).features());
    } else if (node instanceof ImportMarker) {
      visit.visit(((ImportMarker)node).importStatement());
    } else if (node instanceof MixinMarker) {
      visit.visit(((MixinMarker)node).mixinCall());
      visit.visit(((MixinMarker)node).mixinDefinition());
    } else if (node instanceof MixinCall) {
      visit.visit(((MixinCall)node).selector());
      visit.visit(((MixinCall)node).args());
    } else if (node instanceof Selectors) {
      java.util.List<Selector> selectors = ((Selectors)node).selectors();
      for (int i = 0; i < selectors.size(); i++) {
        visit.visit(selectors.get(i));
      }
    } else if (node instanceof Selector) {
      java.util.List<Element> elements = ((Selector)node).elements();
      for (int i = 0; i < elements.size(); i++) {
        visit.visit(elements.get(i));
      }
    } else if (node instanceof ValueElement) {
      visit.visit(((ValueElement)node).value());
    } else if (node instanceof AttributeElement) {
      java.util.List<Node> parts = ((AttributeElement)node).parts();
      for (int i = 0; i < parts.size(); i++) {
        visit.visit(parts.get(i));
      }
    } else if (node instanceof MixinParams) {
      java.util.List<Parameter> params = ((MixinParams)node).params();
      for (int i = 0; i < params.size(); i++) {
        visit.visit(params.get(i));
      }
    } else if (node instanceof Parameter) {
      visit.visit(((Parameter)node).value());
    } else if (node instanceof MixinCallArgs) {
      java.util.List<Argument> args = ((MixinCallArgs)node).args();
      for (int i = 0; i < args.size(); i++) {
        visit.visit(args.get(i));
      }
    } else if (node instanceof Argument) {
      visit.visit(((Argument)node).value());
    } else if (node instanceof Guard) {
      java.util.List<Condition> conditions = ((Guard)node).conditions();
      for (int i = 0; i < conditions.size(); i++) {
        visit.visit(conditions.get(i));
      }
    } else if (node instanceof Condition) {
      visit.visit(((Condition)node).left());
      visit.visit(((Condition)node).right());
    } else if (node instanceof Features) {
      java.util.List<Node> features = ((Features)node).features();
      for (int i = 0; i < features.size(); i++) {
        visit.visit(features.get(i));
      }
    } else if (node instanceof Feature) {
      visit.visit(((Feature)node).property());
      visit.visit(((Feature)node).value());
    } else if (node instanceof Rule) {
      visit.visit(((Rule)node).property());
      visit.visit(((Rule)node).value());
    } else if (node instanceof Definition) {
      visit.visit(((Definition)node).value());
    } else if (node instanceof Directive) {
      visit.visit(((Directive)node).value());
    } else if (node instanceof Expression) {
      java.util.List<Node> values = ((Expression)node).values();
      for (int i = 0; i < values.size(); i++) {
        visit.visit(values.get(i));
      }
    } else if (node instanceof ExpressionList) {
      java.util.List<Node> values = ((ExpressionList)node).expressions();
      for (int i = 0; i < values.size(); i++) {
        visit.visit(values.get(i));
      }
    } else if (node instanceof Quoted) {
      java.util.List<Node> parts = ((Quoted)node).parts();
      if (parts != null) {
        for (int i = 0; i < parts.size(); i++) {
          visit.visit(parts.get(i));
        }
      }
    } else if (node instanceof FunctionCall) {
      java.util.List<Node> args = ((FunctionCall)node).args();
      for (int i = 0; i < args.size(); i++) {
        visit.visit(args.get(i));
      }
    } else if (node instanceof Paren) {
      visit.visit(((Paren)node).value());
    } else if (node instanceof Operation) {
      visit.visit(((Operation)node).left());
      visit.visit(((Operation)node).right());
    } else if (node instanceof Shorthand) {
      visit.visit(((Shorthand)node).left());
      visit.visit(((Shorthand)node).right());
    } else if (node instanceof Alpha) {
      visit.visit(((Alpha)node).value());
    } else if (node instanceof Assignment) {
      visit.visit(((Assignment)node).value());
    } else if (node instanceof Url) {
      visit.visit(((Url)node).value());
    }
  }

  /** Functional child visitor (avoids pulling java.util.function imports). */
  private interface ChildVisitor {
    void visit(Node node);
  }

  /**
   * Finds the first node of the given type reachable from {@code root}.
   */
  private static <T extends Node> T findFirst(Node root, Class<T> type) {
    Deque<Node> stack = new ArrayDeque<>();
    stack.push(root);
    while (!stack.isEmpty()) {
      Node node = stack.pop();
      if (node == null) {
        continue;
      }
      if (type.isInstance(node)) {
        return type.cast(node);
      }
      forChildren(node, child -> {
        if (child != null) {
          stack.push(child);
        }
      });
    }
    throw new AssertionError("no " + type.getSimpleName() + " found in tree");
  }

  /** Count of structurally immutable nodes shared between source and copy. */
  private static int sharedLeafCount = 0;

  /**
   * Walks {@code src} and {@code dst} in lockstep: the classes must match
   * at every position, and no mutable node may be shared between the two
   * trees. Structural lists must be the same length, so no node can be
   * dropped by the copy.
   */
  private static void assertWalk(Node src, Node dst, String path) {
    if (src == null) {
      assertNull(dst, "null at " + path + " in source must be null in copy");
      return;
    }
    assertNotNull(dst, "missing node at " + path);
    assertEquals(dst.getClass(), src.getClass(), "class mismatch at " + path);
    if (isShareable(src)) {
      if (src == dst) {
        sharedLeafCount++;
      }
      return; // shared immutable leaf: nothing below to walk
    }
    assertNotSame(src, dst,
        "mutable node shared between source and copy at " + path
            + ": " + src.getClass().getName());

    if (src instanceof Block) {
      Block sb = (Block)src;
      Block db = (Block)dst;
      assertEquals(db.rules().size(), sb.rules().size(), "block size at " + path);
      for (int i = 0; i < sb.rules().size(); i++) {
        assertWalk(sb.rules().get(i), db.rules().get(i), path + ".rules[" + i + "]");
      }
      assertWalk(sb.charset(), db.charset(), path + ".charset");
    } else if (src instanceof BlockNode) {
      assertWalk(((BlockNode)src).block(), ((BlockNode)dst).block(), path + ".block");
    } else if (src instanceof Mixin) {
      Mixin sm = (Mixin)src;
      Mixin dm = (Mixin)dst;
      assertWalk(sm.params(), dm.params(), path + ".params");
      assertWalk(sm.guard(), dm.guard(), path + ".guard");
    } else if (src instanceof Media) {
      assertWalk(((Media)src).features(), ((Media)dst).features(), path + ".features");
    } else if (src instanceof Import) {
      Import si = (Import)src;
      Import di = (Import)dst;
      assertWalk(si.path(), di.path(), path + ".path");
      assertWalk(si.features(), di.features(), path + ".features");
    } else if (src instanceof ImportMarker) {
      assertWalk(((ImportMarker)src).importStatement(),
          ((ImportMarker)dst).importStatement(), path + ".import");
    } else if (src instanceof MixinMarker) {
      MixinMarker smk = (MixinMarker)src;
      MixinMarker dmk = (MixinMarker)dst;
      assertWalk(smk.mixinCall(), dmk.mixinCall(), path + ".call");
      assertWalk(smk.mixinDefinition(), dmk.mixinDefinition(), path + ".definition");
    } else if (src instanceof MixinCall) {
      MixinCall sc = (MixinCall)src;
      MixinCall dc = (MixinCall)dst;
      assertWalk(sc.selector(), dc.selector(), path + ".selector");
      assertWalk(sc.args(), dc.args(), path + ".args");
    } else if (src instanceof Selectors) {
      assertNodeList(((Selectors)src).selectors(), ((Selectors)dst).selectors(), path);
    } else if (src instanceof Selector) {
      assertNodeList(((Selector)src).elements(), ((Selector)dst).elements(), path);
    } else if (src instanceof ValueElement) {
      assertWalk(((ValueElement)src).value(), ((ValueElement)dst).value(), path + ".value");
    } else if (src instanceof AttributeElement) {
      assertNodeList(((AttributeElement)src).parts(), ((AttributeElement)dst).parts(), path);
    } else if (src instanceof MixinParams) {
      assertNodeList(((MixinParams)src).params(), ((MixinParams)dst).params(), path);
    } else if (src instanceof Parameter) {
      assertWalk(((Parameter)src).value(), ((Parameter)dst).value(), path + ".value");
    } else if (src instanceof MixinCallArgs) {
      assertNodeList(((MixinCallArgs)src).args(), ((MixinCallArgs)dst).args(), path);
    } else if (src instanceof Argument) {
      assertWalk(((Argument)src).value(), ((Argument)dst).value(), path + ".value");
    } else if (src instanceof Guard) {
      assertNodeList(((Guard)src).conditions(), ((Guard)dst).conditions(), path);
    } else if (src instanceof Condition) {
      assertWalk(((Condition)src).left(), ((Condition)dst).left(), path + ".left");
      assertWalk(((Condition)src).right(), ((Condition)dst).right(), path + ".right");
    } else if (src instanceof Features) {
      assertNodeList(((Features)src).features(), ((Features)dst).features(), path);
    } else if (src instanceof Feature) {
      assertWalk(((Feature)src).property(), ((Feature)dst).property(), path + ".property");
      assertWalk(((Feature)src).value(), ((Feature)dst).value(), path + ".value");
    } else if (src instanceof Rule) {
      assertWalk(((Rule)src).property(), ((Rule)dst).property(), path + ".property");
      assertWalk(((Rule)src).value(), ((Rule)dst).value(), path + ".value");
    } else if (src instanceof Definition) {
      assertWalk(((Definition)src).value(), ((Definition)dst).value(), path + ".value");
    } else if (src instanceof Directive) {
      assertWalk(((Directive)src).value(), ((Directive)dst).value(), path + ".value");
    } else if (src instanceof Expression) {
      assertNodeList(((Expression)src).values(), ((Expression)dst).values(), path);
    } else if (src instanceof ExpressionList) {
      assertNodeList(((ExpressionList)src).expressions(), ((ExpressionList)dst).expressions(), path);
    } else if (src instanceof Quoted) {
      java.util.List<Node> sp = ((Quoted)src).parts();
      java.util.List<Node> dp = ((Quoted)dst).parts();
      assertNodeList(sp, dp, path);
    } else if (src instanceof FunctionCall) {
      assertNodeList(((FunctionCall)src).args(), ((FunctionCall)dst).args(), path);
    } else if (src instanceof Paren) {
      assertWalk(((Paren)src).value(), ((Paren)dst).value(), path + ".value");
    } else if (src instanceof Operation) {
      assertWalk(((Operation)src).left(), ((Operation)dst).left(), path + ".left");
      assertWalk(((Operation)src).right(), ((Operation)dst).right(), path + ".right");
    } else if (src instanceof Shorthand) {
      assertWalk(((Shorthand)src).left(), ((Shorthand)dst).left(), path + ".left");
      assertWalk(((Shorthand)src).right(), ((Shorthand)dst).right(), path + ".right");
    } else if (src instanceof Alpha) {
      assertWalk(((Alpha)src).value(), ((Alpha)dst).value(), path + ".value");
    } else if (src instanceof Assignment) {
      assertWalk(((Assignment)src).value(), ((Assignment)dst).value(), path + ".value");
    } else if (src instanceof Url) {
      assertWalk(((Url)src).value(), ((Url)dst).value(), path + ".value");
    }
    // Other node types are structurally immutable leaves (shared or not),
    // with nothing further to walk.
  }

  private static void assertNodeList(java.util.List<? extends Node> sl,
      java.util.List<? extends Node> dl, String path) {
    if (sl == null) {
      assertNull(dl, path);
      return;
    }
    assertNotNull(dl, path);
    assertEquals(dl.size(), sl.size(), "list size at " + path);
    for (int i = 0; i < sl.size(); i++) {
      assertWalk(sl.get(i), dl.get(i), path + "[" + i + "]");
    }
  }

  private static Stylesheet parseRich() throws LessException {
    return parse(IMPORT_SOURCE, importFiles());
  }

  private static Stylesheet parse(String source, Map<Path, String> files) throws LessException {
    LessOptions opts = new LessOptions();
    opts.compress(true);
    LessContext ctx = new LessContext(opts, new HashMapLessLoader(files));
    ctx.setCompiler(COMPILER);
    return COMPILER.parse(source, ctx, Paths.get("."), null);
  }

  /**
   * A deep copy of a parsed tree must be content-identical to an
   * independent fresh parse of the same source.
   */
  @Test
  public void testDeepCopyMatchesFreshParse() throws LessException {
    Stylesheet a = parseRich();
    Stylesheet fresh = parseRich();
    Stylesheet copy = a.deepCopy();

    assertNotSame(a, copy);
    assertNotSame(a.block(), copy.block());
    assertEquals(copy.toString(), fresh.toString(),
        "deep copy must reproduce the parse-time node graph exactly");
  }

  /**
   * The deep copy must share no mutable node with the source: walking the
   * two trees in lockstep, every non-leaf position must be a distinct
   * instance of the same class, and every list must have the same length
   * (no node may be dropped). Structurally immutable leaves may be shared.
   */
  @Test
  public void testDeepCopySharesNoMutableNodes() throws LessException {
    Stylesheet a = parseRich();
    Stylesheet copy = a.deepCopy();

    sharedLeafCount = 0;
    assertWalk(a, copy, "root");
    // Sanity: the soup must actually contain shareable leaves, so this
    // test cannot pass vacuously by deep-copying everything.
    assertTrue(sharedLeafCount > 0,
        "expected some structurally immutable shared leaves, saw " + sharedLeafCount);
  }

  /**
   * Mutating the deep copy's evaluation-time state must never be visible
   * on the source tree. This is the exact failure mode that corrupted a
   * shared import cache: Definition circular-reference flags, Quoted
   * escape flags, Import suppression, Ruleset enter/exit, Mixin
   * entry counts, and Block splices and charset.
   */
  @Test
  public void testDeepCopyIsolatesMutation() throws LessException {
    Stylesheet a = parseRich();
    Stylesheet copy = a.deepCopy();
    String before = a.toString();

    // Splice into the copy's top block the way mixin expansion does.
    copy.block().appendNode(rule(prop("injected"), expn(kwd("nope"))));

    Definition def = findFirst(a, Definition.class);
    Definition copyDef = findFirst(copy, Definition.class);
    assertNotSame(def, copyDef);
    copyDef.evaluating(true);
    assertFalse(def.evaluating(), "source definition must stay un-evaluated");

    Quoted quoted = findFirst(a, Quoted.class);
    Quoted copyQuoted = findFirst(copy, Quoted.class);
    assertNotSame(quoted, copyQuoted);
    copyQuoted.setEscape(true);
    assertFalse(quoted.escaped(), "source quoted string must stay un-escaped");

    Import imp = findFirst(a, Import.class);
    Import copyImp = findFirst(copy, Import.class);
    assertNotSame(imp, copyImp);
    copyImp.suppress(true);
    assertFalse(imp.suppress(), "source import must stay unsuppressed");

    Ruleset ruleset = findFirst(a, Ruleset.class);
    Ruleset copyRuleset = findFirst(copy, Ruleset.class);
    assertNotSame(ruleset, copyRuleset);
    copyRuleset.enter();
    assertFalse(ruleset.evaluating(), "source ruleset must stay un-entered");

    Mixin mixin = findFirst(a, Mixin.class);
    Mixin copyMixin = findFirst(copy, Mixin.class);
    assertNotSame(mixin, copyMixin);
    assertNull(copyMixin.closure(), "copy closure must start unset");
    copyMixin.enter();
    copyMixin.exit();
    assertEquals(mixin.entryCount(), 0, "source entry count must stay zero");

    copy.block().charset((Directive)dir("@charset", quoted('"', false, anon("x"))));
    assertNull(a.block().charset(), "source block must stay charset-free");

    assertEquals(a.toString(), before, "source tree must be unchanged");
  }

  /**
   * Performance contract: deepCopy is a pure object-graph walk and must be
   * much cheaper than re-parsing the same source. Bound is deliberately
   * loose (3x) to avoid CI flakes. Parse is 10x+ slower in practice.
   */
  @Test
  public void testDeepCopyCheaperThanParse() throws LessException {
    StringBuilder sb = new StringBuilder(RICH_SOURCE.length() * 40);
    for (int i = 0; i < 40; i++) {
      sb.append(RICH_SOURCE);
    }
    final String big = sb.toString();

    LessOptions opts = new LessOptions();
    opts.compress(true);
    LessContext ctx = new LessContext(opts, new HashMapLessLoader(importFiles()));
    ctx.setCompiler(COMPILER);

    // Warmup for JIT.
    COMPILER.parse(big, ctx, Paths.get("."), null);
    Stylesheet warm = COMPILER.parse(big, ctx, Paths.get("."), null);
    warm.deepCopy();

    long parseTime = System.nanoTime();
    for (int i = 0; i < 5; i++) {
      COMPILER.parse(big, ctx, Paths.get("."), null);
    }
    parseTime = System.nanoTime() - parseTime;

    Stylesheet parsed = COMPILER.parse(big, ctx, Paths.get("."), null);
    long copyTime = System.nanoTime();
    for (int i = 0; i < 5; i++) {
      parsed.deepCopy();
    }
    copyTime = System.nanoTime() - copyTime;

    assertTrue(copyTime * 3 < parseTime,
        "deepCopy (" + copyTime / 1_000_000 + "us) must be << parse ("
            + parseTime / 1_000_000 + "us)");
  }

}
