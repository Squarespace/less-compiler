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

import java.util.ArrayDeque;
import java.util.Deque;

import com.squarespace.less.model.Node;


/**
 * Sole exception thrown by internals. Actual error type and internal stack are
 * part of the ErrorInfo structure.
 */
public class LessException extends Exception {

  static final long serialVersionUID = 1L;

  /** Collects additional context about where the primary error occurred */
  private final Deque<Node> errorContext = new ArrayDeque<>(5);

  private final ErrorInfo info;

  public LessException(ErrorInfo info) {
    super(info.getMessage());
    this.info = info;
  }

  public ErrorInfo primaryError() {
    return info;
  }

  public Deque<Node> errorContext() {
    return errorContext;
  }

  public void push(Node node) {
    errorContext.push(node);
  }

}
