/**
 * Copyright, 2015, Squarespace, Inc.
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

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.squarespace.less.LessContext;
import com.squarespace.less.LessException;
import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.model.Node;


public class SelectorParserTest extends LessTestBase {

  @Test
  public void testParseEquivalence() throws LessException {

    // Compare the Parselet output to the SelectorParser, for the
    // subset of the syntax both cover.

    assertEquiv(".foo > .bar | baz[foo~='bar']");
    assertEquiv("#ns > .child1 > &:child2");
    assertEquiv("* > a:hover");
    assertEquiv(".\\34 04 strong");
    assertEquiv("\\62\\6c\\6f \\63 \\6B \\0071 \\000075o\\74 e");
  }

  /**
   * Ensures the {@link Parselet} output is equivalent to that produced by
   * the {@link SelectorParser}, for the subset of the syntax both parsers cover.
   */
  private void assertEquiv(String raw) throws LessException {
    LessHarness h = new LessHarness(Parselets.SELECTOR);
    Node expected = h.parse(raw);

    SelectorParser parser = new SelectorParser(new LessContext());
    assertEquals(expected, parser.parse(raw));
  }

}
