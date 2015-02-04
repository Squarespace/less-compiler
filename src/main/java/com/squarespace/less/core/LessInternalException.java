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

package com.squarespace.less.core;


/**
 * Represents a serious internal exception, indicating a probable bug within
 * the framework itself.
 */
public class LessInternalException extends RuntimeException {

  static final long serialVersionUID = 1L;

  public LessInternalException(String message) {
    super(message);
  }

  public LessInternalException(String message, Throwable cause) {
    super(message, cause);
  }

}
