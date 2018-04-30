package com.squarespace.less;

import org.testng.annotations.Test;

public class AstEmitterTest {

  private final AstTestCaseRunner runner = new AstTestCaseRunner(AstEmitterTest.class);

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

}