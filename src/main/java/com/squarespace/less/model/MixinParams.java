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
import com.squarespace.less.core.LessUtils;
import com.squarespace.less.exec.ExecEnv;


/**
 * Represents all parameters for a {@link Mixin} definition.
 */
public class MixinParams extends BaseNode {

  /**
   * List of parameters in this group.
   */
  protected List<Parameter> params;

  /**
   * Whether the {@link Mixin} is variadic.
   */
  protected boolean variadic;

  /**
   * Number of required parameters.
   */
  protected int required;

  /**
   * Indicates at least one of the parameters needs to be evaluated.
   */
  protected boolean evaluate;

  /**
   * Appends a parameter to the list, and sets our internal state based on its
   * properties.
   */
  public void add(Parameter param) {
    params = LessUtils.initList(params, 3);
    params.add(param);
    variadic |= param.variadic();
    Node paramValue = param.value();
    if (!param.variadic() && (param.name() == null || (param.name() != null && paramValue == null))) {
      required++;
    }
    if (paramValue != null) {
      evaluate |= paramValue.needsEval();
    }
  }

  /**
   * List of parameters.
   */
  public List<Parameter> params() {
    return LessUtils.safeList(params);
  }

  /**
   * Number of required parameters.
   */
  public int required() {
    return required;
  }

  /**
   * Whether the {@link Mixin} is variadic.
   */
  public boolean variadic() {
    return variadic;
  }

  /**
   * See {@link Node#type()}
   */
  @Override
  public NodeType type() {
    return NodeType.MIXIN_PARAMS;
  }

  /**
   * See {@link Node#needsEval()}
   */
  @Override
  public boolean needsEval() {
    return evaluate;
  }

  /**
   * See {@link Node#eval(ExecEnv)}
   */
  @Override
  public Node eval(ExecEnv env) throws LessException {
    if (!needsEval()) {
      return this;
    }

    MixinParams result = new MixinParams();
    for (Parameter param : params()) {
      result.add((Parameter)param.eval(env));
    }
    return result;
  }

  /**
   * See {@link Node#repr()}
   */
  @Override
  public void repr(Buffer buf) {
    if (params == null) {
      return;
    }
    int size = params.size();
    for (int i = 0; i < size; i++) {
      if (i > 0) {
        buf.append(", ");
      }
      params.get(i).repr(buf);
    }
  }

  /**
   * See {@link Node#modelRepr(Buffer)}
   */
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    posRepr(buf);
    buf.incrIndent().append('\n');
    if (params == null || params.isEmpty()) {
      buf.indent().append("<empty>");
    } else {
      ReprUtils.modelRepr(buf, "\n", true, params);
    }
    buf.decrIndent();
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof MixinParams) ? safeEquals(params, ((MixinParams)obj).params) : false;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

}
