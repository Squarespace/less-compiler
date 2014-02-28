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

  /** Time in nanoseconds */
  public long now() {
    return System.nanoTime();
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
