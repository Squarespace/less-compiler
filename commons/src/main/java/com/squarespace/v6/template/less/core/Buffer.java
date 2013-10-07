package com.squarespace.v6.template.less.core;

import com.squarespace.v6.template.less.Options;


/**
 * General-purpose buffer. Wraps a StringBuilder with some custom LESS-related output methods.
 */
public class Buffer {

  private StringBuilder buf;
  
  private char delim = Chars.EOF;
  
  private int indentSize;
  
  private int indentCurr;

  private boolean compress;
  
  private char prev = Chars.LINE_FEED;
  
  public Buffer(Options opts) {
    this(opts.indent(), opts.compress());
  }
  
  public Buffer(int indentSize) {
    this(indentSize, false, new StringBuilder());
  }
  
  public Buffer(int indentSize, boolean compress) {
    this(indentSize, compress, new StringBuilder());
  }
  
  private Buffer(int indentSize, boolean compress, StringBuilder buf) {
    this.indentSize = indentSize;
    this.compress = compress;
    this.buf = buf;
  }
  
  /**
   * Return a fresh buffer with the same initialization options as this one.
   */
  public Buffer newBuffer() {
    return new Buffer(indentSize, compress);
  }
  
  public boolean compress() {
    return compress;
  }
  
  public int length() {
    return buf.length();
  }
  
  public void reset() {
    if (buf.length() != 0) {
      buf.setLength(0);
    }
    delim = Chars.EOF;
    prev = Chars.LINE_FEED;
  }
  
  public Buffer incrIndent() {
    this.indentCurr += indentSize;
    return this;
  }
  
  public Buffer decrIndent() {
    this.indentCurr -= indentSize;
    return this;
  }
  
  public Buffer resetIndent() {
    this.indentCurr = 0;
    return this;
  }
  
  public boolean inEscape() {
    return delim != Chars.EOF;
  }

  public char prevChar() {
    return prev;
  }

  public Buffer indent() {
    for (int i = 0; i < indentCurr; i++) {
      buf.append(' ');
    }
    if (indentCurr != 0) {
      prev = ' ';
    }
    return this;
  }
  
  public Buffer startDelim(char ch) {
    if (inEscape()) {
      throw new RuntimeException("Buffer already in escape mode for " + delim);
    }
    this.delim = ch;
    return this;
  }
  
  public Buffer endDelim() {
    this.delim = Chars.EOF;
    return this;
  }
  
  public Buffer append(long num) {
    buf.append(num);
    prev = buf.charAt(buf.length()-1);
    return this;
  }
  
  public Buffer append(double num) {
    buf.append(num);
    prev = buf.charAt(buf.length()-1);
    return this;
  }
  
  public Buffer append(char ch) {
    buf.append(ch);
    prev = ch;
    return this;
  }
  
  public Buffer append(String str) {
    if (str == null) {
      return this;
    }
    int len = str.length();
    if (len == 0) {
      return this;
    }
    buf.append(str);
    prev = str.charAt(len - 1);
    return this;
  }

  public void blockOpen() {
    if (compress) {
      append('{');
    } else {
      append(" {\n");
      incrIndent();
    }
  }
  
  public void blockClose() {
    if (!compress) {
      decrIndent();
      indent();
    }
    append("}\n");
  }
  
  public Buffer listSep() {
    if (compress) {
      append(',');
    } else {
      append(", ");
    }
    return this;
  }
  
  public void ruleEnd() {
    append(';');
    if (!compress) {
      append('\n');
    }
  }
  
  public void ruleSep() {
    if (compress) {
      append(':');
    } else {
      append(": ");
    }
  }
  
  public void selectorSep() {
    if (compress) {
      append(',');
    } else {
      append(",\n");
    }
  }
  
  @Override
  public String toString() {
    return buf.toString();
  }

}
