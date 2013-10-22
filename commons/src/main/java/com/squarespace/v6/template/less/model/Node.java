package com.squarespace.v6.template.less.model;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.Buffer;
import com.squarespace.v6.template.less.exec.ExecEnv;


public interface Node {

  NodeType type();

  int lineOffset();

  int charOffset();
  
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

  boolean is(NodeType type);
  
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
