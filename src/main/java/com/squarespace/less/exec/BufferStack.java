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

import com.squarespace.less.LessContext;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.LessInternalException;


/**
 * Holds a list of reusable buffer instances. Callers must acquire and
 * then immediately return buffers for this to work.
 *
 * When processing a stylesheet, many nodes need to be rendered to an
 * intermediate buffer which is then copied to the main stream. Rather
 * than construct these on the fly this stack keeps reusable instances.
 */
public class BufferStack {

  /**
   * List of buffers that have been allocated.
   */
  private final List<Buffer> bufferList = new ArrayList<>();

  /**
   * Context used to create new {@link Buffer} instances.
   */
  private final LessContext ctx;

  /**
   * Index of the buffer currently in use.
   */
  private int index;

  /**
   * Constructs a stack with the given context.
   */
  public BufferStack(LessContext ctx) {
    this.ctx = ctx;
  }

  /**
   * Acquire a reusable buffer to render nodes.
   */
  public Buffer acquireBuffer() {
    Buffer buf = null;
    if (index == bufferList.size()) {
      // Need to grow the list.
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

  /**
   * Return a buffer to the list.
   */
  public void returnBuffer() {
    index--;
  }

  /**
   * Asserts that the internal stack's state is valid.
   */
  public void sanityCheck() {
    if (index != 0) {
      throw new LessInternalException("Serious error: buffer stack was not returned to zero: " + index);
    }
  }

}
