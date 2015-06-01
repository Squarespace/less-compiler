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

package com.squarespace.less.plugins.ext;

import static com.squarespace.less.core.ExecuteErrorMaker.patternCompile;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.squarespace.less.LessException;
import com.squarespace.less.exec.ExecEnv;
import com.squarespace.less.exec.Function;
import com.squarespace.less.exec.Registry;
import com.squarespace.less.model.Anonymous;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Quoted;


/**
 * String extension function implementations.
 *
 * These are less safe given that unconstrained inputs can adversely
 * affect execution times in a server environment.  For that
 * reason they reside in this optional extension package, which can
 * be enabled only in contexts where inputs are known to be safe.
 */
public class ExtStringFunctions implements Registry<Function> {

  /**
   * WARNING: this is current experimental.
   *
   * Inherently unsafe in certain contexts since argument 2 is a regular
   * expression pattern to be compiled and applied to argument 1.
   */
  public static final Function REPLACE = new Function("replace", "*s*:s") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      env.addWarning("use of replace() is currently experimental");
      Quoted stringArg = (Quoted)args.get(0);
      String string = render(env, stringArg);
      String replacement = render(env, (Quoted)args.get(2));

      Pattern pattern = compile(env, (Quoted)args.get(1));
      Anonymous output = new Anonymous(pattern.matcher(string).replaceAll(replacement));
      return new Quoted(stringArg.delimiter(), stringArg.escaped(), Arrays.<Node>asList(output));
    }
  };

  private static String render(ExecEnv env, Node node) throws LessException {
    // TODO: fix assumption that input is a string. we should render any renderable node
    // to obtain the intermediate strings.
    Quoted string = (Quoted)node;
    string = string.copy();
    string.setEscape(true);
    return env.context().render(string);
  }

  private static Pattern compile(ExecEnv env, Quoted pattern) throws LessException {
    pattern = pattern.copy();
    pattern.setEscape(true);
    try {
      return Pattern.compile(env.context().render(pattern));
    } catch (PatternSyntaxException e) {
      throw new LessException(patternCompile(e.getMessage()));
    }
  }
}
