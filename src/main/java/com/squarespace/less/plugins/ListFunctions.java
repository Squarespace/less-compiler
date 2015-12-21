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
import com.squarespace.less.exec.ExecEnv;
import com.squarespace.less.exec.Function;
import com.squarespace.less.exec.Registry;
import com.squarespace.less.exec.SymbolTable;
import com.squarespace.less.model.Dimension;
import com.squarespace.less.model.ExpressionList;
import com.squarespace.less.model.Node;


/**
 * List function implementations.
 *
 * http://lesscss.org/functions/#list-functions
 */
public class ListFunctions implements Registry<Function> {

  @Override
  public void registerPlugins(SymbolTable<Function> table) {
    table.add(EXTRACT);
    table.add(LENGTH);
  }

  public static final Function LENGTH = new Function("length", "*.") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      Node node = args.get(0);
      int size = 1;
      if (node instanceof ExpressionList) {
        ExpressionList list = (ExpressionList) node;
        size = list.size();
      }
      return new Dimension(size);
    }
  };

  /**
   * Note: I'm not satisfied with the vague behavior exhibited by the upstream
   * JS compiler. If length() is called with 2 or more arguments of the wrong
   * type, it tends to output the literal function call as a default,
   * rather than emitting a warning, or better and error.
   *
   * In order to maintain compatibility with upstream I've replicated this
   * behavior.
   */
  public static final Function EXTRACT = new Function("extract", "**.") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      Node arg1 = args.get(0);
      Node arg2 = args.get(1);
      if (!(arg1 instanceof ExpressionList) || !(arg2 instanceof Dimension)) {
        // Bail out and emit literal representation of function call.
        return null;
      }
      ExpressionList list = (ExpressionList) arg1;
      int size = list.size();
      double index = ((Dimension) arg2).value();
      if (index != Math.round(index) || index < 0 || index >= size) {
        // Bail out and emit literal representation of function call.
        return null;
      }
      return list.expressions().get((int)index);
    }
  };

}
