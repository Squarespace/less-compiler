package com.squarespace.v6.template.less.parse;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.model.Node;


public class Parselets {

  public static final Parselet ADDITION = new AdditionParselet();
  
  public static final Parselet ALPHA = new AlphaParselet();
  
  public static final Parselet ASSIGNMENT = new AssignmentParselet();

  public static final Parselet BLOCK = new BlockParselet();
  
  public static final Parselet COLOR = new ColorParselet();
  
  public static final Parselet COLOR_KEYWORD = new ColorKeywordParselet();
  
  public static final Parselet COMMENT = new CommentParselet();
  
  public static final Parselet COMMENT_RULE = new CommentRuleParselet();
  
  public static final Parselet CONDITION = new ConditionParselet();
  
  public static final Parselet GUARD = new GuardParselet();
  
  public static final Parselet DIMENSION = new DimensionParselet();
  
  public static final Parselet DIRECTIVE = new DirectiveParselet();
  
  public static final Parselet ELEMENT = new ElementParselet();
  
  public static final Parselet ENTITY = new Parselet() {
    @Override
    public Node parse(LessStream stm) throws LessException {
      return stm.parse(LITERAL, VARIABLE, FUNCTION_CALL, KEYWORD, JAVASCRIPT, COMMENT);
    }
  };
  
  public static final Parselet EXPRESSION = new ExpressionParselet();

  public static final Parselet EXPRESSION_LIST = new ExpressionListParselet();

  public static final Parselet FEATURE = new FeatureParselet();
  
  public static final Parselet FEATURES = new FeaturesParselet();
  
  public static final Parselet FONT = new FontParselet();

  public static final Parselet FUNCTION_CALL = new FunctionCallParselet();
  
  public static final Parselet JAVASCRIPT = new JavascriptParselet();
  
  public static final Parselet LITERAL = new Parselet() {
    @Override
    public Node parse(LessStream stm) throws LessException {
      return stm.parse(RATIO, DIMENSION, COLOR, QUOTED, UNICODE_RANGE);
    }
  };
  
  public static final Parselet MIXIN_CALL = new MixinCallParselet();
  
  public static final Parselet MIXIN_CALL_ARGS = new MixinCallArgsParselet();
  
  public static final Parselet MIXIN = new MixinParselet();
  
  public static final Parselet MIXIN_PARAMS = new MixinParamsParselet();
  
  public static final Parselet MULTIPLICATION = new MultiplicationParselet();

  public static final Parselet KEYWORD = new KeywordParselet();
  
  public static final Parselet OPERAND = new OperandParselet();

  public static final Parselet PRIMARY = new PrimaryParselet();

  public static final Parselet PROPERTY = new PropertyParselet();
  
  public static final Parselet QUOTED = new QuotedParselet();
  
  public static final Parselet RATIO = new RatioParselet();
  
  public static final Parselet RULE = new RuleParselet();
  
  public static final Parselet RULESET = new RulesetParselet();
  
  public static final Parselet SELECTOR = new SelectorParselet();
  
  public static final Parselet SELECTORS = new SelectorsParselet();
  
  public static final Parselet SHORTHAND = new ShorthandParselet();

  public static final Parselet STYLESHEET = new StylesheetParselet();
  
  public static final Parselet SUB = new SubParselet();
  
  public static final Parselet UNICODE_RANGE = new UnicodeRangeParselet();
  
  public static final Parselet URL = new UrlParselet();
  
  public static final Parselet VARIABLE = new VariableParselet();
  
  public static final Parselet VARIABLE_CURLY = new VariableCurlyParselet();
}
