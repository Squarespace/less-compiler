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

package com.squarespace.less.parse;


public class Parselets {

  public static final Parselet[] ADDITION = new Parselet[] {
    new AdditionParselet()
  };

  public static final Parselet[] ALPHA = new Parselet[] {
    new AlphaParselet()
  };

  public static final Parselet[] ALPHA_SUB = new Parselet[] {
    new DimensionParselet(),
    new VariableParselet()
  };

  public static final Parselet[] ASSIGNMENT = new Parselet[] {
    new AssignmentParselet()
  };

  public static final Parselet[] BLOCK = new Parselet[] {
    new BlockParselet()
  };

  public static final Parselet[] COLOR = new Parselet[] {
    new ColorParselet()
  };

  public static final Parselet[] COLOR_KEYWORD = new Parselet[] {
    new ColorKeywordParselet()
  };

  public static final Parselet[] COMMENT = new Parselet[] {
    new CommentParselet()
  };

  public static final Parselet[] CONDITION = new Parselet[] {
    new ConditionParselet()
  };

  public static final Parselet[] CONDITION_SUB = new Parselet[] {
    new AdditionParselet(),
    new KeywordParselet(),
    new QuotedParselet()
  };

  public static final Parselet[] DIMENSION = new Parselet[] {
    new DimensionParselet()
  };

  public static final Parselet[] DIRECTIVE = new Parselet[] {
    new DirectiveParselet()
  };

  public static final Parselet[] DIRECTIVE_IMPORT = new Parselet[] {
    new QuotedParselet(),
    new UrlParselet()
  };

  public static final Parselet[] ELEMENT = new Parselet[] {
    new ElementParselet()
  };

  public static final Parselet[] ELEMENT_SUB = new Parselet[] {
    new VariableCurlyParselet(),
    new VariableParselet(),
    new SelectorParselet()
  };

  public static final Parselet[] LITERAL = new Parselet[] {
    new RatioParselet(),
    new DimensionParselet(),
    new ColorParselet(),
    new QuotedParselet(),
    new UnicodeRangeParselet()
  };

  public static final Parselet[] EXPRESSION = new Parselet[] {
    new ExpressionParselet()
  };

  public static final Parselet[] EXPRESSION_LIST = new Parselet[] {
    new ExpressionListParselet()
  };

  public static final Parselet[] FEATURE = new Parselet[] {
    new FeatureParselet()
  };

  public static final Parselet[] FEATURES = new Parselet[] {
    new FeaturesParselet()
  };

  public static final Parselet[] FONT = new Parselet[] {
    new FontParselet()
  };

  public static final Parselet[] FUNCTION_CALL = new Parselet[] {
    new FunctionCallParselet()
  };

  public static final Parselet[] FUNCTION_CALL_SUB = new Parselet[] {
    new QuotedParselet(),
    new VariableParselet()
  };

  public static final Parselet[] GUARD = new Parselet[] {
    new GuardParselet()
  };

  public static final Parselet[] KEYWORD = new Parselet[] {
    new KeywordParselet()
  };

  public static final Parselet[] MIXIN_CALL = new Parselet[] {
    new MixinCallParselet()
  };

  public static final Parselet[] MIXIN_CALL_ARGS = new Parselet[] {
    new MixinCallArgsParselet()
  };

  public static final Parselet[] MIXIN_PARAMS = new Parselet[] {
    new MixinParamsParselet()
  };

  public static final Parselet[] MULTIPLICATION = new Parselet[] {
    new MultiplicationParselet()
  };

  public static final Parselet[] OPERAND = new Parselet[] {
    new OperandParselet()
  };

  public static final Parselet[] OPERAND_SUB = new Parselet[] {
    new SubParselet(),
    new DimensionParselet(),
    new FunctionCallParselet(),
    new ColorKeywordParselet(),
    new ColorParselet(),
    new VariableParselet()
  };

  public static final Parselet[] PRIMARY = new Parselet[] {
    new PrimaryParselet()
  };

  public static final Parselet[] PRIMARY_SUB = new Parselet[] {
    new MixinParselet(),
    new RuleParselet(),
    new RulesetParselet(),
    new CommentRuleParselet(),
    new MixinCallParselet(),
    new RulesetCallParselet(),
    new DirectiveParselet()
  };

  public static final Parselet[] PROPERTY = new Parselet[] {
    new PropertyParselet()
  };

  public static final Parselet[] QUOTED = new Parselet[] {
    new QuotedParselet()
  };

  public static final Parselet[] RATIO = new Parselet[] {
    new RatioParselet()
  };

  public static final Parselet[] RULE = new Parselet[] {
    new RuleParselet()
  };

  public static final Parselet[] RULE_KEY = new Parselet[] {
    new PropertyParselet(),
    new VariableParselet()
  };

  public static final Parselet[] RULESET = new Parselet[] {
    new RulesetParselet()
  };

  public static final Parselet[] SELECTOR = new Parselet[] {
    new SelectorParselet()
  };

  public static final Parselet[] SELECTORS = new Parselet[] {
    new SelectorsParselet()
  };

  public static final Parselet[] SHORTHAND = new Parselet[] {
    new ShorthandParselet()
  };

  public static final Parselet[] STYLESHEET = new Parselet[] {
    new StylesheetParselet()
  };

  public static final Parselet[] UNICODE_RANGE = new Parselet[] {
    new UnicodeRangeParselet()
  };

  public static final Parselet[] VARIABLE = new Parselet[] {
    new VariableParselet()
  };

  public static final Parselet[] VARIABLE_CURLY = new Parselet[] {
    new VariableCurlyParselet()
  };


  // Composed of other Parselet[]

  public static final Parselet[] ENTITY = new ParseletBuilder()
    .add(LITERAL)
    .add(
        new VariableParselet(),
        new FunctionCallParselet(),
        new KeywordParselet(),
        new JavascriptParselet(),
        new CommentParselet()
     ).build();

  public static final Parselet[] EXPRESSION_SUB = new ParseletBuilder()
    .add(
      new CommentParselet(),
      new AdditionParselet())
    .add(ENTITY)
    .build();

  public static final Parselet[] FONT_SUB = new ParseletBuilder()
    .add(new ShorthandParselet())
    .add(ENTITY)
    .build();

  public static final Parselet[] FUNCTION_CALL_ARGS = new ParseletBuilder()
    .add(new AssignmentParselet())
    .add(EXPRESSION)
    .build();

  public static final Parselet[] MIXIN_PARAMETER = new ParseletBuilder()
    .add(new VariableParselet())
    .add(LITERAL)
    .add(new KeywordParselet())
    .build();

};
