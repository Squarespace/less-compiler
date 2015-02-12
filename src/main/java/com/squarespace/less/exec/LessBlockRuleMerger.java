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

package com.squarespace.less.exec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.squarespace.less.LessContext;
import com.squarespace.less.LessException;
import com.squarespace.less.model.Expression;
import com.squarespace.less.model.ExpressionList;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.PropertyMergeMode;
import com.squarespace.less.model.PropertyMergeable;
import com.squarespace.less.model.Rule;


/**
 * Special class to accumulate and selectively merge rules
 * based on their properties having merge modes defined.
 */
public class LessBlockRuleMerger {

  /**
   * Context used for rendering properties.
   */
  private final LessContext context;

  /**
   * Mapping of rendered property name to rule.
   */
  private final Map<String, Rule> ruleMap = new HashMap<>();

  /**
   * List of rules in the order added.
   */
  private final List<Rule> rules = new ArrayList<>(4);

  /**
   * Constructs an object to merge rules based on properties and
   * merge modes.
   */
  public LessBlockRuleMerger(LessContext context) {
    this.context = context;
  }

  /**
   * Return the list of processed rules.
   */
  public List<Rule> rules() {
    return rules;
  }

  /**
   * Add a rule to the set, selectively merging it with a pre-existing
   * rule with the same property name and a valid merge mode.
   */
  public void add(Rule rule) throws LessException {
    Node property = rule.property();
    PropertyMergeable mergeable = (PropertyMergeable)rule.property();
    if (mergeable.mergeMode() == PropertyMergeMode.NONE) {
      rules.add(rule);
      return;
    }

    // The rendered property is used as the key to merge rules.
    String name = context.render(property);
    Rule mapped = ruleMap.get(name);
    if (mapped == null) {

      // First rule we've seen with this property.
      ruleMap.put(name, rule);
      rules.add(rule);
      return;
    }

    // Merge the important flag.
    boolean important = mapped.important() || rule.important();
    mapped.markImportant(important);

    // Merge the source value to the destination using the source's
    // merge mode.
    Node dst = mapped.value();
    Node src = rule.value();
    if (mergeable.mergeMode() == PropertyMergeMode.COMMA) {
      mapped.value(mergeComma(dst, src));

    } else if (mergeable.mergeMode() == PropertyMergeMode.SPACE) {
      mapped.value(mergeSpace(dst, src));
    }
  }

  /**
   * Combine values into a comma-separated list.
   */
  private Node mergeComma(Node dst, Node src) {
    if (dst instanceof ExpressionList) {
      ExpressionList list = (ExpressionList)dst;
      list.add(src);
      return dst;
    }

    ExpressionList list = new ExpressionList();
    list.add(dst);
    list.add(src);
    return list;
  }

  /**
   * Combine values into a space-separated list.
   */
  private Node mergeSpace(Node dst, Node src) {
    if (dst instanceof Expression) {
      Expression expn = (Expression)dst;
      expn.add(src);
      return dst;
    }

    Expression expn = new Expression();
    expn.add(dst);
    expn.add(src);
    return expn;
  }

}
