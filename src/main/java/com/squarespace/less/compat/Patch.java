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

package com.squarespace.less.compat;

/**
 * Legacy behaviors gated by a {@link CompatLevel}. A patch's legacy
 * behavior is active when the compile level is below its threshold (its
 * fix not yet applied), or when a per-site override forces it on.
 *
 * <p>A fix that changes output ships with a legacy path gated by a new
 * patch at a new higher threshold, so higher levels apply more fixes
 * without changing the released surface at level 0. The default level
 * is 0, preserving released behavior until sites migrate up.
 *
 * <p>BUG1..BUG4 are the former safe-mode tolerances. Retire a patch
 * when no site needs it: remove the legacy path, its tests, and the
 * entry together.
 */
public enum Patch {

  /**
   * Extraneous '+' at block scope is tolerated.
   */
  BUG1(1),

  /**
   * Block-less '@media' is dropped. The statements that follow attach to
   * the enclosing block.
   */
  BUG2(1),

  /**
   * A variable followed by empty parens, e.g. '@dk-gray();', is accepted.
   */
  BUG3(1),

  /**
   * Invalid addition like 'random(90) + px' is tolerated. Parse
   * backtracking is skipped.
   */
  BUG4(1),

  /**
   * Selector-complexity overflow in a nested rule is swallowed: the
   * current selector is dropped instead of failing the compile.
   */
  SELECTOR_COMPLEXITY_OVERFLOW(2),

  /**
   * '@import url("x.less")' is emitted literally instead of being
   * resolved and inlined.
   */
  IMPORT_URL_INLINE(2),

  /**
   * NaN and Infinity values render as '0' instead of visible text.
   */
  NONFINITE_AS_ZERO(2),

  /**
   * 'mod(x, 0)' silently returns NaN instead of obeying the division
   * contract (strict fails, lenient warns).
   */
  MOD_ZERO_STRICT(2),

  /**
   * convert() to an incompatible unit silently emits 0 instead of
   * failing the compile.
   */
  CONVERT_INCOMPATIBLE_UNITS(2),

  /**
   * replace() treats '$' and '\\' in the replacement as regex group
   * references instead of inserting them literally.
   */
  REPLACE_REGEX_GROUPS(2),

  /**
   * A named argument that targets the variadic parameter is rejected
   * instead of binding to it.
   */
  VARIADIC_NAMED_ARG(2);

  /**
   * Lowest level where this legacy behavior is active. Frozen once a
   * release ships: raising it silently changes live sites at intermediate
   * levels.
   */
  private final int threshold;

  Patch(int threshold) {
    this.threshold = threshold;
  }

  public int threshold() {
    return threshold;
  }

  /**
   * Highest threshold. The default level sits here, preserving released
   * behavior.
   */
  public static int maxThreshold() {
    int max = 0;
    for (Patch patch : values()) {
      max = Math.max(max, patch.threshold);
    }
    return max;
  }
}
