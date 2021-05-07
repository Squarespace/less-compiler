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

import com.squarespace.less.LessException;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.ExecuteErrorMaker;
import com.squarespace.less.exec.ExecEnv;


/**
 * Methods common to all nodes.
 */
public interface Node {

  /**
   * The enumerated type of this node.
   */
  NodeType type();

  /**
   * Constructs the type representation for this node.
   */
  default void typeRepr(Buffer buf) {
    buf.append(type().toString());
  }

  /**
   * Constructs the position representation for this node.
   */
  default void posRepr(Buffer buf) {
    // Nothing
  }

  /**
   * Indicates if this is a structural node.
   */
  default boolean isStructural() {
    return false;
  }

  /**
   * Returns the original LESS representation of this node as a string.
   */
  default String repr() {
    Buffer buf = new Buffer(2);
    repr(buf);
    return buf.toString();
  }

  /**
   * Writes the original LESS representation of this node to the given {@link Buffer}.
   */
  default void repr(Buffer buf) {
    buf.append("<no repr for " + type().toString() + ">");
  }

  /**
   * Outputs a human-readable representation of the model node.
   */
  default void modelRepr(Buffer buf) {
    typeRepr(buf);
    posRepr(buf);
    buf.append("<not implemented>");
  }

  /**
   * Evaluates the node against the Frame and returns itself or a new Node
   * that is itself an Atom, e.g. reduced to its simplest form possible.
   */
  default Node eval(ExecEnv env) throws LessException {
    return this;
  }

  /**
   * Optimization to avoid eval() when the node is an Atom or a composite
   * that contains no variable references.
   */
  default boolean needsEval() {
    return false;
  }

  /**
   * Called when a node participates on an operation.
   */
  default Node operate(ExecEnv env, Operator op, Node arg) throws LessException {
    NodeType argType = (arg == null) ? null : arg.type();
    throw new LessException(ExecuteErrorMaker.invalidOperation(op, type(), argType));
  }

}
