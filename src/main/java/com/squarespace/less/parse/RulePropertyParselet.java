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

package com.squarespace.less.parse;

import static com.squarespace.less.core.Chars.COLON;

import java.util.ArrayList;
import java.util.List;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Chars;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Property;
import com.squarespace.less.model.PropertyMergeMode;
import com.squarespace.less.model.RuleProperty;
import com.squarespace.less.model.Variable;


/**
 * Parses the property for a rule, which may be a single {@link Property} or
 * a list of alternating {@link Property} and curly {@link Variable} references.
 */
public class RulePropertyParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    Mark mark = stm.mark();
    PropertyMergeMode merge = null;
    Node node = stm.parse(Parselets.VARIABLE);
    if (node != null) {
      merge = end(stm);
      // Definitions should not have a merge mode.
      if (merge == PropertyMergeMode.NONE) {
        return node;
      }
      stm.restore(mark);
      return null;
    }

    // The vast majority of rules will have a static property, so try to
    // parse a single segment first.
    Node first = stm.parse(Parselets.PROPERTY);
    if (first == null) {
      stm.restore(mark);
      return null;
    }

    // Check if this is the only property segment.
    merge = end(stm);
    if (merge != null) {
      Property prop = (Property) first;
      prop.mergeMode(merge);
      return prop;
    }

    // We failed to find the end, so try to parse one or more Property and curly Variable nodes.
    Node second = stm.parse(Parselets.RULE_KEY);
    if (second == null) {
      stm.restore(mark);
      return null;
    }

    // Okay, we have 2 segments so keep parsing.
    List<Node> segments = new ArrayList<>();
    segments.add(first);
    segments.add(second);
    while ((node = stm.parse(Parselets.RULE_KEY)) != null) {
      segments.add(node);
    }

    // We found N segments, so look for the end or bail out.
    merge = end(stm);
    if (merge != null) {
      return new RuleProperty(segments, merge);
    }
    stm.restore(mark);
    return null;
  }

  /**
   * Look for the sequence of characters that end a property.
   */
  private PropertyMergeMode end(LessStream stm) {
    PropertyMergeMode merge = PropertyMergeMode.NONE;
    if (stm.peek() == Chars.PLUS_SIGN) {
      merge = PropertyMergeMode.COMMA;
      stm.seek1();
      if (stm.peek() == Chars.UNDERSCORE) {
        merge = PropertyMergeMode.SPACE;
        stm.seek1();
      }
    }

    stm.skipWs();
    if (stm.seekIf(COLON)) {
      return merge;
    }
    return null;
  }

}
