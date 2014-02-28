package com.squarespace.v6.template.less;

import static com.squarespace.v6.template.less.core.Constants.NULL_PLACEHOLDER;

import java.util.Map;

import com.squarespace.v6.template.less.core.MapFormat;


/**
 * Errors thrown in the execute phase of the compile are execute errors.
 */
public enum ExecuteErrorType implements ErrorType {

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
  
  IMPORT_ERROR
  ("An error occurred importing '%(arg0)s': %(arg1)s"),
  
  INCOMPATIBLE_UNITS
  ("No conversion is possible from %(arg0)s to %(arg1)s"),
  
  INVALID_ARG
  ("Argument %(arg0)s must be %(arg1)s. Found %(arg2)s"),

  INVALID_OPERATION1
  ("Operation %(arg0)s cannot be applied to %(arg1)s"),
  
  INVALID_OPERATION2
  ("Operation %(type)s cannot be applied to %(arg0)s and %(arg1)s"),

  MIXIN_RECURSE
  ("Mixin call %(name)s exceeded the recursion limit of %(arg0)s"),
  
  MIXIN_UNDEFINED
  ("Failed to locate a mixin using selector %(arg0)s"),
  
  PERCENT_MATH_ORDER
  ("The value %(arg0)s cannot be added to or subtracted from a percentage."),
  
  RULESET_RECURSE
  ("The ruleset %(name)s cannot call itself recursively."),
  
  UNCOMPARABLE_TYPE
  ("Unable to compare instances of %(type)s"),
  
  UNKNOWN_UNIT
  ("Unknown unit %(arg0)s"),

  VAR_UNDEFINED
  ("Failed to locate a definition for the variable %(name)s in current scope"),
  
  VAR_CIRCULAR_REFERENCE
  ("Variable %(name)s references itself")
  
  ;

  private static final String PREFIX = "ExecuteError %(code)s: ";
  
  private MapFormat prefixFormat;
  
  private MapFormat messageFormat;
  
  private ExecuteErrorType(String rawFormat) {
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
