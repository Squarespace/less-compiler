package com.squarespace.less.core;

import com.squarespace.less.ErrorInfo;
import com.squarespace.less.ErrorType;
import com.squarespace.less.ExecuteErrorType;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Operator;


/**
 * Builds error messages for execute phase of the compile, implemented as 
 * static methods to reduce code clutter somewhat.
 */
public class ExecuteErrorMaker {

  private ExecuteErrorMaker() {
  }
  
  public static ErrorInfo argCount(String name, int expected, int actual) {
    return error(ExecuteErrorType.ARG_COUNT).name(name).arg0(expected).arg1(actual);
  }
  
  public static ErrorInfo argNamedNotFound(Object name) {
    return error(ExecuteErrorType.ARG_NAMED_NOTFOUND).name(name);
  }

  public static ErrorInfo argTooMany() {
    return error(ExecuteErrorType.ARG_TOO_MANY);
  }
  
  public static ErrorInfo badColorMath(Operator op, Node arg) {
    String msg = op == Operator.SUBTRACT ? "be subtracted from" : "divide";
    return error(ExecuteErrorType.BAD_COLOR_MATH).arg0(msg).arg1(arg.repr());
  }
  
  public static ErrorInfo divideByZero(Node arg) {
    return error(ExecuteErrorType.DIVIDE_BY_ZERO).arg0(arg);
  }
  
  public static ErrorInfo expectedBoolOp(Object op) {
    return error(ExecuteErrorType.EXPECTED_BOOLOP).arg0(op);
  }
  
  public static ErrorInfo expectedMathOp(Object op) {
    return error(ExecuteErrorType.EXPECTED_MATHOP).arg0(op);
  }
  
  public static ErrorInfo formatFunctionArgs(int needed, int actual) {
    return error(ExecuteErrorType.FORMAT_FUNCTION_ARGS).arg0(needed).arg1(actual);
  }
  
  public static ErrorInfo importError(Object path, Object msg) {
    return error(ExecuteErrorType.IMPORT_ERROR).arg0(path).arg1(msg);
  }
  
  public static ErrorInfo incompatibleUnits(Object u0, Object u1) {
    return error(ExecuteErrorType.INCOMPATIBLE_UNITS).arg0(u0).arg1(u1);
  }
  
  public static ErrorInfo invalidArg(int index, Object type1, Object type2) {
    return error(ExecuteErrorType.INVALID_ARG).arg0(index).arg1(type1).arg2(type2);
  }
  
  public static ErrorInfo invalidOperation(Object op, Object type) {
    return error(ExecuteErrorType.INVALID_OPERATION1).arg0(op).arg1(type);
  }
  
  public static ErrorInfo invalidOperation(Object op, Object type0, Object type1) {
    return error(ExecuteErrorType.INVALID_OPERATION2).type(op).arg0(type0).arg1(type1);
  }
  
  public static ErrorInfo mixinRecurse(Object path, int limit) {
    return error(ExecuteErrorType.MIXIN_RECURSE).name(path).arg0(limit);
  }
  
  public static ErrorInfo mixinUndefined(Object selector) {
    return error(ExecuteErrorType.MIXIN_UNDEFINED).arg0(selector);
  }
  
  public static ErrorInfo percentMathOrder(Object dim) {
    return error(ExecuteErrorType.PERCENT_MATH_ORDER).arg0(dim);
  }
  
  public static ErrorInfo uncomparableType(Object type) {
    return error(ExecuteErrorType.UNCOMPARABLE_TYPE).type(type);
  }
  
  public static ErrorInfo unknownUnit(Object obj) {
    return error(ExecuteErrorType.UNKNOWN_UNIT).arg0(obj);
  }
  
  public static ErrorInfo varCircularRef(Object name) {
    return error(ExecuteErrorType.VAR_CIRCULAR_REFERENCE).name(name);
  }
  
  public static ErrorInfo varUndefined(Object name) {
    return error(ExecuteErrorType.VAR_UNDEFINED).name(name);
  }
  
  private static ErrorInfo error(ErrorType type) {
    ErrorInfo info = new ErrorInfo(type);
    info.code(type);
    return info;
  }
  
}
