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

import static com.squarespace.less.core.ExecuteErrorMaker.unknownUnit;

import java.util.Arrays;
import java.util.List;

import com.squarespace.less.LessException;
import com.squarespace.less.exec.ExecEnv;
import com.squarespace.less.exec.Function;
import com.squarespace.less.exec.Registry;
import com.squarespace.less.model.Anonymous;
import com.squarespace.less.model.Dimension;
import com.squarespace.less.model.Keyword;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Quoted;
import com.squarespace.less.model.RGBColor;
import com.squarespace.less.model.Unit;
import com.squarespace.less.model.UnitConversions;
import com.squarespace.less.model.Units;


/**
 * Misc function implementations.
 *
 * http://lesscss.org/functions/#misc-functions
 */
public class MiscFunctions implements Registry<Function> {

  public static final Function COLOR = new Function("color", "s") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      Quoted str = (Quoted)args.get(0);
      str = str.copy();
      str.setEscape(true);
      String repr = env.context().render(str);
      return RGBColor.fromHex(repr);
    }
  };

  public static final Function CONVERT = new Function("convert", "d*") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      Dimension dim = (Dimension)args.get(0);
      Unit destUnit = toUnit(env, args.get(1));
      double factor = UnitConversions.factor(dim.unit(), destUnit);
      return new Dimension(dim.value() * factor, destUnit);
    }
  };

  // TODO: DATA-URI

  // TODO: DEFAULT (? maybe a special, since only used in guard expressions)

  public static final Function GET_UNIT = new Function("get-unit", "d") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      Dimension dim = (Dimension)args.get(0);
      Unit unit = dim.unit();
      if (unit == null) {
        return new Anonymous();
      } else if (Units.PERCENTAGE.equals(unit)) {
        return new Quoted('"', false, Arrays.<Node>asList(new Anonymous("%")));
      }
      return new Keyword(unit.repr());
    }
  };

  // TODO: SVG-GRADIENT

  public static final Function UNIT = new Function("unit", "d:*") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      Dimension dim = (Dimension)args.get(0);
      Unit unit = null;
      if (args.size() == 2) {
        unit = toUnit(env, args.get(1));
      }
      return new Dimension(dim.value(), unit);
    }
  };

  private static Unit toUnit(ExecEnv env, Node node) throws LessException {
    Unit unit = null;
    if (node instanceof Keyword) {
      unit = Unit.get(((Keyword)node).value());

    } else if (node instanceof Quoted) {
      Quoted quoted = (Quoted)node;
      quoted = new Quoted(quoted.delimiter(), true, quoted.parts());
      String repr = env.context().render(quoted);
      unit = Unit.get(repr);
    }

    if (unit == null) {
      throw new LessException(unknownUnit(node.repr()));
    }
    return unit;
  }

}
