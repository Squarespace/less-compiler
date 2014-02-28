package com.squarespace.less.core;

import java.nio.file.Path;

import com.squarespace.less.Context;
import com.squarespace.less.LessException;


public class ErrorUtils {

  private static final int STACK_FRAME_WINDOW = 6;
  
  /**
   * Formats an error message including a full stack trace.
   */
  public static String formatError(Context ctx, Path mainPath, LessException exc, int indent) {
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
