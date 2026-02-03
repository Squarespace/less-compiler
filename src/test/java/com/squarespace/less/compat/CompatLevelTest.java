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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import org.testng.annotations.Test;


public class CompatLevelTest {

  @Test
  public void testFixedLevel() {
    // The fully-fixed compiler is the highest level.
    CompatLevel compat = CompatLevel.fixed();
    assertEquals(compat.level(), Patch.maxThreshold());
    for (Patch patch : Patch.values()) {
      assertFalse(compat.enabled(patch));
    }
  }

  @Test
  public void testDefaultLevel() {
    // The default keeps every legacy behavior active: the released
    // surface, at level 0.
    CompatLevel compat = CompatLevel.defaultLevel();
    assertEquals(compat.level(), 0);
    for (Patch patch : Patch.values()) {
      assertTrue(compat.enabled(patch));
    }
  }

  @Test
  public void testLevelThresholds() {
    // The legacy-active set at level L is { patch : threshold(patch) > L }:
    // a fix applies at its threshold level and above.
    for (int level = 0; level <= Patch.maxThreshold(); level++) {
      CompatLevel compat = CompatLevel.at(level);
      for (Patch patch : Patch.values()) {
        assertEquals(compat.enabled(patch), patch.threshold() > level, patch.name() + " at level " + level);
      }
    }
  }

  @Test
  public void testMonotonic() {
    // The ladder is monotone: raising a site's level can only fix
    // behaviors, never re-enable a legacy one.
    for (int level = 0; level < Patch.maxThreshold(); level++) {
      for (Patch patch : Patch.values()) {
        if (CompatLevel.at(level + 1).enabled(patch)) {
          assertTrue(CompatLevel.at(level).enabled(patch));
        }
      }
    }
  }

  @Test
  public void testOverrides() {
    CompatLevel base = CompatLevel.fixed();
    CompatLevel patched = base.withPatch(Patch.BUG2);
    for (Patch patch : Patch.values()) {
      assertEquals(patched.enabled(patch), patch == Patch.BUG2, patch.name());
    }
    // The original level is untouched.
    for (Patch patch : Patch.values()) {
      assertFalse(base.enabled(patch));
    }
    // An override on an already-active patch is a no-op for that patch.
    CompatLevel defaulted = CompatLevel.defaultLevel().withPatch(Patch.BUG2);
    assertTrue(defaulted.enabled(Patch.BUG2));
    assertEquals(defaulted.level(), 0);
  }

  @Test
  public void testInvalidLevel() {
    try {
      CompatLevel.at(-1);
      fail("expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // expected
    }
  }
}
