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

import static com.squarespace.less.core.Constants.FALSE;
import static com.squarespace.less.core.Constants.TRUE;

import java.util.List;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Constants;
import com.squarespace.less.exec.ExecEnv;
import com.squarespace.less.exec.Function;
import com.squarespace.less.exec.Registry;
import com.squarespace.less.model.BaseColor;
import com.squarespace.less.model.Dimension;
import com.squarespace.less.model.Keyword;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Quoted;
import com.squarespace.less.model.Unit;
import com.squarespace.less.model.Url;


/**
 * Type function implementations.
 *
 * http://lesscss.org/functions/#type-functions
 */
public class TypeFunctions implements Registry<Function> {

  public static final Function ISCOLOR = new Function("iscolor", "*") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      return (args.get(0) instanceof BaseColor) ? TRUE : FALSE;
    }
  };

  public static final Function ISEM = new DimensionUnitFunction("isem", Unit.EM);

  public static final Function ISKEYWORD = new Function("iskeyword", "*") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      switch (args.get(0).type()) {
        case FALSE:
        case KEYWORD:
        case TRUE:
          return Constants.TRUE;
        default:
          return Constants.FALSE;
      }
    }
  };

  public static final Function ISNUMBER = new Function("isnumber", "*") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      return (args.get(0) instanceof Dimension) ? TRUE : FALSE;
    }
  };

  public static final Function ISPERCENTAGE = new DimensionUnitFunction("ispercentage", Unit.PERCENTAGE);

  public static final Function ISPIXEL = new DimensionUnitFunction("ispixel", Unit.PX);

  // TODO: ISRULESET

  public static final Function ISSTRING = new Function("isstring", "*") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      return (args.get(0) instanceof Quoted) ? TRUE : FALSE;
    }
  };

  public static final Function ISUNIT = new Function("isunit", "**") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      Node arg = args.get(0);
      if (!(arg instanceof Dimension)) {
        return FALSE;
      }

      Dimension dim = (Dimension) args.get(0);
      Node node = args.get(1);
      Unit unit = null;

      // TODO: generalize this keyword/quoted/anonymous parsing into a utility method.

      if (node instanceof Keyword) {
        String value = ((Keyword)node).value();
        unit = Unit.get(value);

      } else if (node instanceof Quoted) {
        Quoted temp = (Quoted) node;
        Quoted string = new Quoted(temp.delimiter(), true, temp.parts());
        unit = Unit.get(env.context().render(string));

      } else {
        return FALSE;
      }
      return (unit.equals(dim.unit())) ? TRUE : FALSE;
    }
  };

  public static final Function ISURL = new Function("isurl", "*") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      return (args.get(0) instanceof Url) ? TRUE : FALSE;
    }
  };

  private static class DimensionUnitFunction extends Function {

    private Unit unit;

    public DimensionUnitFunction(String name, Unit unit) {
      super(name, "*");
      this.unit = unit;
    }

    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      Node arg = args.get(0);
      if (arg instanceof Dimension) {
        Dimension dim = (Dimension)arg;
        return dim.unit() == unit ? TRUE : FALSE;
      }
      return FALSE;
    }
  }

}
