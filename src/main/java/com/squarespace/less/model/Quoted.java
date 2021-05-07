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

package com.squarespace.less.model;

import static com.squarespace.less.core.LessUtils.safeEquals;
import static com.squarespace.less.model.NodeType.QUOTED;

import java.util.ArrayList;
import java.util.List;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.LessUtils;
import com.squarespace.less.exec.ExecEnv;


/**
 * A quoted string which may contain embedded variables.
 */
public class Quoted implements Node {

  /**
   * Default capacity for the components in the string.
   */
  private static final int DEFAULT_CAPACITY = 3;

  /**
   * Delimiter for the quoted string, either \' or \"
   */
  protected final char delim;

  /**
   * Components that make up the string. Some of these may be variable
   * references.
   */
  protected List<Node> parts;

  /**
   * Indicates whether the string is escaped. If true, it will be emitted
   * without the start/end delimiters.
   */
  protected boolean escaped;

  /**
   * Indicates whether any of the parts require evaluation.
   */
  protected boolean evaluate;

  /**
   * Constructs an empty string with the given delimiter character.
   */
  public Quoted(char delim) {
    this(delim, false);
  }

  /**
   * Constructs an empty string with the given delimiter character, and setting
   * the value for the escape flag.
   */
  public Quoted(char delim, boolean escape) {
    this(delim, escape, null);
  }

  /**
   * Constructs an empty string with the given delimiter character, setting
   * the value for the escape flag, and setting the initial parts.
   */
  public Quoted(char delim, boolean escape, List<Node> parts) {
    this.delim = delim;
    this.escaped = escape;
    this.parts = parts;
    if (parts != null) {
      int size = parts.size();
      for (int i = 0; i < size; i++) {
        Node part = parts.get(i);
        evaluate |= part.needsEval();
      }
    }
  }

  /**
   * Returns the delimiter character.
   */
  public char delimiter() {
    return delim;
  }

  /**
   * Indicates whether the string is escaped. See {@link #escaped}.
   */
  public boolean escaped() {
    return escaped;
  }

  /**
   * Returns the list of component parts for the string.
   */
  public List<Node> parts() {
    return parts;
  }

  /**
   * Sets the escape flag.
   */
  public void setEscape(boolean flag) {
    this.escaped = flag;
  }

  /**
   * Appends a part to the string.
   */
  public void append(Node node) {
    this.parts = LessUtils.initList(parts, DEFAULT_CAPACITY);
    this.parts.add(node);
    this.evaluate |= node.needsEval();
  }

  /**
   * Copies the string.
   */
  public Quoted copy() {
    Quoted res = new Quoted(delim);
    int size = parts.size();
    for (int i = 0; i < size; i++) {
      Node part = parts.get(i);
      res.append(part);
    }
    return res;
  }

  /**
   * See {@link Node#type()}
   */
  @Override
  public NodeType type() {
    return QUOTED;
  }

  /**
   * See {@link Node#needsEval()}
   */
  @Override
  public boolean needsEval() {
    return evaluate;
  }

  /**
   * See {@link Node#eval(ExecEnv)}
   */
  @Override
  public Node eval(ExecEnv env) throws LessException {
    if (!evaluate) {
      return this;
    }
    int size = parts.size();
    List<Node> result = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      result.add(parts.get(i).eval(env));
    }
    return new Quoted(delim, escaped, result);
  }

  /**
   * See {@link Node#repr(Buffer)}
   */
  @Override
  public void repr(Buffer buf) {
    if (escaped) {
      buf.append('~');
    }
    buf.append(delim);
    int size = parts.size();
    for (int i = 0; i < size; i++) {
      parts.get(i).repr(buf);
    }
    buf.append(delim);
  }

  /**
   * See {@link Node#modelRepr(Buffer)}
   */
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    posRepr(buf);
    buf.append(" delim=").append(delim);
    if (escaped) {
      buf.append(" [escaped]");
    }
    buf.append('\n').incrIndent();
    ReprUtils.modelRepr(buf, "\n", true, parts);
    buf.decrIndent();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Quoted) {
      Quoted other = (Quoted)obj;
      return delim == other.delim && escaped == other.escaped && safeEquals(parts, other.parts);
    }
    return false;
  }

  @Override
  public String toString() {
    return ModelUtils.toString(this);
  }

  @Override
  public int hashCode() {
    return ModelUtils.notHashable();
  }

}
