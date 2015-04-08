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
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.model.Block;
import com.squarespace.less.model.Ruleset;


public class LessParserTest extends LessTestBase {

  @Test
  public void testParser() throws LessException {
    LessContext ctx = new LessContext();
    LessParser parser = new LessParser(ctx);
    parser.parse(".header { color: blue }", null);

    Ruleset rs = ruleset(selector(element(".header")));
    rs.add(rule(prop("color"), color("blue")));
    Block expected = block(rs);
    assertEquals(parser.stylesheet().block(), expected);
  }

}
