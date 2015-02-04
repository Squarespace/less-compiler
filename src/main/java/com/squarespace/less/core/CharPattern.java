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

package com.squarespace.less.core;


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

  /**
   * Returns the number of characters in the pattern.
   */
  public int length() {
    return chars.length;
  }

}
