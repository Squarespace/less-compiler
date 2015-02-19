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

import com.squarespace.less.LessErrorInfo;
import com.squarespace.less.LessErrorType;
import com.squarespace.less.SyntaxErrorType;


/**
 * Builds error messages for parse phase of the compile, implemented as
 * static methods for concision.
 */
public class SyntaxErrorMaker {

  private SyntaxErrorMaker() {
  }

  public static LessErrorInfo alphaUnitsInvalid(Object arg) {
    return error(SyntaxErrorType.ALPHA_UNITS_INVALID).arg0(arg);
  }

  public static LessErrorInfo bareVariable(Object name) {
    return error(SyntaxErrorType.BARE_VARIABLE).arg0(name);
  }

  public static LessErrorInfo expected(Object thing) {
    return error(SyntaxErrorType.EXPECTED).arg0(thing);
  }

  public static LessErrorInfo general(Object obj) {
    return error(SyntaxErrorType.GENERAL).arg0(obj);
  }

  public static LessErrorInfo importError(Object path, Object msg) {
    return error(SyntaxErrorType.IMPORT_ERROR).arg0(path).arg1(msg);
  }

  public static LessErrorInfo incompleteParse() {
    return error(SyntaxErrorType.INCOMPLETE_PARSE);
  }

  public static LessErrorInfo javascriptDisabled() {
    return error(SyntaxErrorType.JAVASCRIPT_DISABLED);
  }

  public static LessErrorInfo mixedDelimiters() {
    return error(SyntaxErrorType.MIXED_DELIMITERS);
  }

  public static LessErrorInfo quotedBareLF() {
    return error(SyntaxErrorType.QUOTED_BARE_LF);
  }

  public static LessErrorInfo selectorOneGuard() {
    return error(SyntaxErrorType.SELECTOR_ONE_GUARD);
  }

  public static LessErrorInfo selectorEndGuard() {
    return error(SyntaxErrorType.SELECTOR_END_GUARD);
  }

  private static LessErrorInfo error(LessErrorType type) {
    LessErrorInfo info = new LessErrorInfo(type);
    info.code(type);
    return info;
  }

}
