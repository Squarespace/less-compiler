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
import com.squarespace.less.model.Unit;


/**
 * Math function implementations.
 *
 * http://lesscss.org/functions/#math-functions
 */
public class MathFunctions implements Registry<Function> {

  // TODO: ABS

  // TODO: ASIN

  // TODO: ACOS

  // TODO: ATAN

  public static final Function CEIL = new Function("ceil", "d") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      Dimension dim = (Dimension)args.get(0);
      return new Dimension(Math.ceil(dim.value()), dim.unit());
    }
  };

  // TODO: COS

  public static final Function FLOOR = new Function("floor", "d") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      Dimension dim = (Dimension)args.get(0);
      return new Dimension(Math.floor(dim.value()), dim.unit());
    }
  };

  // TODO: MAX

  // TODO: MIN

  // TODO: MOD

  public static final Function PERCENTAGE = new Function("percentage", "d") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      Dimension dim = (Dimension)args.get(0);
      return new Dimension(dim.value() * 100, Unit.PERCENTAGE);
    }
  };

  // TODO: PI

  // TODO: POW


  public static final Function ROUND = new Function("round", "d:n") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      double places = 0.0;
      int size = args.size();
      Dimension dim = (Dimension)args.get(0);
      if (size == 2) {
        places = Math.max(((Dimension)args.get(1)).value(), 0);
      }
      double scale = Math.pow(10, places);
      return new Dimension(Math.round(dim.value() * scale) / scale, dim.unit());
    }
  };

  // TODO: SIN

  // TODO: SQRT

  // TODO: TAN

  @Override
  public void registerTo(SymbolTable<Function> table) {
   // NO-OP
  }

}
