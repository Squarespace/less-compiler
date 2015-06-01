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

package com.squarespace.less.plugins;

import java.util.List;

import com.squarespace.less.LessException;
import com.squarespace.less.core.LessUtils;
import com.squarespace.less.exec.ExecEnv;
import com.squarespace.less.exec.Function;
import com.squarespace.less.exec.Registry;
import com.squarespace.less.model.Dimension;
import com.squarespace.less.model.Node;


/**
 * List function implementations.
 *
 * http://lesscss.org/functions/#list-functions
 */
public class ListFunctions implements Registry<Function> {

  public static final Function LENGTH = new Function("length", "*.") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      Node arg = args.get(0);
      List<Node> values = LessUtils.listValues(arg);
      return (values == null) ? new Dimension(1) : new Dimension(values.size());
    }
  };

  /**
   * NOTE: Functions called with arguments of the wrong type will emit the
   * literal representation of the function call, rather than emitting
   * a warning or error.
   *
   * In order to maintain compatibility with upstream I've replicated this
   * behavior.
   * TODO: revisit this to ensure the behavior is properly documented somewhere.
   * - phensley
   */
  public static final Function EXTRACT = new Function("extract", "**.") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      Node arg1 = args.get(0);
      Node arg2 = args.get(1);

      // Indices must be a number.
      if (!(arg2 instanceof Dimension)) {
        return null;
      }

      // Indices are 1-based integers. Verify integer-ness and subtract 1 to
      // get true index.
      double index = ((Dimension) arg2).value();
      index--;
      if (index != (int)index || index < 0) {
        return null;
      }

      // Extract values for list types.
      List<Node> values = LessUtils.listValues(arg1);
      if (values == null) {
        // Treat non-list types as list of size 1.
        return index == 0 ? arg1 : null;
      }

      return (index >= values.size()) ? null : values.get((int)index);
    }
  };

}
