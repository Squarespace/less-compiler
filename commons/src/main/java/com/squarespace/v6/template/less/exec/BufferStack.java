package com.squarespace.v6.template.less.exec;

import java.util.ArrayList;
import java.util.List;

import com.squarespace.v6.template.less.Context;
import com.squarespace.v6.template.less.core.Buffer;


/**
 * Holds a list of reusable buffer instances. Callers must acquire and
 * then return a buffer.
 */
public class BufferStack {

  private Context ctx;
  
  private List<Buffer> bufferList = new ArrayList<>();
  
  private int index;

  public BufferStack(Context ctx) {
    this.ctx = ctx;
  }
  
  /**
   * Acquire a reusable buffer to render nodes.
   */
  public Buffer acquireBuffer() {
    // Need to grow the list.
    Buffer buf = null;
    if (index == bufferList.size()) {
      buf = ctx.newBuffer();
      bufferList.add(buf);

    } else {
      // Reuse a pre-allocated buffer in the list
      buf = bufferList.get(index);
      buf.reset();
    }

    index++;
    return buf;
  }
  
  public void returnBuffer() {
    index--;
  }
  
  public void sanityCheck() {
    if (index != 0) {
      throw new RuntimeException("Serious error: buffer stack was not returned to zero: " + index);
    }
  }

}
