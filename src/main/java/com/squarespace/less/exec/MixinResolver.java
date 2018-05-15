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
import java.util.List;
import java.util.Map;

import com.squarespace.less.LessException;
import com.squarespace.less.model.Block;
import com.squarespace.less.model.Mixin;
import com.squarespace.less.model.MixinCallArgs;
import com.squarespace.less.model.MixinParams;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Ruleset;
import com.squarespace.less.model.Selector;
import com.squarespace.less.model.Selectors;


/**
 * Searches the node tree, looking for any MIXIN or RULESET nodes which match
 * the given MIXIN_CALL's selector and arguments, if any.
 */
public class MixinResolver {

  protected List<MixinMatch> results;

  protected MixinMatcher matcher;

  protected MixinCallArgs args;

  protected List<String> callPath;

  protected int callPathSize;

  protected int maxIndex;

  // TODO: change how this is used, so we can avoid the default constructor / reset method. - phensley

  public MixinResolver() {
  }

  public void reset(MixinMatcher matcher) {
    this.matcher = matcher;
    this.args = matcher.mixinArgs();
    this.callPath = matcher.mixinCall().path();
    this.callPathSize = callPath.size();
    this.maxIndex = callPath.size() - 1;
    this.results = new ArrayList<>(3);
  }

  public List<MixinMatch> matches() {
    return results;
  }

  public boolean match(Block block) throws LessException {
    return match(0, block);
  }

  protected boolean match(int index, Block block) throws LessException {
    if (index >= callPathSize) {
      return false;
    }

    Map<String, List<Node>> mixins = block.mixins();
    if (mixins == null) {
      return false;
    }

    // We prune the mixin search space at every level, leveraging
    // the mixin path prefix index to avoid scanning non-matching blocks.
    // We also iterate directly over only mixin nodes.

    String key = this.callPath.get(index);
    List<Node> rules = mixins.get(key);
    if (rules == null || rules.isEmpty()) {
      return false;
    }

    boolean matched = false;
    int size = rules.size();
    for (int i = 0; i < size; i++) {
      Node node = rules.get(i);
      if (node instanceof Ruleset) {
        matched |= matchRuleset(index, (Ruleset)node);

      } else if (node instanceof Mixin) {
        matched |= matchMixin(index, (Mixin)node);
      }
    }

    return matched;
  }

  protected boolean matchRuleset(int index, Ruleset ruleset) throws LessException {
    if (!ruleset.hasMixinPath()) {
      return false;
    }
    Ruleset original = (Ruleset)ruleset.original();
    if (original.evaluating()) {
      return false;
    }

    Selectors selectors = ruleset.selectors();
    for (Selector selector : selectors.selectors()) {
      List<String> path = selector.mixinPath();
      int remaining = matchPath(index, path);
      if (remaining < 0) {
        continue;
      }

      // Full match.. add this to the results.
      if (remaining == 0) {
        if (args == null || args.isEmpty()) {
          results.add(new MixinMatch(ruleset, selector, null));
          return true;
        }

      } else {
        // Partial match.. continue matching the children of this ruleset.
        if (match(index + path.size(), ruleset.block())) {
          return true;
        }
      }
    }

    return false;
  }

  private boolean matchMixin(int index, Mixin mixin) throws LessException {
    boolean matched = callPath.get(index).equals(mixin.name());
    if (!matched) {
      return false;
    }
    if (matched && index < maxIndex) {
      // We haven't matched entire path, so drill deeper.
      return match(index + 1, mixin.block());
    }

    // Evaluate the mixin's params in order to perform pattern-matching.
    MixinParams params = mixin.params();
    ExecEnv env = matcher.callEnv().copy();

    // Append the mixin definitions closure frames, if any.
    ExecEnv defEnv = mixin.closure();
    if (defEnv != null) {
      env.append(defEnv.frames().copy());
    }

    params = (MixinParams) params.eval(env);
    boolean matches = matcher.patternMatch(params);

    if (matches) {
      results.add(new MixinMatch(mixin, null, params));
    }
    return matches;
  }

  protected int matchPath(int index, List<String> other) {
    if (other == null) {
      return -1;
    }
    int otherSize = other.size();
    int currSize = callPathSize - index;
    if (otherSize == 0 || currSize < otherSize) {
      return -1;
    }

    int j = 0;
    while (j < otherSize) {
      if (!callPath.get(index).equals(other.get(j))) {
        return -1;
      }
      index++;
      j++;
    }
    return callPathSize - index;
  }

}
