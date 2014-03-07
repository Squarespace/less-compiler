package com.squarespace.less.model;


/**
 * A NodeType can refer to a specific type (COLOR) or an abstract type (ENTITY).
 * It is used for fast identification of nodes, registration of node parsers, etc.
 */
public enum NodeType {

  ALPHA,
  ANONYMOUS,
  ARGUMENT,
  ASSIGNMENT,
  BLOCK,
  BLOCK_DIRECTIVE,
  FUNCTION_CALL,
  COLOR,
  COMMENT,
  CONDITION,
  GENERIC_BLOCK,
  GUARD,
  DEFINITION,
  DIMENSION,
  DIRECTIVE,
  ELEMENT,
  EXPRESSION,
  FALSE,
  FEATURES,
  IMPORT,
  IMPORT_MARKER,
  KEYWORD,
  MEDIA,
  MIXIN,
  MIXIN_ARGS,
  MIXIN_CALL,
  MIXIN_MARKER,
  MIXIN_PARAMS,
  OPERATION,
  PARAMETER,
  PAREN,
  PARSE_ERROR,
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
  VARIABLE

  ;

}
