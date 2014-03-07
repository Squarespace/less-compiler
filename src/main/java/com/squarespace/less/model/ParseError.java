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
public class ParseError extends BaseNode {

  private Path filePath;

  private String errorMessage;

  public Path filePath() {
    return filePath;
  }

  public String errorMessage() {
    return errorMessage;
  }

  public void filePath(Path path) {
    this.filePath = path;
  }

  public void errorMessage(String message) {
    this.errorMessage = message;
  }

  @Override
  public NodeType type() {
    return NodeType.PARSE_ERROR;
  }

}
