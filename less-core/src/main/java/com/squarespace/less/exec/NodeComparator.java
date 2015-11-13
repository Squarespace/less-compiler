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

package com.squarespace.less.exec;

import static com.squarespace.less.core.Chars.hexchar;
import static com.squarespace.less.core.ExecuteErrorMaker.uncomparableType;
import static com.squarespace.less.exec.Comparison.EQUAL_TO;
import static com.squarespace.less.exec.Comparison.GREATER_THAN;
import static com.squarespace.less.exec.Comparison.LESS_THAN;
import static com.squarespace.less.exec.Comparison.NOT_COMPARABLE;
import static com.squarespace.less.model.NodeType.ANONYMOUS;
import static com.squarespace.less.model.NodeType.COLOR;
import static com.squarespace.less.model.NodeType.DETACHED_RULESET;
import static com.squarespace.less.model.NodeType.DIMENSION;
import static com.squarespace.less.model.NodeType.FALSE;
import static com.squarespace.less.model.NodeType.KEYWORD;
import static com.squarespace.less.model.NodeType.QUOTED;
import static com.squarespace.less.model.NodeType.TRUE;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.squarespace.less.LessContext;
import com.squarespace.less.LessException;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.LessUtils;
import com.squarespace.less.model.Anonymous;
import com.squarespace.less.model.BaseColor;
import com.squarespace.less.model.Colors;
import com.squarespace.less.model.Condition;
import com.squarespace.less.model.Dimension;
import com.squarespace.less.model.Expression;
import com.squarespace.less.model.ExpressionList;
import com.squarespace.less.model.Keyword;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.NodeType;
import com.squarespace.less.model.Quoted;
import com.squarespace.less.model.RGBColor;
import com.squarespace.less.model.Unit;
import com.squarespace.less.model.UnitConversions;


/**
 * Performs comparisons between nodes, including recursive comparisons
 * of composite types like EXPRESSION, and falling back to comparing
 * the rendered string forms of certain types.
 *
 * At some point it may make sense for this class to be used to define
 * fuzzy or strict comparison modes at runtime.
 */
public class NodeComparator {

  /**
   * Types which have special comparisons against other types or their rendered forms.
   */
  private static final Set<NodeType> SPECIAL_COMPARE = EnumSet.of(
      ANONYMOUS, COLOR, DIMENSION, FALSE, KEYWORD, QUOTED, TRUE);

  /**
   * Types which are not comparable.
   */
  private static final Set<NodeType> NO_COMPARE = EnumSet.of(DETACHED_RULESET);

  /**
   * Context which provides buffers for rendering.
   */
  private final LessContext context;

  /**
   * Constructs a comparator.
   */
  public NodeComparator(LessContext context) {
    this.context = context;
  }

  /**
   * Compares two nodes for use in {@link Condition} expressions.
   */
  public Comparison compare(Node left, Node right) throws LessException {
    NodeType leftType = left.type();
    NodeType rightType = right.type();

    // Some types cannot be compared at all, so ignore them.
    check(leftType);
    check(rightType);

    // Comparisons between same types are more efficient.
    if (leftType == rightType) {
      return compareSameType(left, right);
    }

    boolean skip = rightType == ANONYMOUS || rightType == QUOTED;
    if (SPECIAL_COMPARE.contains(leftType) && !skip) {
      return compareImpl(left, right);

    } else if (SPECIAL_COMPARE.contains(rightType)) {
      return invert(compareImpl(right, left));
    }

    return NOT_COMPARABLE;
  }

  /**
   * Throws an exception if the given type cannot be compared.
   */
  private void check(NodeType type) throws LessException {
    if (NO_COMPARE.contains(type)) {
      throw new LessException(uncomparableType(type));
    }
  }

  /**
   * Compare two nodes where the left node's type is known.
   */
  private Comparison compareImpl(Node left, Node right) throws LessException {
    switch (left.type()) {
      case ANONYMOUS:
        return compareExact(render(left), render(right));

      case COLOR:
        return compareColors((BaseColor)left, right);

      case DIMENSION:
        if (right instanceof Dimension) {
          return compareDimensions((Dimension)left, (Dimension)right);
        }
        return NOT_COMPARABLE;

      case KEYWORD:
      case TRUE:
      case FALSE:
        return compareExact(render(left), render(right));

      case QUOTED:
        if (right instanceof Quoted) {
          return compareQuoted((Quoted)left, (Quoted)right);
        }
        return compareExact(render(left), render(right));

      default:
        return NOT_COMPARABLE;
    }
  }

  /**
   * Compare two nodes of the same type. This is a fast path that requires no
   * rendering.
   */
  private Comparison compareSameType(Node left, Node right) throws LessException {
    switch (left.type()) {
      case ANONYMOUS:
        return compareOrdered(((Anonymous)left).value(), ((Anonymous)right).value());

      case KEYWORD:
      case TRUE:
      case FALSE:
        return compareOrdered(((Keyword)left).value(), ((Keyword)right).value());

      case COLOR:
        return compareColorsExact((BaseColor)left, (BaseColor)right);

      case DETACHED_RULESET:
        // TODO: these can be compared in theory, but as of less.js 2.4 they raise an error.
        throw new LessException(uncomparableType(DETACHED_RULESET));

      case DIMENSION:
        return compareDimensions((Dimension)left, (Dimension)right);

      case EXPRESSION:
        return compareExpressions((Expression)left, (Expression)right);

      case EXPRESSION_LIST:
        return compareExpressionLists((ExpressionList)left, (ExpressionList)right);

      case QUOTED:
        return compareQuoted((Quoted)left, (Quoted)right);

      default:
        return compareOrdered(render(left), render(right));
    }
  }

  /**
   * Compare a known color against unknown type.
   */
  private Comparison compareColors(BaseColor left, Node right) {
    if (right instanceof BaseColor) {
      return compareColorsExact(left, (BaseColor)right);
    }

    // Avoids comparing colors against strings which will obviously
    // fail to match, e.g. non-escaped quoted strings.
    String repr = null;
    if (right instanceof Keyword) {
      repr = ((Keyword)right).value();

    } else if (right instanceof Anonymous) {
      repr = ((Anonymous)right).value();

    } else if (right instanceof Quoted) {
      Quoted temp = (Quoted)right;
      if (temp.escaped()) {
        repr = render(temp);
      }
    }
    return (repr == null) ? NOT_COMPARABLE : compareExact(render(left), repr);
  }

  /**
   * Compare two known colors.
   */
  private Comparison compareColorsExact(BaseColor left, BaseColor right) {
    RGBColor color0 = left.toRGB();
    RGBColor color1 = right.toRGB();
    return color0.red() == color1.red()
        && color0.green() == color1.green()
        && color0.blue() == color1.blue()
        && color0.alpha() == color1.alpha() ? EQUAL_TO : NOT_COMPARABLE;
  }

  /**
   * Compare two dimensions, optionally converting the units if necessary.
   */
  private Comparison compareDimensions(Dimension left, Dimension right) {
    Unit unit0 = left.unit();
    Unit unit1 = right.unit();
    unit0 = Unit.PERCENTAGE.equals(unit0) ? null : unit0;
    unit1 = Unit.PERCENTAGE.equals(unit1) ? null : unit1;

    double factor = 1.0;
    double value0 = left.value();
    double scaled = right.value();
    if (!LessUtils.safeEquals(unit0, unit1)) {
      factor = UnitConversions.factor(unit1, unit0);
      if (factor == 0.0) {
        return NOT_COMPARABLE;
      }
      scaled *= factor;
    }
    return value0 < scaled ? LESS_THAN : (value0 > scaled ? GREATER_THAN : EQUAL_TO);
  }

  /**
   * Compares two quoted strings, ignoring their delimiters.
   */
  private Comparison compareQuoted(Quoted left, Quoted right) {
    if (left.escaped() != right.escaped()) {
      if (left.delimiter() != right.delimiter()) {
        right = new Quoted(left.delimiter(), false, right.parts());
        return compareOrdered(render(left), render(right));
      }
    }
    return compareOrdered(render(left), render(right));
  }

  /**
   * Compares two known expressions.
   */
  private Comparison compareExpressions(Expression left, Expression right) throws LessException {
    return compareLists(left.values(), right.values());
  }

  /**
   * Compares two known expression lists.
   */
  private Comparison compareExpressionLists(ExpressionList left, ExpressionList right) throws LessException {
    return compareLists(left.expressions(), right.expressions());
  }

  /**
   * Compare the two lists, checking each pair of nodes for equivalence.
   */
  private Comparison compareLists(List<Node> left, List<Node> right) throws LessException {
    if (left == null || right == null) {
      return NOT_COMPARABLE;
    }

    int size = left.size();
    if (size != right.size()) {
      return NOT_COMPARABLE;
    }

    for (int i = 0; i < size; i++) {
      if (compare(left.get(i), right.get(i)) != EQUAL_TO) {
        return NOT_COMPARABLE;
      }
    }
    return EQUAL_TO;
  }

  /**
   * Compares two strings lexicographically, returning a {@link Comparison} result.
   */
  private Comparison compareOrdered(String left, String right) {
    int result = left.compareTo(right);
    return result < 0 ? LESS_THAN : (result > 0 ? GREATER_THAN : EQUAL_TO);
  }

  /**
   * Compare two strings exactly, returning a {@link Comparison} result.
   */
  private Comparison compareExact(String left, String right) {
    return left.compareTo(right) == 0 ? EQUAL_TO : NOT_COMPARABLE;
  }

  /**
   * Inverts the {@link Comparison} if necessary.
   */
  private Comparison invert(Comparison result) {
    return result == LESS_THAN ? GREATER_THAN : (result == GREATER_THAN ? LESS_THAN : result);
  }

  /**
   * Renders a node to string, with shortcuts for nodes with embedded strings.
   */
  private String render(Node node) {
    switch (node.type()) {
      case ANONYMOUS:
        return ((Anonymous)node).value();

      case KEYWORD:
      case TRUE:
      case FALSE:
        return ((Keyword)node).value();

      case COLOR:
      {
        RGBColor color = ((BaseColor)node).toRGB();

        // Compare colors by keyword
        if (color.fromKeyword()) {
          return Colors.colorToName(color);
        }

        // If alpha != 1.0 we can't represent this color as a hex-6 string.
        if (color.alpha() != 1.0) {
          return context.render(color);
        }

        // force hex-6 representation for color -> string comparisons
        Buffer buf = context.acquireBuffer();
        int red = color.red();
        int green = color.green();
        int blue = color.blue();

        buf.append('#');
        buf.append(hexchar(red >> 4)).append(hexchar(red & 0xF));
        buf.append(hexchar(green >> 4)).append(hexchar(green & 0xF));
        buf.append(hexchar(blue >> 4)).append(hexchar(blue & 0xF));

        String repr = buf.toString();
        context.returnBuffer();
        return repr;
      }

      default:
        return context.render(node);
    }
  }

}
