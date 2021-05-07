/**
 * Copyright (c) 2018 SQUARESPACE, Inc.
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

import java.io.InputStream;

import org.testng.annotations.Test;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.ParseException;
import com.squarespace.less.core.FlexList;
import com.squarespace.less.core.LessUtils;
import com.squarespace.less.exec.LessSuiteBase;
import com.squarespace.less.jsonast.AstEmitter;
import com.squarespace.less.model.BlockNode;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.NodeType;
import com.squarespace.less.model.Stylesheet;

public class AstEmitterTest {

  private final AstTestCaseRunner runner = new AstTestCaseRunner(AstEmitterTest.class);
  private final LessCompiler compiler = new LessCompiler();

  @Test
  public void testNullify() throws Exception {
    LessOptions opts = new LessOptions();
    LessContext ctx = new LessContext(opts);
    Stylesheet sheet = compiler.parse(read("generic.less"), ctx);
    nullify(sheet);

    String actual = sheet.repr();
    String expected = read("generic-repr.less");
    String diff = LessSuiteBase.diff(expected, actual);
    if (!actual.equals(expected)) {
      throw new AssertionError("Found differences in repr:\n" + diff);
    }

    actual = compiler.render(sheet, ctx);
    expected = read("generic.css");
    diff = LessSuiteBase.diff(expected, actual);
    if (!actual.equals(expected)) {
      throw new AssertionError("Found differences in css:\n" + diff);
    }

    actual = AstEmitter.render(sheet);
    try {
      Json.parse(actual);
    } catch (ParseException e) {
      throw new AssertionError(e);
    }
  }

  private String read(String path) throws Exception {
    try (InputStream stream = getClass().getResourceAsStream(path)) {
      return LessUtils.readStream(stream);
    }
  }

  private void nullify(BlockNode blockNode) {
    FlexList<Node> rules = blockNode.block().rules();
    for (int i = 0; i < rules.size(); i++) {
      Node rule = rules.get(i);
      if (rule.type() == NodeType.COMMENT) {
        rules.set(i, null);
      } else if (rule instanceof BlockNode) {
        nullify((BlockNode)rule);
      }
    }
  }

  @Test
  public void testAddition() throws Exception {
    runner.run("ast-addition.txt");
  }

  @Test
  public void testAlpha() throws Exception {
    runner.run("ast-alpha.txt");
  }

  @Test
  public void testArgument() throws Exception {
    runner.run("ast-argument.txt");
  }

  @Test
  public void testColor() throws Exception {
    runner.run("ast-color.txt");
  }

  @Test
  public void testColorKeyword() throws Exception {
    runner.run("ast-color-keyword.txt");
  }

  @Test
  public void testComment() throws Exception {
    runner.run("ast-comment.txt");
  }

  @Test
  public void testCommentRule() throws Exception {
    runner.run("ast-comment-rule.txt");
  }

  @Test
  public void testCondition() throws Exception {
    runner.run("ast-condition.txt");
  }

  @Test
  public void testDefinition() throws Exception {
    runner.run("ast-definition.txt");
  }

  @Test
  public void testDimension() throws Exception {
    runner.run("ast-dimension.txt");
  }

  @Test
  public void testDirective() throws Exception {
    runner.run("ast-directive.txt");
  }

  @Test
  public void testFunctionCall() throws Exception {
    runner.run("ast-function-call.txt");
  }

  @Test
  public void testGuard() throws Exception {
    runner.run("ast-guard.txt");
  }

  @Test
  public void testImport() throws Exception {
    runner.run("ast-import.txt");
  }

  @Test
  public void testMedia() throws Exception {
    runner.run("ast-media.txt");
  }

  @Test
  public void testMixin() throws Exception {
    runner.run("ast-mixin.txt");
  }

  @Test
  public void testMixinCall() throws Exception {
    runner.run("ast-mixin-call.txt");
  }

  @Test
  public void testRatio() throws Exception {
    runner.run("ast-ratio.txt");
  }

  @Test
  public void testRule() throws Exception {
    runner.run("ast-rule.txt");
  }

  @Test
  public void testRuleset() throws Exception {
    runner.run("ast-ruleset.txt");
  }

  @Test
  public void testSelectors() throws Exception {
    runner.run("ast-selector.txt");
  }

  @Test
  public void testShorthand() throws Exception {
    runner.run("ast-shorthand.txt");
  }

  @Test
  public void testStylesheet() throws Exception {
    runner.run("ast-stylesheet.txt");
  }

  @Test
  public void testUnicodeRange() throws Exception {
    runner.run("ast-unicode-range.txt");
  }

  @Test
  public void testVariables() throws Exception {
    runner.run("ast-variables.txt");
  }

  @Test
  public void testWhitespace() throws Exception {
    runner.run("ast-whitespace.txt");
  }

}
