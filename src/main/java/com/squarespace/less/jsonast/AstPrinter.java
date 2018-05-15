/**
 * Copyright (c) 2018 SQUARESPACE, Inc.
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

package com.squarespace.less.jsonast;

import static com.squarespace.less.jsonast.AstEmitter.ALPHA;
import static com.squarespace.less.jsonast.AstEmitter.ANONYMOUS;
import static com.squarespace.less.jsonast.AstEmitter.ARGUMENT;
import static com.squarespace.less.jsonast.AstEmitter.ASSIGNMENT;
import static com.squarespace.less.jsonast.AstEmitter.ATTR_ELEMENT;
import static com.squarespace.less.jsonast.AstEmitter.BLOCK_DIRECTIVE;
import static com.squarespace.less.jsonast.AstEmitter.COLOR;
import static com.squarespace.less.jsonast.AstEmitter.COMMENT;
import static com.squarespace.less.jsonast.AstEmitter.CONDITION;
import static com.squarespace.less.jsonast.AstEmitter.DEFINITION;
import static com.squarespace.less.jsonast.AstEmitter.DIMENSION;
import static com.squarespace.less.jsonast.AstEmitter.DIRECTIVE;
import static com.squarespace.less.jsonast.AstEmitter.EXPRESSION;
import static com.squarespace.less.jsonast.AstEmitter.EXPRESSION_LIST;
import static com.squarespace.less.jsonast.AstEmitter.FALSE;
import static com.squarespace.less.jsonast.AstEmitter.FEATURE;
import static com.squarespace.less.jsonast.AstEmitter.FEATURES;
import static com.squarespace.less.jsonast.AstEmitter.FUNCTION_CALL;
import static com.squarespace.less.jsonast.AstEmitter.GUARD;
import static com.squarespace.less.jsonast.AstEmitter.IMPORT;
import static com.squarespace.less.jsonast.AstEmitter.KEYWORD;
import static com.squarespace.less.jsonast.AstEmitter.MEDIA;
import static com.squarespace.less.jsonast.AstEmitter.MIXIN;
import static com.squarespace.less.jsonast.AstEmitter.MIXIN_ARGS;
import static com.squarespace.less.jsonast.AstEmitter.MIXIN_CALL;
import static com.squarespace.less.jsonast.AstEmitter.MIXIN_PARAMS;
import static com.squarespace.less.jsonast.AstEmitter.OPERATION;
import static com.squarespace.less.jsonast.AstEmitter.PARAMETER;
import static com.squarespace.less.jsonast.AstEmitter.PAREN;
import static com.squarespace.less.jsonast.AstEmitter.PROPERTY;
import static com.squarespace.less.jsonast.AstEmitter.QUOTED;
import static com.squarespace.less.jsonast.AstEmitter.RATIO;
import static com.squarespace.less.jsonast.AstEmitter.RULE;
import static com.squarespace.less.jsonast.AstEmitter.RULESET;
import static com.squarespace.less.jsonast.AstEmitter.SELECTOR;
import static com.squarespace.less.jsonast.AstEmitter.SELECTORS;
import static com.squarespace.less.jsonast.AstEmitter.SHORTHAND;
import static com.squarespace.less.jsonast.AstEmitter.STYLESHEET;
import static com.squarespace.less.jsonast.AstEmitter.TEXT_ELEMENT;
import static com.squarespace.less.jsonast.AstEmitter.TRUE;
import static com.squarespace.less.jsonast.AstEmitter.UNICODE_RANGE;
import static com.squarespace.less.jsonast.AstEmitter.URL;
import static com.squarespace.less.jsonast.AstEmitter.VALUE_ELEMENT;
import static com.squarespace.less.jsonast.AstEmitter.VARIABLE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

/**
 * Renders a JSON AST into a text representation for easier comparison
 * during testing.
 */
public class AstPrinter {

  private static final String INDENT = "  ";
  private final JsonArray root;
  private final List<String> strings;

  public AstPrinter(JsonObject obj) {
    this.root = obj.get("root").asArray();
    this.strings = copyStrings(obj.get("strings").asArray());
  }

  public String print() {
    return print(root, 0);
  }

  private String print(JsonArray obj, int depth) {
    StringBuilder buf = new StringBuilder();
    int type = obj.get(0).asInt();
    if (type == -1) {
      return "NULL";
    }
    int[] rec = TYPES[type];
    String name = NAMES.get(rec[0]);
    buf.append('[').append(name);
    for (int i = 1; i < rec.length; i++) {
      JsonValue val = obj.get(i);
      buf.append(", ");
      switch (rec[i]) {
        case LIST:
          buf.append(printList(val.asArray(), depth + 1));
          break;

        case VALUE:
          buf.append(print(val.asArray(), depth));
          break;

        case STR:
        {
          int idx = val.asInt();
          String str = this.strings.get(idx);
          if (str == null) {
            buf.append("null");
          } else {
            buf.append('\'').append(StringEscapeUtils.escapeJava(str)).append('\'');
          }
          break;
        }

        case BOOL:
          buf.append(val.asInt() == 0 ? false : true);
          break;

        case NUM:
        {
          // JSON lib we use for strict parsing doesn't let us see the number type.
          // Hack.
          String s = val.toString();
          if (s.indexOf('.') != -1) {
            buf.append(val.asDouble());
          } else {
            buf.append(val.asLong());
          }
          break;
        }

        default:
          System.err.println("unexpected:" + rec[i]);
          break;
      }
    }
    buf.append(']');
    return buf.toString();
  }

  private String printList(JsonArray list, int depth) {
    int size = list.size();
    if (size == 0) {
      return "[]";
    }
    StringBuilder buf = new StringBuilder();
    buf.append("[\n");
    String prefix = repeat(depth);
    for (int i = 0; i < size; i++) {
      JsonArray val = list.get(i).asArray();
      buf.append(prefix).append(this.print(val, depth));
      if (i < size - 1) {
        buf.append(",\n");
      }
    }
    buf.append('\n').append(repeat(depth - 1)).append(']');
    return buf.toString();
  }

  private static String repeat(int count) {
    String res = "";
    for (int i = 0; i < count; i++) {
      res += INDENT;
    }
    return res;
  }

  private static List<String> copyStrings(JsonArray arr) {
    List<String> result = new ArrayList<>();
    for (JsonValue value : arr) {
      result.add(value.isNull() ? null : value.asString());
    }
    return result;
  }

  private static final int LIST = 1;
  private static final int VALUE = 2;
  private static final int STR = 3;
  private static final int BOOL = 4;
  private static final int NUM = 5;

  private static final int[][] TYPES = new int[][] {
    { ALPHA, VALUE },
    { ANONYMOUS, STR },
    { ARGUMENT, STR, VALUE },
    { ASSIGNMENT, STR, VALUE },
    { ATTR_ELEMENT, STR, LIST },
    { BLOCK_DIRECTIVE, STR, LIST },
    { COLOR, NUM, NUM, NUM, NUM, STR },
    { COMMENT, STR, NUM, BOOL },
    { CONDITION, STR, VALUE, VALUE, BOOL },
    { DEFINITION, STR, VALUE },
    { DIMENSION, NUM, STR },
    { DIRECTIVE, STR, VALUE },
    { EXPRESSION, LIST },
    { EXPRESSION_LIST, LIST },
    { FALSE },
    { FEATURE, VALUE, VALUE },
    { FEATURES, LIST },
    { FUNCTION_CALL, STR, LIST },
    { GUARD, LIST },
    { IMPORT, VALUE, NUM, VALUE },
    { KEYWORD, STR },
    { MEDIA, VALUE, LIST },
    { MIXIN, STR, VALUE, VALUE, LIST },
    { MIXIN_ARGS, NUM, LIST },
    { MIXIN_CALL, VALUE, VALUE, BOOL },
    { MIXIN_PARAMS, LIST, BOOL, NUM },
    { OPERATION, STR, VALUE, VALUE },
    { PARAMETER, STR, VALUE, BOOL },
    { PAREN, VALUE },
    { PROPERTY, STR },
    { QUOTED, NUM, BOOL, LIST },
    { RATIO, STR },
    { RULE, VALUE, VALUE, BOOL },
    { RULESET, VALUE, LIST },
    { SELECTOR, LIST },
    { SELECTORS, LIST },
    { SHORTHAND, VALUE, VALUE },
    { STYLESHEET, NUM, LIST },
    { TEXT_ELEMENT, STR, STR },
    { TRUE },
    { UNICODE_RANGE, STR },
    { URL, VALUE },
    { VALUE_ELEMENT, STR, VALUE },
    { VARIABLE, STR },
  };

  private static final Map<Integer, String> NAMES = new HashMap<Integer, String>() { {
    put(ALPHA, "ALPHA");
    put(ANONYMOUS, "ANONYMOUS");
    put(ARGUMENT, "ARGUMENT");
    put(ASSIGNMENT, "ASSIGNMENT");
    put(ATTR_ELEMENT, "ATTR_ELEMENT");
    put(BLOCK_DIRECTIVE, "BLOCK_DIRECTIVE");
    put(COLOR, "COLOR");
    put(COMMENT, "COMMENT");
    put(CONDITION, "CONDITION");
    put(DEFINITION, "DEFINITION");
    put(DIMENSION, "DIMENSION");
    put(DIRECTIVE, "DIRECTIVE");
    put(EXPRESSION, "EXPRESSION");
    put(EXPRESSION_LIST, "EXPRESSION_LIST");
    put(FALSE, "FALSE");
    put(FEATURE, "FEATURE");
    put(FEATURES, "FEATURES");
    put(FUNCTION_CALL, "FUNCTION_CALL");
    put(GUARD, "GUARD");
    put(IMPORT, "IMPORT");
    put(KEYWORD, "KEYWORD");
    put(MEDIA, "MEDIA");
    put(MIXIN, "MIXIN");
    put(MIXIN_ARGS, "MIXIN_ARGS");
    put(MIXIN_CALL, "MIXIN_CALL");
    put(MIXIN_PARAMS, "MIXIN_PARAMS");
    put(OPERATION, "OPERATION");
    put(PARAMETER, "PARAMETER");
    put(PAREN, "PAREN");
    put(PROPERTY, "PROPERTY");
    put(QUOTED, "QUOTED");
    put(RATIO, "RATIO");
    put(RULE, "RULE");
    put(RULESET, "RULESET");
    put(SELECTOR, "SELECTOR");
    put(SELECTORS, "SELECTORS");
    put(SHORTHAND, "SHORTHAND");
    put(STYLESHEET, "STYLESHEET");
    put(TEXT_ELEMENT, "TEXT_ELEMENT");
    put(TRUE, "TRUE");
    put(UNICODE_RANGE, "UNICODE_RANGE");
    put(URL, "URL");
    put(VALUE_ELEMENT, "VALUE_ELEMENT");
    put(VARIABLE, "VARIABLE");
  } };
}
