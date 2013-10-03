package com.squarespace.v6.template.less.model;


/**
 * A NodeType can refer to a specific type (COLOR) or an abstract type (ENTITY).
 * It is used for fast identification of nodes, registration of node parsers, etc.
 */
public enum NodeType {

  ALPHA,
  ANONYMOUS,
  ARGUMENT,
  ASSIGNMENT,
  BINDINGS,
  BLOCK,
  BLOCK_DIRECTIVE,
  BLOCK_LIST,
  FUNCTION_CALL,
  COLOR,
  COMMENT,
  CONDITION,
  GUARD,
  DEFINITION,
  DIMENSION,
  DIRECTIVE,
  ELEMENT,
  ENTITY,
  EXPRESSION,
  FALSE,
  FEATURES,
  IMPORT,
  KEYWORD,
  MEDIA,
  MIXIN,
  MIXIN_ARGS,
  MIXIN_CALL,
  MIXIN_PARAMS,
  OPERAND,
  OPERATION,
  OPERATOR,
  PARAMETER,
  PAREN,
  PROPERTY,
  QUOTED,
  RATIO,
  RULE,
  RULESET,
  SELECTOR,
  SELECTORS,
  SHORTHAND,
  STYLESHEET,
  TRUE,
  UNICODE_RANGE,
  URL,
  EXPRESSION_LIST,
  VARIABLE,
  WHITESPACE
  ;
  
}
