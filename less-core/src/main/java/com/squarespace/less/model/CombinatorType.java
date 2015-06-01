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
 * Represents the set of known CSS combinators.
 */
public enum CombinatorType {

  CHILD('>', "Child"),
  DESC(' ', "Descendant"),
  NAMESPACE('|', "Namespace"),
  SIB_ADJ('+', "Adjacent sibling"),
  SIB_GEN('~', "General sibling");

  private final char repr;

  private final String fullName;

  private CombinatorType(char ch, String fullName) {
    this.repr = ch;
    this.fullName = fullName;
  }

  public char repr() {
    return repr;
  }

  public String fullName() {
    return fullName;
  }

  public static CombinatorType fromChar(char ch) {
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
      default:
        break;
    }
    return null;
  }

}
