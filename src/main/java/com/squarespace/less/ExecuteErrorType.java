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
 * Definitions for the errors thrown in the execute phase of the compile.
 */
public enum ExecuteErrorType implements LessErrorType {

  ARG_COUNT
  ("Function %(name)s requires at least %(arg0)s args, found %(arg1)s "),

  ARG_TOO_MANY
  ("Too many arguments"),

  ARG_NAMED_NOTFOUND
  ("Named arg %(name)s not found"),

  BAD_COLOR_MATH
  ("A color cannot %(arg0)s %(arg1)s"),

  DIVIDE_BY_ZERO
  ("Attempt to divide %(arg0)s by zero."),

  EXPECTED_BOOLOP
  ("Expected a boolean operator but found %(arg0)s"),

  EXPECTED_MATHOP
  ("Expected a math operator but found %(arg0)s"),

  FORMAT_FUNCTION_ARGS
  ("Not enough args for format string. Format expects %(arg0)s arguments, but only found %(arg1)s"),

  FUNCTION_CALL
  ("Encountered an error calling function %(name)s: %(arg0)s"),

  GENERAL
  ("An execution error occurred: %(arg0)s"),

  INCOMPATIBLE_UNITS
  ("No conversion is possible from %(arg0)s to %(arg1)s"),

  INVALID_ARG
  ("Argument %(arg0)s must be %(arg1)s. Found %(arg2)s"),

  INVALID_ARG_EXT
  ("Argument %(arg0)s must be %(arg1)s. Found %(arg2)s: %(arg3)s"),

  INVALID_OPERATION1
  ("Operation %(arg0)s cannot be applied to %(arg1)s"),

  INVALID_OPERATION2
  ("Operation %(type)s cannot be applied to %(arg0)s and %(arg1)s"),

  MIXIN_RECURSE
  ("Mixin call %(name)s exceeded the recursion limit of %(arg0)s"),

  MIXIN_UNDEFINED
  ("Failed to locate a mixin using selector %(arg0)s"),

  PATTERN_COMPILE
  ("Regular expression pattern compilation failed: %(arg0)s"),

  PERCENT_MATH_ORDER
  ("The value %(arg0)s cannot be added to or subtracted from a percentage."),

  RULESET_RECURSE
  ("The ruleset %(name)s cannot call itself recursively."),

  SELECTOR_TOO_COMPLEX
  ("Selector exceeds the complexity threshold"),

  UNCOMPARABLE_TYPE
  ("Unable to compare instances of %(type)s"),

  UNKNOWN_UNIT
  ("Unknown unit %(arg0)s"),

  VAR_UNDEFINED
  ("Failed to locate a definition for the variable %(name)s in current scope"),

  VAR_CIRCULAR_REFERENCE
  ("Variable %(name)s references itself");

  private static final String PREFIX = "ExecuteError %(code)s: ";

  private MapFormat prefixFormat;

  private MapFormat messageFormat;

  ExecuteErrorType(String rawFormat) {
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
