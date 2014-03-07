package com.squarespace.less.model;


/**
 * Represents the set of known CSS combinators.
 */
public enum Combinator {

  CHILD('>'),
  DESC(' '),
  NAMESPACE('|'),
  SIB_ADJ('+'),
  SIB_GEN('~')
  ;

  private final char ch;

  private Combinator(char ch) {
    this.ch = ch;
  }

  public char getChar() {
    return ch;
  }

  public static Combinator fromChar(char ch) {
    switch (ch) {
      case '>':
        return CHILD;
      case ' ':
        return DESC;
      case '|':
        return NAMESPACE;
      case '+':
        return SIB_ADJ;
      case '~':
        return SIB_GEN;
    }
    return null;
  }

}
