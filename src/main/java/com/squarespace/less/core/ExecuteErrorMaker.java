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

  public static LessErrorInfo argCount(String name, int expected, int actual) {
    return error(ExecuteErrorType.ARG_COUNT).name(name).arg0(expected).arg1(actual);
  }

  public static LessErrorInfo argNamedNotFound(Object name) {
    return error(ExecuteErrorType.ARG_NAMED_NOTFOUND).name(name);
  }

  public static LessErrorInfo argTooMany() {
    return error(ExecuteErrorType.ARG_TOO_MANY);
  }

  public static LessErrorInfo badColorMath(Operator op, Node arg) {
    String msg = op == Operator.SUBTRACT ? "be subtracted from" : "divide";
    return error(ExecuteErrorType.BAD_COLOR_MATH).arg0(msg).arg1(arg.repr());
  }

  public static LessErrorInfo divideByZero(Node arg) {
    return error(ExecuteErrorType.DIVIDE_BY_ZERO).arg0(arg);
  }

  public static LessErrorInfo expectedBoolOp(Object op) {
    return error(ExecuteErrorType.EXPECTED_BOOLOP).arg0(op);
  }

  public static LessErrorInfo expectedMathOp(Object op) {
    return error(ExecuteErrorType.EXPECTED_MATHOP).arg0(op);
  }

  public static LessErrorInfo formatFunctionArgs(int needed, int actual) {
    return error(ExecuteErrorType.FORMAT_FUNCTION_ARGS).arg0(needed).arg1(actual);
  }

  public static LessErrorInfo importError(Object path, Object msg) {
    return error(ExecuteErrorType.IMPORT_ERROR).arg0(path).arg1(msg);
  }

  public static LessErrorInfo incompatibleUnits(Object u0, Object u1) {
    return error(ExecuteErrorType.INCOMPATIBLE_UNITS).arg0(u0).arg1(u1);
  }

  public static LessErrorInfo invalidArg(int index, Object type1, Object type2) {
    return error(ExecuteErrorType.INVALID_ARG).arg0(index).arg1(type1).arg2(type2);
  }

  public static LessErrorInfo invalidOperation(Object op, Object type) {
    return error(ExecuteErrorType.INVALID_OPERATION1).arg0(op).arg1(type);
  }

  public static LessErrorInfo invalidOperation(Object op, Object type0, Object type1) {
    return error(ExecuteErrorType.INVALID_OPERATION2).type(op).arg0(type0).arg1(type1);
  }

  public static LessErrorInfo mixinRecurse(Object path, int limit) {
    return error(ExecuteErrorType.MIXIN_RECURSE).name(path).arg0(limit);
  }

  public static LessErrorInfo mixinUndefined(Object selector) {
    return error(ExecuteErrorType.MIXIN_UNDEFINED).arg0(selector);
  }

  public static LessErrorInfo percentMathOrder(Object dim) {
    return error(ExecuteErrorType.PERCENT_MATH_ORDER).arg0(dim);
  }

  public static LessErrorInfo uncomparableType(Object type) {
    return error(ExecuteErrorType.UNCOMPARABLE_TYPE).type(type);
  }

  public static LessErrorInfo unknownUnit(Object obj) {
    return error(ExecuteErrorType.UNKNOWN_UNIT).arg0(obj);
  }

  public static LessErrorInfo varCircularRef(Object name) {
    return error(ExecuteErrorType.VAR_CIRCULAR_REFERENCE).name(name);
  }

  public static LessErrorInfo varUndefined(Object name) {
    return error(ExecuteErrorType.VAR_UNDEFINED).name(name);
  }

  private static LessErrorInfo error(LessErrorType type) {
    LessErrorInfo info = new LessErrorInfo(type);
    info.code(type);
    return info;
  }

}
