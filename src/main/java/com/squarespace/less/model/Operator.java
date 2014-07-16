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
  NOT_EQUAL ("<>", "not equal to"),
  OR ("or", "or"),
  SUBTRACT ("-", "subtract");

  private final String repr;

  private final String humanRepr;

  private Operator(String repr, String humanRepr) {
    this.repr = repr;
    this.humanRepr = humanRepr;
  }

  public String humanRepr() {
    return humanRepr;
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
      default:
        break;
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

      default:
        break;
    }
    return null;
  }

}
