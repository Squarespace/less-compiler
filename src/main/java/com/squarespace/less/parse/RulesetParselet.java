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

import static com.squarespace.less.parse.Parselets.BLOCK;
import static com.squarespace.less.parse.Parselets.SELECTORS;

import com.squarespace.less.LessException;
import com.squarespace.less.model.Block;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Ruleset;
import com.squarespace.less.model.Selectors;


public class RulesetParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    int[] mark = stm.mark();
    Selectors group = (Selectors) stm.parse(SELECTORS);
    if (group == null) {
      stm.popMark();
      return null;
    }

    Node block = stm.parse(BLOCK);
    if (block == null) {
      stm.restore(mark);
      stm.popMark();
      return null;
    }

    Ruleset ruleset = stm.context().nodeBuilder().buildRuleset(group, (Block)block);
    ruleset.fileName(stm.fileName());
    stm.popMark();
    return ruleset;
  }

}
