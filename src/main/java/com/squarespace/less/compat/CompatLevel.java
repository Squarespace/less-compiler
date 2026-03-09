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

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;


/**
 * Compatibility level for one compile: how far along the fix ladder the
 * compiler sits. Level 0 keeps every legacy behavior active (the
 * released surface). At level N every patch whose threshold is at most
 * N is fixed. The highest level is the fully-fixed compiler.
 *
 * <p>Per-site overrides force legacy behaviors on regardless of level,
 * for the rare site the ladder cannot express.
 */
public final class CompatLevel {

  /**
   * Fully fixed compiler: every fix applied, no legacy behavior.
   */
  public static CompatLevel fixed() {
    return new CompatLevel(Patch.maxThreshold(), EnumSet.noneOf(Patch.class));
  }

  /**
   * Default level: every legacy behavior active, preserving released
   * behavior. Safe default for unmigrated sites.
   */
  public static CompatLevel defaultLevel() {
    return new CompatLevel(0, EnumSet.noneOf(Patch.class));
  }

  /**
   * A specific ladder position.
   */
  public static CompatLevel at(int level) {
    if (level < 0) {
      throw new IllegalArgumentException("compat level must be >= 0, got " + level);
    }
    return new CompatLevel(level, EnumSet.noneOf(Patch.class));
  }

  private final int level;

  private final Set<Patch> overrides;

  private CompatLevel(int level, Set<Patch> overrides) {
    this.level = level;
    this.overrides = overrides;
  }

  /**
   * True when the legacy behavior is active at this level: the patch's
   * fix is not yet applied (level below its threshold), or the patch is
   * forced on via an override.
   */
  public boolean enabled(Patch patch) {
    return overrides.contains(patch) || level < patch.threshold();
  }

  /**
   * Level value. 0 keeps every legacy behavior active (released), the
   * maximum is the fully-fixed compiler.
   */
  public int level() {
    return level;
  }

  /**
   * Patches whose legacy behavior is forced on regardless of level.
   */
  public Set<Patch> overrides() {
    return Collections.unmodifiableSet(overrides);
  }

  /**
   * Copy with the patch's legacy behavior forced on.
   */
  public CompatLevel withPatch(Patch patch) {
    Set<Patch> copy = EnumSet.copyOf(overrides);
    copy.add(patch);
    return new CompatLevel(level, copy);
  }

  /**
   * Copy at a different ladder position, preserving the override set.
   * Changing the level must not silently discard per-site legacy
   * overrides, regardless of setter order on the options.
   */
  public CompatLevel withLevel(int level) {
    if (level < 0) {
      throw new IllegalArgumentException("compat level must be >= 0, got " + level);
    }
    return new CompatLevel(level, overrides);
  }

  @Override
  public String toString() {
    return "CompatLevel(level=" + level + ", overrides=" + overrides + ")";
  }
}
