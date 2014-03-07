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

import java.util.Map;

import com.squarespace.less.core.MapBuilder;


/**
 * Captures type and message formatting arguments for error messages.
 */
public class ErrorInfo {

  private static final String ARG0 = "arg0";

  private static final String ARG1 = "arg1";

  private static final String ARG2 = "arg2";

  private static final String CODE = "code";

  private static final String NAME = "name";

  private static final String TYPE = "type";

  private final MapBuilder<String, Object> builder = new MapBuilder<>();

  private final ErrorType type;

  public ErrorInfo(ErrorType type) {
    this.type = type;
  }

  public ErrorInfo code(Object code) {
    return put(CODE, code);
  }

  public ErrorInfo arg0(Object arg) {
    return put(ARG0, arg);
  }

  public ErrorInfo arg1(Object arg) {
    return put(ARG1, arg);
  }

  public ErrorInfo arg2(Object arg) {
    return put(ARG2, arg);
  }

  public ErrorInfo name(Object name) {
    return put(NAME, name);
  }

  public ErrorInfo type(Object type) {
    return put(TYPE, type);
  }

  public ErrorType type() {
    return type;
  }

  public String getMessage() {
    Map<String, Object> params = builder.get();
    StringBuilder buf = new StringBuilder();
    buf.append(type.prefix(params)).append(type.message(params));
    return buf.toString();
  }

  private ErrorInfo put(String key, Object val) {
    builder.put(key, val);
    return this;
  }
}
