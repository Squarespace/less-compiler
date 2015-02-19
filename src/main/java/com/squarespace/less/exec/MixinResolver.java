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

import com.squarespace.less.LessException;
import com.squarespace.less.core.FlexList;
import com.squarespace.less.model.Block;
import com.squarespace.less.model.GenericBlock;
import com.squarespace.less.model.Mixin;
import com.squarespace.less.model.MixinCall;
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

  protected String callPath;

  protected int callPathLength;

  protected int maxIndex;

  // TODO: change how this is used, so we can avoid the default constructor / reset method.

  public MixinResolver() {
  }

  public void reset(MixinMatcher matcher) {
    this.matcher = matcher;
    this.args = matcher.mixinArgs();
    this.callPath = matcher.mixinCall().path();
    this.callPathLength = callPath.length();
    this.maxIndex = callPathLength - 1;
    this.results = new ArrayList<>(3);
  }

  public List<MixinMatch> matches() {
    return results;
  }

  /**
   * Starts the matching process at the beginning of the {@link MixinCall}'s path.
   */
  public boolean match(Block block) throws LessException {
    return match(0, block);
  }

  /**
   * Scan the block and match each found {@link Mixin} or {@link Ruleset}'s path
   * against this {@link MixinCall}'s path starting at position {@code index}.
   */
  protected boolean match(int index, Block block) throws LessException {
    if (index >= callPathLength) {
      return false;
    }

    FlexList<Node> rules = block.rules();
    if (rules.isEmpty()) {
      return false;
    }

    // TODO: future mixin resolution optimization to cache ruleset/mixins in
    // a separate field on the block to reduce size of these inner loops.
    // this should improve execution times for large stylesheets with many
    // imports.

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

  /**
   * Attempt to match the mixin call's path against a {@link Ruleset}
   */
  protected boolean matchRuleset(int index, Ruleset ruleset) throws LessException {
    if (!ruleset.hasMixinPath()) {
      return false;
    }

    Ruleset original = (Ruleset)ruleset.original();

    /// Ignore recursive entries into ruleset mixins.
    if (original.evaluating()) {
      return false;
    }

    Selectors selectors = ruleset.selectors();
    for (Selector selector : selectors.selectors()) {
      String path = selector.mixinPath();
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
        if (match(index + path.length(), ruleset.block())) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Attempt to match the mixin call's path against a {@link Mixin}.
   */
  private boolean matchMixin(int index, Mixin mixin) throws LessException {
    int remaining = matchPath(index, mixin.name());

    // No match, bail out
    if (remaining < 0) {
      return false;
    }

    if (remaining > 0) {
      // We haven't matched entire path, so drill deeper.
      return match(index + mixin.name().length(), mixin.block());
    }

    // Full match, check if the arguments pattern match the parameters.
    MixinParams params = mixin.params();
    ExecEnv env = matcher.callEnv().copy();

    // Append the mixin definitions closure frames, if any.
    ExecEnv defEnv = mixin.closure();
    if (defEnv != null) {
      env.append(defEnv.frames().copy());
    }

    // Make sure we can resolve params against other params.
    GenericBlock block = params.toBlock(env);
    if (block != null) {
      env.push(block);
    }

    params = (MixinParams) params.eval(env);
    boolean matches = matcher.patternMatch(params);

    if (matches) {
      results.add(new MixinMatch(mixin, null, params));
    }
    return matches;
  }

  /**
   * Partially match the call path against the given string.
   */
  protected int matchPath(int index, String other) {
    if (other == null) {
      return -1;
    }

    int otherLength = other.length();
    int currSize = callPathLength - index;
    if (otherLength == 0 || currSize < otherLength) {
      return -1;
    }

    int j = 0;
    while (j < otherLength) {
      if (callPath.charAt(index) != other.charAt(j)) {
        return -1;
      }
      index++;
      j++;
    }
    return callPathLength - index;
  }

}
