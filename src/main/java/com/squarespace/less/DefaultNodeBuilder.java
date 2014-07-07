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

package com.squarespace.less;

import com.squarespace.less.model.Block;
import com.squarespace.less.model.Comment;
import com.squarespace.less.model.Definition;
import com.squarespace.less.model.Guard;
import com.squarespace.less.model.Mixin;
import com.squarespace.less.model.MixinCall;
import com.squarespace.less.model.MixinCallArgs;
import com.squarespace.less.model.MixinParams;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Operation;
import com.squarespace.less.model.Operator;
import com.squarespace.less.model.Rule;
import com.squarespace.less.model.Ruleset;
import com.squarespace.less.model.Selector;
import com.squarespace.less.model.Selectors;
import com.squarespace.less.model.Variable;


public class DefaultNodeBuilder implements NodeBuilder {

  @Override
  public Comment buildComment(String body, boolean block) {
    return buildComment(body, block, false);
  }

  @Override
  public Comment buildComment(String body, boolean block, boolean newline) {
    return new Comment(body, block, newline);
  }

  @Override
  public Definition buildDefinition(String name, Node value) {
    return new Definition(name, value);
  }

  @Override
  public Definition buildDefinition(Variable variable, Node value) {
    return new Definition(variable, value);
  }

  @Override
  public Mixin buildMixin(String name, MixinParams params, Guard guard) {
    return new Mixin(name, params, guard);
  }

  @Override
  public Mixin buildMixin(String name, MixinParams params, Guard guard, Block block) {
    return new Mixin(name, params, guard, block);
  }

  @Override
  public MixinCall buildMixinCall(Selector selector, MixinCallArgs args, boolean important) {
    return new MixinCall(selector, args, important);
  }

  @Override
  public Operation buildOperation(Operator operator, Node operand0, Node operand1) {
    return new Operation(operator, operand0, operand1);
  }

  @Override
  public Rule buildRule(Node name, Node value) {
    return new Rule(name, value);
  }

  @Override
  public Rule buildRule(Node name, Node value, boolean important) {
    return new Rule(name, value, important);
  }

  @Override
  public Ruleset buildRuleset(Selectors selectors, Block block) {
    return new Ruleset(selectors, block);
  }

  @Override
  public Selector buildSelector() {
    return new Selector();
  }

  @Override
  public Variable buildVariable(String name) {
    return new Variable(name);
  }

  @Override
  public Variable buildVariable(String name, boolean curly) {
    return new Variable(name, curly);
  }

}
