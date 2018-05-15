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

package com.squarespace.less.cli;

import com.squarespace.less.core.LessUtils;


/**
 * Enumerate flags telling the compiler to run using a particular debug mode.
 */
public enum LessDebugMode {

  CANONICALIZE
  ("Parses and emits the canonical representation of the parsed template."),

  EVAL
  ("Expands all IMPORT and MIXIN_CALL statements, and resolves all variable references, and emits "
      + "the canonicalized form of the expanded template"),

  EVALTREE
  ("Performs an EVAL and emits the pre-render syntax tree."),

  JSONAST
  ("Emits the JSON AST for the canonical representation of the parsed template"),

  JSONREPR
  ("Emits a pseudocode representation of the contents of a JSON AST for debugging / testing"),

  PARSETREE
  ("Parses the file and displays the internal syntax tree (potentially extremely verbose)");

  private String description;

  LessDebugMode(String desc) {
    this.description = desc;
  }

  public static LessDebugMode fromString(String str) {
    return valueOf(str.toUpperCase());
  }

  public static String modes() {
    return LessUtils.enumValueList(LessDebugMode.class, true);
  }

  public String description() {
    return description;
  }

}
