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

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import com.squarespace.less.core.FlexList;
import com.squarespace.less.model.Alpha;
import com.squarespace.less.model.Anonymous;
import com.squarespace.less.model.Argument;
import com.squarespace.less.model.Assignment;
import com.squarespace.less.model.AttributeElement;
import com.squarespace.less.model.BaseColor;
import com.squarespace.less.model.BlockDirective;
import com.squarespace.less.model.BlockNode;
import com.squarespace.less.model.Combinator;
import com.squarespace.less.model.Comment;
import com.squarespace.less.model.Condition;
import com.squarespace.less.model.Definition;
import com.squarespace.less.model.Dimension;
import com.squarespace.less.model.Directive;
import com.squarespace.less.model.Element;
import com.squarespace.less.model.Expression;
import com.squarespace.less.model.ExpressionList;
import com.squarespace.less.model.Feature;
import com.squarespace.less.model.Features;
import com.squarespace.less.model.FunctionCall;
import com.squarespace.less.model.Guard;
import com.squarespace.less.model.Import;
import com.squarespace.less.model.Keyword;
import com.squarespace.less.model.KeywordColor;
import com.squarespace.less.model.Media;
import com.squarespace.less.model.Mixin;
import com.squarespace.less.model.MixinCall;
import com.squarespace.less.model.MixinCallArgs;
import com.squarespace.less.model.MixinParams;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.NodeType;
import com.squarespace.less.model.Operation;
import com.squarespace.less.model.Parameter;
import com.squarespace.less.model.Paren;
import com.squarespace.less.model.Property;
import com.squarespace.less.model.Quoted;
import com.squarespace.less.model.RGBColor;
import com.squarespace.less.model.Ratio;
import com.squarespace.less.model.Rule;
import com.squarespace.less.model.Ruleset;
import com.squarespace.less.model.Selector;
import com.squarespace.less.model.Selectors;
import com.squarespace.less.model.Shorthand;
import com.squarespace.less.model.Stylesheet;
import com.squarespace.less.model.TextElement;
import com.squarespace.less.model.UnicodeRange;
import com.squarespace.less.model.Unit;
import com.squarespace.less.model.Url;
import com.squarespace.less.model.ValueElement;
import com.squarespace.less.model.Variable;

public class AstEmitter {

  public static final int NULL = -1;
  public static final int ALPHA = 0;
  public static final int ANONYMOUS = 1;
  public static final int ARGUMENT = 2;
  public static final int ASSIGNMENT = 3;
  public static final int ATTR_ELEMENT = 4;
  public static final int BLOCK_DIRECTIVE = 5;
  public static final int COLOR = 6;
  public static final int COMMENT = 7;
  public static final int CONDITION = 8;
  public static final int DEFINITION = 9;
  public static final int DIMENSION = 10;
  public static final int DIRECTIVE = 11;
  public static final int EXPRESSION = 12;
  public static final int EXPRESSION_LIST = 13;
  public static final int FALSE = 14;
  public static final int FEATURE = 15;
  public static final int FEATURES = 16;
  public static final int FUNCTION_CALL = 17;
  public static final int GUARD = 18;
  public static final int IMPORT = 19;
  public static final int KEYWORD = 20;
  public static final int MEDIA = 21;
  public static final int MIXIN = 22;
  public static final int MIXIN_ARGS = 23;
  public static final int MIXIN_CALL = 24;
  public static final int MIXIN_PARAMS = 25;
  public static final int OPERATION = 26;
  public static final int PARAMETER = 27;
  public static final int PAREN = 28;
  public static final int PROPERTY = 29;
  public static final int QUOTED = 30;
  public static final int RATIO = 31;
  public static final int RULE = 32;
  public static final int RULESET = 33;
  public static final int SELECTOR = 34;
  public static final int SELECTORS = 35;
  public static final int SHORTHAND = 36;
  public static final int STYLESHEET = 37;
  public static final int TEXT_ELEMENT = 38;
  public static final int TRUE = 39;
  public static final int UNICODE_RANGE = 40;
  public static final int URL = 41;
  public static final int VALUE_ELEMENT = 42;
  public static final int VARIABLE = 43;

  private static final int VERSION = 1;

  /** Root node we're converting to JSON AST (typically a STYLESHEET node). */
  private final Node root;

  /** Current buffer we're emitting to */
  private AstBuffer buf;

  private AstEmitter(Node root) {
    this.root = root;
  }

  public static String render(Node root) {
    return render(root, false);
  }

  public static String render(Node root, boolean savePositions) {
    return new AstEmitter(root).process(savePositions);
  }

  private String process(boolean savePositions) {
    // Pass 1: escape and index all strings
    AstOptimizer optimizer = new AstOptimizer();
    buf = optimizer;
    emit(root);

    Pair<Map<String, Integer>, List<String>> result = optimizer.optimize();

    // Pass 2: encode final JSON with optimized string table
    AstEncoder encoder = new AstEncoder(result.getLeft(), result.getRight(), savePositions);
    buf = encoder;
    emit(root);
    return encoder.render();
  }

  private AstEmitter emit(Node n) {
    if (n == null) {
      open(NULL, n);
      close();
      return this;
    }

    NodeType type = n.type();
    switch (type) {

      case BLOCK:
      case GENERIC_BLOCK:
      case IMPORT_MARKER:
      case MIXIN_MARKER:
      case PARSE_ERROR:
      {
        open(NULL, n);
        close();
        break;
      }

      case ALPHA:
      {
        Alpha o = (Alpha)n;
        open(ALPHA, n);
        sep();
        emit(o.value());
        close();
        break;
      }

      case ANONYMOUS:
      {
        Anonymous o = (Anonymous)n;
        open(ANONYMOUS, n);
        sep();
        string(o.value());
        close();
        break;
      }

      case ARGUMENT:
      {
        Argument o = (Argument)n;
        open(ARGUMENT, n);
        sep();
        string(o.name());
        sep();
        emit(o.value());
        close();
        break;
      }

      case ASSIGNMENT:
      {
        Assignment o = (Assignment)n;
        open(ASSIGNMENT, n);
        sep();
        string(o.name());
        sep();
        emit(o.value());
        close();
        break;
      }

      case BLOCK_DIRECTIVE:
      {
        BlockDirective o = (BlockDirective)n;
        open(BLOCK_DIRECTIVE, n);
        sep();
        string(o.name());
        sep();
        emit(o.block().rules());
        close();
        break;
      }

      case COLOR:
      {
        BaseColor base = (BaseColor) n;
        String keyword = base instanceof KeywordColor ? ((KeywordColor)base).keyword() : null;
        RGBColor o = (RGBColor) base.toRGB();
        open(COLOR, n);
        sep();
        number(o.red());
        sep();
        number(o.green());
        sep();
        number(o.blue());
        sep();
        number(o.alpha());
        sep();
        string(keyword);
        close();
        break;
      }

      case COMMENT:
      {
        Comment o = (Comment)n;
        open(COMMENT, n);
        sep();
        string(o.body());
        sep();
        number(o.block() ? 1 : 0);
        sep();
        number(o.newline() ? 1 : 0);
        close();
        break;
      }

      case CONDITION:
      {
        Condition o = (Condition)n;
        open(CONDITION, n);
        sep();
        string(o.operator().repr());
        sep();
        emit(o.left());
        sep();
        emit(o.right());
        sep();
        number(o.negate() ? 1 : 0);
        close();
        break;
      }

      case DEFINITION:
      {
        Definition o = (Definition)n;
        open(DEFINITION, n);
        sep();
        string(o.name());
        sep();
        emit(o.value());
        close();
        break;
      }

      case DIMENSION:
      {
        Dimension o = (Dimension)n;
        double v = o.value();

        open(DIMENSION, n);
        sep();
        if (v == (long)v) {
          number((long)v);
        } else {
          number(v);
        }
        sep();

        Unit unit = o.unit();
        string(unit == null ? null : unit.repr());
        close();
        break;
      }

      case DIRECTIVE:
      {
        Directive o = (Directive)n;
        open(DIRECTIVE, n);
        sep();
        string(o.name());
        sep();
        emit(o.value());
        close();
        break;
      }

      case ELEMENT:
      {
        Element o = (Element) n;
        if (o instanceof AttributeElement) {
          open(ATTR_ELEMENT, n);
        } else if (o instanceof ValueElement) {
          open(VALUE_ELEMENT, n);
        } else if (o instanceof TextElement) {
          open(TEXT_ELEMENT, n);
        }
        sep();

        Combinator c = (Combinator)o.combinator();
        string(c == null ? null : c.getChar() + "");
        sep();
        if (o instanceof AttributeElement) {
          AttributeElement attr = (AttributeElement)o;
          emit(attr.parts());

        } else if (o instanceof ValueElement) {
          emit(((ValueElement)o).value());

        } else if (o instanceof TextElement) {
          string(((TextElement)o).name());
        }

        close();
        break;
      }

      case EXPRESSION:
      {
        Expression o = (Expression)n;
        open(EXPRESSION, n);
        sep();
        emit(o.values());
        close();
        break;
      }

      case EXPRESSION_LIST:
      {
        ExpressionList o = (ExpressionList)n;
        open(EXPRESSION_LIST, n);
        sep();
        emit(o.expressions());
        close();
        break;
      }

      case FALSE:
      {
        open(FALSE, n);
        close();
        break;
      }

      case FEATURE:
      {
        Feature o = (Feature)n;
        open(FEATURE, n);
        sep();
        emit(o.property());
        sep();
        emit(o.value());
        close();
        break;
      }

      case FEATURES:
      {
        Features o = (Features)n;
        open(FEATURES, n);
        sep();
        emit(o.features());
        close();
        break;
      }

      case FUNCTION_CALL:
      {
        FunctionCall o = (FunctionCall)n;
        open(FUNCTION_CALL, n);
        sep();
        string(o.name());
        sep();
        emit(o.args());
        close();
        break;
      }

      case GUARD:
      {
        Guard o = (Guard)n;
        open(GUARD, n);
        sep();
        emit(o.conditions());
        close();
        break;
      }

      case IMPORT:
      {
        Import o = (Import)n;
        open(IMPORT, n);
        sep();
        emit(o.path());
        sep();
        number(o.once() ? 1 : 0);
        sep();
        emit(o.features());
        close();
        break;
      }

      case KEYWORD:
      {
        Keyword o = (Keyword)n;
        open(KEYWORD, n);
        sep();
        string(o.value());
        close();
        break;
      }

      case MEDIA:
      {
        Media o = (Media)n;
        open(MEDIA, n);
        sep();
        emit(o.features());
        sep();
        emit(o.block().rules());
        close();
        break;
      }

      case MIXIN:
      {
        Mixin o = (Mixin)n;
        open(MIXIN, n);
        sep();
        string(o.name());
        sep();
        emit(o.params());
        sep();
        emit(o.guard());
        sep();
        emit(o.block().rules());
        close();
        break;
      }

      case MIXIN_ARGS:
      {
        MixinCallArgs o = (MixinCallArgs)n;
        open(MIXIN_ARGS, n);
        sep();
        number(o.delim() == ',' ? 0 : 1);
        sep();
        emit(o.args());
        close();
        break;
      }

      case MIXIN_CALL:
      {
        MixinCall o = (MixinCall)n;
        open(MIXIN_CALL, n);
        sep();
        emit(o.selector());
        sep();
        emit(o.args());
        sep();
        number(o.important() ? 1 : 0);
        close();
        break;
      }

      case MIXIN_PARAMS:
      {
        MixinParams o = (MixinParams)n;
        open(MIXIN_PARAMS, n);
        sep();
        emit(o.params());
        sep();
        number(o.variadic() ? 1 : 0);
        sep();
        number(o.required());
        close();
        break;
      }

      case OPERATION:
      {
        Operation o = (Operation)n;
        open(OPERATION, n);
        sep();
        string(o.operator().repr());
        sep();
        emit(o.left());
        sep();
        emit(o.right());
        close();
        break;
      }

      case PARAMETER:
      {
        Parameter o = (Parameter)n;
        open(PARAMETER, n);
        sep();
        string(o.name());
        sep();
        emit(o.value());
        sep();
        number(o.variadic() ? 1 : 0);
        close();
        break;
      }

      case PAREN:
      {
        Paren o = (Paren)n;
        open(PAREN, n);
        sep();
        emit(o.value());
        close();
        break;
      }

      case PROPERTY:
      {
        Property o = (Property)n;
        open(PROPERTY, n);
        sep();
        string(o.name());
        close();
        break;
      }

      case QUOTED:
      {
        Quoted o = (Quoted)n;
        open(QUOTED, n);
        sep();
        number(o.delimiter() == '"' ? 1 : 0);
        sep();
        number(o.escaped() ? 1 : 0);
        sep();
        emit(o.parts());
        close();
        break;
      }

      case RATIO:
      {
        Ratio o = (Ratio)n;
        open(RATIO, n);
        sep();
        string(o.value());
        close();
        break;
      }

      case RULE:
      {
        Rule o = (Rule)n;
        open(RULE, n);
        sep();
        emit(o.property());
        sep();
        emit(o.value());
        sep();
        number(o.important() ? 1 : 0);
        close();
        break;
      }

      case RULESET:
      {
        Ruleset o = (Ruleset)n;
        open(RULESET, n);
        sep();
        emit(o.selectors());
        sep();
        emit(o.block().rules());
        close();
        break;
      }

      case SELECTOR:
      {
        Selector o = (Selector)n;
        open(SELECTOR, n);
        sep();
        emit(o.elements());
        close();
        break;
      }

      case SELECTORS:
      {
        Selectors o = (Selectors)n;
        open(SELECTORS, n);
        sep();
        emit(o.selectors());
        close();
        break;
      }

      case SHORTHAND:
      {
        Shorthand o = (Shorthand)n;
        open(SHORTHAND, n);
        sep();
        emit(o.left());
        sep();
        emit(o.right());
        close();
        break;
      }

      case STYLESHEET:
      {
        Stylesheet o = (Stylesheet)n;
        open(STYLESHEET, n);
        sep();
        number(VERSION);
        sep();
        emit(o.block().rules());
        close();
        break;
      }

      case TRUE:
      {
        open(TRUE, n);
        close();
        break;
      }

      case UNICODE_RANGE:
      {
        UnicodeRange o = (UnicodeRange)n;
        open(UNICODE_RANGE, n);
        sep();
        string(o.value());
        close();
        break;
      }

      case URL:
      {
        Url o = (Url)n;
        open(URL, n);
        sep();
        emit(o.value());
        close();
        break;
      }

      case VARIABLE:
      {
        Variable o = (Variable)n;
        open(VARIABLE, n);
        sep();
        string(o.name());
        sep();
        number(o.indirect() ? 1 : 0);
        sep();
        number(o.curly() ? 1 : 0);
        close();
        break;
      }

      default:
        break;
    }
    return this;
  }

  private void emit(List<? extends Node> nodes) {
    if (nodes == null) {
      buf.append("[]");
      return;
    }
    int size = nodes.size();
    buf.append('[');
    for (int i = 0; i < size; i++) {
      if (i > 0) {
        sep();
      }
      emit(nodes.get(i));
    }
    buf.append(']');
  }

  private void emit(FlexList<? extends Node> nodes) {
    int size = nodes.size();
    buf.append('[');
    for (int i = 0; i < size; i++) {
      if (i > 0) {
        sep();
      }
      emit(nodes.get(i));
    }
    buf.append(']');
  }

  private void open(int type, Node n) {
    if (n != null) {
      int line = n.lineOffset();
      int col = n.charOffset();
      Path path = null;
      if (n instanceof BlockNode) {
        path = ((BlockNode)n).fileName();
      }
      buf.position(line, col, path == null ? null : path.toString());
    }
    buf.append('[');
    buf.append(type);
  }

  private void close() {
    buf.append(']');
  }

  private void sep() {
    buf.append(',');
  }

  private void number(long n) {
    buf.append(n);
  }

  private void number(double n) {
    buf.append(n);
  }

  private void string(String s) {
    buf.string(s);
  }

}
