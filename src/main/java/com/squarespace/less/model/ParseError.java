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

import java.nio.file.Path;


/**
 * A vehicle to pass parser errors up through the execution chain.
 * Used when a template being executed imports another template which
 * fails to parse.  We need to show the execution context, followed by
 * the full parse error.  This node lets us pass the full parse error
 * message.
 */
public class ParseError implements Node {

  /**
   * Path of the file in which this parse error occurred.
   */
  protected Path filePath;

  /**
   * Error message.
   */
  protected String errorMessage;

  /**
   * Path to the file in which this parse error occurred.
   */
  public Path filePath() {
    return filePath;
  }

  /**
   * Error message.
   */
  public String errorMessage() {
    return errorMessage;
  }

  /**
   * Sets the path to the file in which the error occurred.
   */
  public void filePath(Path path) {
    this.filePath = path;
  }

  /**
   * Sets the error message.
   */
  public void errorMessage(String message) {
    this.errorMessage = message;
  }

  /**
   * See {@link Node#type()}
   */
  @Override
  public NodeType type() {
    return NodeType.PARSE_ERROR;
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
