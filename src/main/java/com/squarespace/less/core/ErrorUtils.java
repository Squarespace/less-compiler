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

import java.nio.file.Path;

import com.squarespace.less.LessContext;
import com.squarespace.less.LessException;


public class ErrorUtils {

  private static final int STACK_FRAME_WINDOW = 6;

  private ErrorUtils() {
  }

  /**
   * Formats an error message including a full stack trace.
   */
  public static String formatError(LessContext ctx, Path mainPath, LessException exc, int indent) {
    Buffer buf = ctx.acquireBuffer();
    buf.append("An error occurred in '" + mainPath + "':\n\n");
    StackFormatter fmt = new StackFormatter(exc.errorContext(), 4, STACK_FRAME_WINDOW);
    buf.append(fmt.format()).append('\n');
    buf.append(exc.primaryError().getMessage());
    String result = buf.toString();
    ctx.returnBuffer();
    return result;
  }

}
