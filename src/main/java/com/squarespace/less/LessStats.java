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


/**
 * Collects timing and stats for one execution of the LESS compiler.
 */
public class LessStats {

  private static final double FACTOR = 1000000.0;

  private double parseTimeMs;

  private double compileTimeMs;

  private double diskWaitTimeMs;

  private int importCount;

  private int fileReads;

  private int totalSize;

  private int maxImportDepth;

  private int maxMixinDepth;

  /** Time spent parsing (including imports) */
  public double parseTimeMs() {
    return parseTimeMs;
  }

  /** Time spent compiling (including imports) */
  public double compileTimeMs() {
    return compileTimeMs;
  }

  /** Time spent waiting for disk I/O of imported files */
  public double diskWaitTimeMs() {
    return diskWaitTimeMs;
  }

  /** Number of imports, including cached references */
  public int importCount() {
    return importCount;
  }

  /** Number of files read from disk */
  public int fileReads() {
    return fileReads;
  }

  /** Total input size, in number of characters */
  public int totalSize() {
    return totalSize;
  }

  /** Maximum import recursion depth reached */
  public int maxImportDepth() {
    return maxImportDepth;
  }

  /** Maximum mixin recursion depth reached */
  public int maxMixinDepth() {
    return maxMixinDepth;
  }

  /** Time in nanoseconds */
  public long now() {
    return System.nanoTime();
  }

  /** Sets maximum import depth reached for this compile */
  public void importDepth(int depth) {
    this.maxImportDepth = Math.max(this.maxImportDepth, depth);
  }

  /** Sets maximum mixin depth reached for this compile */
  public void mixinDepth(int depth) {
    this.maxMixinDepth = Math.max(this.maxMixinDepth, depth);
  }

  /** Indicate that a parse operation is complete. */
  public void parseDone(int size, long started) {
    parseTimeMs += deltaMs(started);
  }

  /** Indicate that a compile operation is complete. */
  public void compileDone(long started) {
    compileTimeMs += deltaMs(started);
  }

  /** Indicate that disk I/O is complete */
  public void diskWaitDone(long started) {
    diskWaitTimeMs += deltaMs(started);
  }

  /**
   * Indicate that an import is complete, with a flag to indicate whether
   * the tree was cached or not.
   */
  public void importDone(boolean cached) {
    importCount++;
    if (!cached) {
      fileReads++;
    }
  }

  private double deltaMs(long started) {
    return (now() - started) / FACTOR;
  }

}
