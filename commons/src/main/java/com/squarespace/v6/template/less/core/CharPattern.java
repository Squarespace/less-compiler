package com.squarespace.v6.template.less.core;


/**
 * Given a character pattern, builds a Knuth-Morris-Pratt 'next' table, used to
 * efficiently locate the pattern within a longer string.
 */
public class CharPattern {
  
  private final char[] chars;
  
  private final int[] next;

  public CharPattern(char ... chars) {
    this.chars = chars;
    int len = chars.length;
    int j = -1;
    next = new int[len];
    for (int i = 0; i < len; i++) {
      if (i == 0) {
        next[i] = -1;
      } else if (chars[i] != chars[j]) {
        next[i] = j;
      } else {
        next[i] = next[j];
      }
      while (j >= 0 && chars[i] != chars[j]) {
        j = next[j];
      }
      j++;
    }
  }
  
  /**
   * Returns the character pattern.
   * @return
   */
  public char[] pattern() {
    return chars;
  }
 
  /**
   * Returns the 'next' table, used by the K-M-P algorithm.
   */
  public int[] next() {
    return next;
  }
  
  public int length() {
    return chars.length;
  }
  
}
