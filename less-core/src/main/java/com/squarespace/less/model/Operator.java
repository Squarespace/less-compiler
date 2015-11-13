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

package com.squarespace.less.model;


/**
 * Holds the set of all known operators. This includes both logical and mathematical.
 */
public enum Operator {

  ADD ("+", "add"),
  AND ("and", "and"),
  DIVIDE ("/", "divide"),
  EQUAL ("=", "equal to"),
  GREATER_THAN (">", "greater than"),
  GREATER_THAN_OR_EQUAL (">=", "greater than or equal to"),
  LESS_THAN ("<", "less than"),
  LESS_THAN_OR_EQUAL ("<=", "less than or equal to"),
  MULTIPLY ("*", "multiply"),
  NOT_EQUAL ("<>", "not equal to"), // Extension, not present in less.js 2.4
  OR ("or", "or"),
  SUBTRACT ("-", "subtract");

  /**
   * Shorthand representation for the operator.
   */
  private final String repr;

  /**
   * Longer name for the operator.
   */
  private final String humanRepr;

  /**
   * Constructs an operator with the given representations.
   */
  Operator(String repr, String humanRepr) {
    this.repr = repr;
    this.humanRepr = humanRepr;
  }

  /**
   * Returns the long name for the operator.
   */
  public String humanRepr() {
    return humanRepr;
  }

  /**
   * Returns the operator value associated with the given character.
   * Used when dealing with a single character.
   */
  public static Operator fromChar(char ch) {
    switch (ch) {
      case '+': return ADD;
      case '/': return DIVIDE;
      case '=': return EQUAL;
      case '>': return GREATER_THAN;
      case '<': return LESS_THAN;
      case '*': return MULTIPLY;
      case '-': return SUBTRACT;
      default:
        break;
    }
    return null;
  }

  /**
   * Returns the operator value associated with the given string.
   */
  public static Operator fromString(String str) {
    if (str.length() == 1) {
      return fromChar(str.charAt(0));
    }

    switch (str) {
      case "==":
        return EQUAL;

      case ">=":
//      case "=>": // this form not present in less.js 2.4. maybe artifact of ruby days?
        return GREATER_THAN_OR_EQUAL;

      case "<=":
      case "=<":
        return LESS_THAN_OR_EQUAL;

      // Extension: these forms not in less.js 2.4 but some users may prefer them to "not (a = b)".
//      case "!=":
//      case "<>":
//        return NOT_EQUAL;

      default:
        break;
    }
    return null;
  }

  @Override
  public String toString() {
    return repr;
  }

}
