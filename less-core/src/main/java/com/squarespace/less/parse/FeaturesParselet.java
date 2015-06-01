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

import static com.squarespace.less.parse.Parselets.FEATURE;
import static com.squarespace.less.parse.Parselets.VARIABLE;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Chars;
import com.squarespace.less.model.Features;
import com.squarespace.less.model.Node;


/**
 * Parse a comma-delimited list of features which are part of a MEDIA or IMPORT node.
 */
public class FeaturesParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    Node node = parseOne(stm);
    if (node == null) {
      return null;
    }

    Features features = new Features();
    while (node != null) {
      features.add(node);
      stm.skipWs();
      if (!stm.seekIf(Chars.COMMA)) {
        break;
      }
      node = parseOne(stm);
    }
    return features;
  }

  private Node parseOne(LessStream stm) throws LessException {
    Node node = stm.parse(FEATURE);
    if (node != null) {
      return node;
    }
    return stm.parse(VARIABLE);
  }

}
