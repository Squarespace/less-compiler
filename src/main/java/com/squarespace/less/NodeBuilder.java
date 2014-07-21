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
import com.squarespace.less.model.BlockDirective;
import com.squarespace.less.model.Comment;
import com.squarespace.less.model.Definition;
import com.squarespace.less.model.Directive;
import com.squarespace.less.model.Features;
import com.squarespace.less.model.Guard;
import com.squarespace.less.model.Media;
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
import com.squarespace.less.model.Stylesheet;
import com.squarespace.less.model.Variable;


/**
 * Allows clients to substitute custom implementations of nodes.
 */
public interface NodeBuilder {

  BlockDirective buildBlockDirective(String name, Block block);

  Comment buildComment(String body, boolean block);

  Comment buildComment(String body, boolean block, boolean newline);

  Definition buildDefinition(Variable variable, Node value);

  Definition buildDefinition(String name, Node value);

  Directive buildDirective(String name, Node value);

  Media buildMedia(Features features, Block block);

  Mixin buildMixin(String name, MixinParams params, Guard guard);

  Mixin buildMixin(String name, MixinParams params, Guard guard, Block block);

  MixinCall buildMixinCall(Selector selector, MixinCallArgs args, boolean important);

  Operation buildOperation(Operator operator, Node operand0, Node operand1);

  Rule buildRule(Node property, Node value);

  Rule buildRule(Node property, Node value, boolean important);

  Ruleset buildRuleset(Selectors group, Block block);

  Selector buildSelector();

  Stylesheet buildStylesheet(Block block);

  Variable buildVariable(String name);

  Variable buildVariable(String name, boolean curly);

}
