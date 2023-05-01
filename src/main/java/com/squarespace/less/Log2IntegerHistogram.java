package com.squarespace.less;

import java.util.Arrays;

/**
 * Incremental base-2 logarithmic histogram of signed integers.
 */
public class Log2IntegerHistogram {

  // log2 buckets
  private final int[] counts = new int[32];
  private int max = 0;

  public void reset() {
    Arrays.fill(this.counts, 0);
    this.max = 0;
  }

  public void add(int v) {
    if (v > 0) {
      // number of leading zero bits indicate the log2 of the number
      int i = 31 - Integer.numberOfLeadingZeros(v);
      this.max = Math.max(this.max, i);
      this.counts[i]++;
    }
  }

  public void merge(Log2IntegerHistogram h) {
    this.max = Math.max(this.max, h.max);
    for (int i = 0; i < 32; i++) {
      this.counts[i] += h.counts[i];
    }
  }

  /**
   * Return a comma-delimited string containing buckets up to and including
   * the largest non-zero bucket. An empty string indicates no buckets have
   * been filled, therefore the last bucket in the string will always be
   * non-zero.
   */
  public String toString() {
    // Check if nothing has been added
    if (this.max == 0 && this.counts[0] == 0) {
      return "";
    }

    // Emit all buckets up to and including the highest non-zero bucket
    int lim = this.max + 1;
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < lim; i++) {
      if (i > 0) {
        buf.append(',');
      }
      if (counts[i] > 0) {
        buf.append(counts[i]);
      }
    }
    return buf.toString();
  }

}