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

import static com.squarespace.less.core.ExecuteErrorMaker.formatFunctionArgs;

import java.util.List;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.CharClass;
import com.squarespace.less.core.EncodeUtils;
import com.squarespace.less.exec.ExecEnv;
import com.squarespace.less.exec.Function;
import com.squarespace.less.exec.Registry;
import com.squarespace.less.exec.SymbolTable;
import com.squarespace.less.model.Anonymous;
import com.squarespace.less.model.BaseColor;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.NodeType;
import com.squarespace.less.model.Quoted;
import com.squarespace.less.model.RGBColor;


/**
 * String function implementations.
 *
 * http://lesscss.org/functions/#string-functions
 */
public class StringFunctions implements Registry<Function> {

  public static final Function E = new Function("e", "s") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      Quoted str = (Quoted)args.get(0);
      str = new Quoted(str.delimiter(), true, str.parts());
      return new Anonymous(env.context().render(str));
    }
  };

  public static final Function ESCAPE = new Function("escape", "s") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      String value = asString(env, args.get(0), true);
      return new Anonymous(EncodeUtils.escape(value));
    }
  };

  /**
   * See http://lesscss.org/#reference  "% format" section.
   */
  public static final Function FORMAT = new Function("%", "s.") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      Quoted orig = (Quoted)args.get(0);
      String format = asString(env, orig, true);

      Buffer buf = env.context().newBuffer();
      int size = format.length();
      int i = 0; // character index
      int j = 1; // argument index
      int formatters = 0;
      boolean error = false;
      while (i < size) {
        char ch = format.charAt(i);
        if (ch != '%') {
          buf.append(ch);
          i++;
          continue;
        }

        i++;
        if (i == size) {
          buf.append('%');
          break;
        }
        ch = format.charAt(i);
        if (ch == '%') {
          buf.append('%');
          i++;
          continue;
        }
        formatters++;
        if (j >= args.size()) {
          i++;
          error = true;
          continue;
        }

        Node arg = args.get(j);
        if (arg.is(NodeType.COLOR)) {
          // Force representation of this color to always be hex, not keyword.
          RGBColor color = ((BaseColor)arg).toRGB().copy();
          color.forceHex(true);
          arg = color;
        }
        boolean escape = (ch == 's' || ch == 'S');
        String value = asString(env, arg, escape);
        if (CharClass.uppercase(ch)) {
          value = EncodeUtils.encodeURIComponent(value);
        }
        buf.append(value);
        i++;
        j++;
      }
      if (error) {
        throw new LessException(formatFunctionArgs(formatters, args.size() - 1));
      }
      Quoted result = new Quoted(orig.delimiter(), orig.escaped());
      result.append(new Anonymous(buf.toString()));
      return result;
    }
  };

  // TODO: REPLACE

  @Override
  public void registerTo(SymbolTable<Function> table) {
    // NO-OP
  }

  private static String asString(ExecEnv env, Node node, boolean escape) throws LessException {
    if (escape && node.is(NodeType.QUOTED)) {
      Quoted str = (Quoted)node;
      str = str.copy();
      str.setEscape(true);
      node = str;
    }
    return env.context().render(node);
  }

}
