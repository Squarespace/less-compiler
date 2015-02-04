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
import com.squarespace.less.exec.ExecEnv;


public interface Node {

  NodeType type();

  int lineOffset();

  int charOffset();

  Object userData();

  void userData(Object obj);

  /**
   * Outputs the original LESS representation of this node.
   */
  void repr(Buffer buf);

  String repr();

  /**
   * Outputs a human-readable representation of the model node.
   */
  void modelRepr(Buffer buf);

  /**
   * Evaluates the node against the Frame and returns itself or a new Node
   * that is itself an Atom, e.g. reduced to its simplest form possible.
   */
  Node eval(ExecEnv env) throws LessException;

  /**
   * Optimization to avoid eval() when the node is an Atom or a composite
   * that contains no variable references.
   */
  boolean needsEval();

  /**
   * Called when a node participates on an operation.
   */
  Node operate(ExecEnv env, Operator op, Node arg) throws LessException;

  void setLineOffset(int offset);

  void setCharOffset(int offset);

}
