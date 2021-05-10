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
public class LessErrorInfo {

  private static final String ARG0 = "arg0";

  private static final String ARG1 = "arg1";

  private static final String ARG2 = "arg2";

  private static final String ARG3 = "arg3";

  private static final String CODE = "code";

  private static final String NAME = "name";

  private static final String TYPE = "type";

  private final MapBuilder<String, Object> builder = new MapBuilder<>();

  private final LessErrorType type;

  public LessErrorInfo(LessErrorType type) {
    this.type = type;
  }

  public LessErrorInfo code(Object code) {
    return put(CODE, code);
  }

  public LessErrorInfo arg0(Object arg) {
    return put(ARG0, arg);
  }

  public LessErrorInfo arg1(Object arg) {
    return put(ARG1, arg);
  }

  public LessErrorInfo arg2(Object arg) {
    return put(ARG2, arg);
  }

  public LessErrorInfo arg3(Object arg) {
    return put(ARG3, arg);
  }

  public LessErrorInfo name(Object name) {
    return put(NAME, name);
  }

  public LessErrorInfo type(Object type) {
    return put(TYPE, type);
  }

  public LessErrorType type() {
    return type;
  }

  public String getMessage() {
    Map<String, Object> params = builder.get();
    StringBuilder buf = new StringBuilder();
    buf.append(type.prefix(params)).append(type.message(params));
    return buf.toString();
  }

  private LessErrorInfo put(String key, Object val) {
    builder.put(key, val);
    return this;
  }
}
