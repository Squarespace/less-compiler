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

package com.squarespace.less;

import static com.squarespace.less.core.Constants.NULL_PLACEHOLDER;

import java.util.Map;

import com.squarespace.less.core.MapFormat;


/**
 * Errors thrown during the parse phase of the compile are syntax errors.
 */
public enum SyntaxErrorType implements ErrorType {

  ALPHA_UNITS_INVALID
  ("Numeric values for alpha cannot have units. Found %(arg0)s"),

  EXPECTED
  ("Expected %(arg0)s"),

  GENERAL
  ("%(arg0)s"),

  INCOMPLETE_PARSE
  ("Unable to complete parse."),

  JAVASCRIPT_DISABLED
  ("This version of LESS lacks Javascript support."),

  MIXED_DELIMITERS
  ("Cannot mix semicolon and comma as delimiters"),

  QUOTED_BARE_LF
  ("Quoted string contains a bare line feed");

  private static final String PREFIX = "SyntaxError %(code)s ";

  private MapFormat prefixFormat;

  private MapFormat messageFormat;

  private SyntaxErrorType(String rawFormat) {
    this.prefixFormat = new MapFormat(PREFIX, NULL_PLACEHOLDER);
    this.messageFormat = new MapFormat(rawFormat, NULL_PLACEHOLDER);
  }

  public String prefix(Map<String, Object> params) {
    return prefixFormat.apply(params);
  }

  public String message(Map<String, Object> params) {
    return messageFormat.apply(params);
  }

}
