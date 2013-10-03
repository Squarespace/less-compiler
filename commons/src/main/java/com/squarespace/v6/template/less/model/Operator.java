package com.squarespace.v6.template.less.model;


public enum Operator {

  ADD ("+"),
  AND ("and"),
  DIVIDE ("/"),
  EQUAL ("=="),
  GREATER_THAN (">"),
  GREATER_THAN_OR_EQUAL (">="),
  LESS_THAN ("<"),
  LESS_THAN_OR_EQUAL ("<="),
  MULTIPLY ("*"),
  NOT_EQUAL ("<>"),
  OR ("or"),
  SUBTRACT ("-")
  ;

  private String repr;
  
  private Operator(String repr) {
    this.repr = repr;
  }
  
  public String toString() {
    return repr;
  }
  
  public static Operator fromChar(char ch) {
    switch (ch) {
      case '+': return ADD;
      case '/': return DIVIDE;
      case '=': return EQUAL;
      case '>': return GREATER_THAN;
      case '<': return LESS_THAN;
      case '*': return MULTIPLY;
      case '-': return SUBTRACT;
    }
    return null;
  }

  public static Operator fromString(String str) {
    if (str.length() == 1) {
      return fromChar(str.charAt(0));
    }

    switch (str) {
      case "==":
        return EQUAL;

      case ">=":
      case "=>":
        return GREATER_THAN_OR_EQUAL;

      case "<=":
      case "=<":
        return LESS_THAN_OR_EQUAL;
        
      case "!=":
      case "<>":
        return NOT_EQUAL;
    }
    return null;
  }
  
}
