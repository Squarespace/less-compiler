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
import com.squarespace.less.model.Node;


/**
 * Dummy functions, only used for testing the general Function framework.
 */
public class DummyFunctions implements Registry<Function> {

  public static final Function DUMMY3 = new Function("dummy3", "nnn") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      double n0 = number(args.get(0));
      double n1 = number(args.get(1));
      double n2 = number(args.get(2));
      return new Dimension(n0 + n1 + n2);
    }
  };

  @Override
  public void registerTo(SymbolTable<Function> table) {
    // NO-OP
  }

}
