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

package com.squarespace.less.exec;

import java.util.ArrayList;
import java.util.List;

import com.squarespace.less.Context;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.LessInternalException;


/**
 * Holds a list of reusable buffer instances. Callers must acquire and
 * then return a buffer.
 */
public class BufferStack {

  private final List<Buffer> bufferList = new ArrayList<>();

  private final Context ctx;

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
      throw new LessInternalException("Serious error: buffer stack was not returned to zero: " + index);
    }
  }

}
