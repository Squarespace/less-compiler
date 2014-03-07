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

package com.squarespace.less.model;

import static com.squarespace.less.core.LessUtils.safeEquals;

import java.util.List;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.LessInternalException;
import com.squarespace.less.core.LessUtils;
import com.squarespace.less.exec.ExecEnv;


public class MixinCallArgs extends BaseNode {

  private final char delimiter;

  private List<Argument> args;

  private boolean evaluate;

  public MixinCallArgs(char delimiter) {
    this.delimiter = delimiter;
  }

  public char delim() {
    return delimiter;
  }

  public List<Argument> args() {
    return LessUtils.safeList(args);
  }

  public void add(Argument arg) {
    if (arg == null) {
      throw new LessInternalException("Serious error: arg cannot be null.");
    }
    args = LessUtils.initList(args, 3);
    args.add(arg);
    evaluate |= arg.needsEval();
  }

  public boolean isEmpty() {
    return args().isEmpty();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof MixinCallArgs) {
      MixinCallArgs other = (MixinCallArgs)obj;
      return delimiter == other.delimiter && safeEquals(args, other.args);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public NodeType type() {
    return NodeType.MIXIN_ARGS;
  }

  @Override
  public boolean needsEval() {
    return evaluate;
  }

  @Override
  public Node eval(ExecEnv env) throws LessException {
    if (!evaluate) {
      return this;
    }
    MixinCallArgs res = new MixinCallArgs(delimiter);
    for (Argument arg : args) {
      res.add((Argument)arg.eval(env));
    }
    return res;
  }

  @Override
  public void repr(Buffer buf) {
    buf.append('(');
    if (args != null) {
      int size = args.size();
      for (int i = 0; i < size; i++) {
        if (i > 0) {
          buf.append(delimiter).append(' ');
        }
        args.get(i).repr(buf);
      }
    }
    buf.append(')');
  }

  @Override
  public void modelRepr(Buffer buf) {
    buf.append(type().toString()).append(" delim=").append(delimiter).append('\n');
    buf.incrIndent();
    ReprUtils.modelRepr(buf, "\n", true, args);
    buf.decrIndent();
  }

}
