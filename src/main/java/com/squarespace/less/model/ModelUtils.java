package com.squarespace.less.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.squarespace.less.core.Buffer;

public class ModelUtils {

  private ModelUtils() { }

  /**
   * Helper method to format a double value and append it to the buffer.
   */
  public static void formatDouble(Buffer buf, double value) {
    if (!Double.isFinite(value)) {
      buf.append("0");
      return;
    }

    long lval = (long)value;
    if (value == lval) {
      buf.append(lval);
    } else {
      // Strip trailing zeros and avoid scientific notation.
      String repr = BigDecimal.valueOf(value)
          .setScale(buf.numericScale(), RoundingMode.HALF_EVEN)
          .stripTrailingZeros()
          .toPlainString();
      // Strip leading zeros for positive and negative numbers.
      if (value > 0 && value < 1.0) {
        buf.append(repr.substring(1));
      } else if (value > -1.0 && value < 0 && repr.startsWith("-")) {
        buf.append('-').append(repr.substring(2));
      } else {
        buf.append(repr);
      }
    }
  }

  public static int notHashable() {
    throw new UnsupportedOperationException("Serious error: model objects are not designed to be hashed.");
  }

  /**
   * Return a string representation of a node.
   */
  public static String toString(Node node) {
    Buffer buf = new Buffer(4);
    node.modelRepr(buf);
    return buf.toString();
  }
}
