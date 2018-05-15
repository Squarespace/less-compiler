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

package com.squarespace.less;

import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.testng.TestNGException;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.ParseException;
import com.eclipsesource.json.WriterConfig;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessUtils;
import com.squarespace.less.exec.LessSuiteBase;
import com.squarespace.less.jsonast.AstEmitter;
import com.squarespace.less.jsonast.AstPrinter;
import com.squarespace.less.model.Node;
import com.squarespace.less.parse.Parselet;
import com.squarespace.less.parse.Parselets;

public class AstTestCaseParser {

  private static final String E_PAIRS = "Each LESS section must be paired with a JSON section";
  private static final Pattern RE_LINES = Pattern.compile("\n");
  private static final Pattern RE_SECTION = Pattern.compile("^:([\\w-_]+)\\s*$");

  public static AstTestCase load(Class<?> cls, String path) throws IOException {
    try (InputStream stream = cls.getResourceAsStream(path)) {
      String raw = LessUtils.readStream(stream);
      return parse(path, raw);
    }
  }

  public static AstTestCase parse(String name, String source) {
    Matcher matcher = RE_SECTION.matcher("");
    String[] lines = RE_LINES.split(source);

    AstTestCase c = new AstTestCase(name, Parselets.STYLESHEET);
    String key = null;
    StringBuilder buf = new StringBuilder();

    String currLess = null;
    int size = lines.length;
    for (int i = 0; i < size; i++) {
      matcher.reset(lines[i]);
      if (matcher.lookingAt()) {
        if (key != null) {
          switch (key) {
            case "PROPERTIES":
              Properties props = loadProperties(buf.toString());
              String parseletName = props.getProperty("parser");
              Parselet[] parselet = parselet(parseletName == null ? "stylesheet" : parseletName);
              c.set(parselet);
              currLess = null;
              break;

            case "LESS":
              if (currLess != null) {
                throw new AssertionError(E_PAIRS);
              }
              currLess = buf.toString();
              break;

            case "REPR":
              if (currLess == null) {
                throw new AssertionError(E_PAIRS);
              }
              c.add(currLess, buf.toString());
              currLess = null;
              break;

            default:
              break;
          }
          buf.setLength(0);
        }
        key = matcher.group(1);

      } else {
        buf.append('\n');
        buf.append(lines[i]);
      }
    }

    if (currLess != null) {
      if (key.equals("REPR") && buf.length() > 0) {
        c.add(currLess, buf.toString());
      } else {
        throw new AssertionError(E_PAIRS);
      }
    }
    return c;
  }

  public static class AstTestCase {

    private String name;
    private Parselet[] parselet;

    private List<Pair<String, String>> cases = new ArrayList<>();

    public AstTestCase(String name, Parselet[] parselet) {
      this.name = name;
      this.parselet = parselet;
    }

    public void set(Parselet[] parselet) {
      this.parselet = parselet;
    }

    public void add(String less, String json) {
      cases.add(Pair.of(less, json));
    }

    public void execute() throws LessException {
      LessHarness h = new LessHarness(parselet);
      int i = 1;
      for (Pair<String, String> entry : cases) {
        Node node = h.parse(entry.getLeft().trim());
        String result = AstEmitter.render(node);
        JsonObject actual = parseJson(result, node);
        String actualStr = render(actual);
        String expectedStr = entry.getRight().trim();
        if (!expectedStr.equals(actualStr)) {
          String header = String.format("AST test '%s' case %d", name, i);
          String diff = LessSuiteBase.diff(expectedStr, actualStr);
          throw new AssertionError(header + "\n\n"
              + actualStr + "\n\n"
              + actual.toString(WriterConfig.PRETTY_PRINT) + "\n\n"
              + diff);
        }
        i++;
      }
    }
  }

  private static String render(JsonObject result) {
    try {
      AstPrinter printer = new AstPrinter(result);
      return printer.print();
    } catch (Exception e) {
      throw new AssertionError(e + "\n\nInput: " + result);
    }
  }

  private static JsonObject parseJson(String raw, Node node) {
    try {
      return Json.parse(raw).asObject();
    } catch (ParseException e) {
      int len = raw.length();
      int offset = e.getLocation().offset;
      int start = Math.max(0, offset - 20);
      int end = Math.min(len, offset + 20);
      String prefix = escapeJava(raw.substring(start, offset));
      String suffix = escapeJava(raw.substring(offset, end));
      String msg = e.getMessage() + ":\n" + prefix + suffix + "\n";
      msg += StringUtils.repeat('-', prefix.length()) + "^\n";
      if (node != null) {
        Buffer buf = new Buffer(2);
        node.modelRepr(buf);
        msg += "\nLESS was: " + buf.toString();
      }
      throw new AssertionError(msg);
    }
  }

//  private static String lineNumbers(String raw) {
//    StringBuilder buf = new StringBuilder();
//    String[] lines = raw.split("\n");
//    int len = lines.length;
//    int width = Integer.toString(len).length();
//    String fmt = "%-" + width + "s  %s\n";
//    for (int i = 0; i < len; i++) {
//      buf.append(String.format(fmt, i, lines[i]));
//    }
//    return buf.toString();
//  }

  private static Properties loadProperties(String raw) {
    try {
      Properties props = new Properties();
      props.load(new StringReader(raw));
      return props;
    } catch (IOException e) {
      throw new AssertionError("Properties failed to parse: " + e.getMessage(), e);
    }
  }

  private static Parselet[] parselet(String name) {
    Parselet[] result = PARSELETS.get(name);
    if (result != null) {
      return result;
    }
    throw new TestNGException("Unsupported parselet name '" + name + "'");
  }

  private static final Map<String, Parselet[]> PARSELETS = new HashMap<String, Parselet[]>() { {
    put("ADDITION", Parselets.ADDITION);
    put("ALPHA", Parselets.ALPHA);
    put("COLOR", Parselets.COLOR);
    put("COLOR_KEYWORD", Parselets.COLOR_KEYWORD);
    put("COMMENT", Parselets.COMMENT);
    put("CONDITION", Parselets.CONDITION);
    put("FUNCTION_CALL_ARGS", Parselets.FUNCTION_CALL_ARGS);
    put("DIMENSION", Parselets.DIMENSION);
    put("DIRECTIVE", Parselets.DIRECTIVE);
    put("FUNCTION_CALL", Parselets.FUNCTION_CALL);
    put("GUARD", Parselets.GUARD);
    put("MEDIA", Parselets.PRIMARY_SUB);
    put("MIXIN", Parselets.PRIMARY_SUB);
    put("MIXIN_CALL", Parselets.MIXIN_CALL);
    put("MIXIN_CALL_ARGS", Parselets.MIXIN_CALL_ARGS);
    put("PRIMARY_SUB", Parselets.PRIMARY_SUB);
    put("RATIO", Parselets.RATIO);
    put("RULE", Parselets.RULE);
    put("RULESET", Parselets.RULESET);
    put("SELECTORS", Parselets.SELECTORS);
    put("SHORTHAND", Parselets.SHORTHAND);
    put("STYLESHEET", Parselets.STYLESHEET);
  } };

}


