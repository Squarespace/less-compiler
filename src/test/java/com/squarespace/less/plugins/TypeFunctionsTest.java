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

package com.squarespace.less.plugins;

import static com.squarespace.less.ExecuteErrorType.ARG_COUNT;
import static com.squarespace.less.core.Constants.FALSE;
import static com.squarespace.less.core.Constants.TRUE;

import org.testng.annotations.Test;

import com.squarespace.less.LessException;
import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.model.GenericBlock;
import com.squarespace.less.parse.LessSyntax;


public class TypeFunctionsTest extends LessTestBase {

  @Test
  public void testIsColor() throws LessException {
    LessHarness h = harness();

    h.evalEquals("iscolor(#123)", TRUE);
    h.evalEquals("iscolor(rgb(1, 2, 3))", TRUE);
    h.evalEquals("iscolor(@color)", TRUE);
    h.evalEquals("iscolor('foo')", FALSE);
    h.evalEquals("iscolor(@number)", FALSE);
  }

  @Test
  public void testIsEm() throws LessException {
    LessHarness h = harness();

    h.evalEquals("isem(12.3em)", TRUE);
    h.evalEquals("isem(12.3)", FALSE);
    h.evalEquals("isem(1dpi)", FALSE);
    h.evalEquals("isem('foo')", FALSE);
  }

@Test
  public void testIsKeyword() throws LessException {
    LessHarness h = harness();

    h.evalEquals("iskeyword(foo)", TRUE);
    h.evalEquals("iskeyword(true)", TRUE);
    h.evalEquals("iskeyword(false)", TRUE);
    h.evalEquals("iskeyword(blue)", FALSE);
    h.evalEquals("iskeyword('abc')", FALSE);
    h.evalEquals("iskeyword(@color)", FALSE);
  }

  @Test
  public void testIsNumber() throws LessException {
    LessHarness h = harness();

    h.evalEquals("isnumber(3.14)", TRUE);
    h.evalEquals("isnumber(10px)", TRUE);
    h.evalEquals("isnumber('foo')", FALSE);
    h.evalEquals("isnumber(@number)", TRUE);
    h.evalEquals("isnumber(@color)", FALSE);
  }

  @Test
  public void testIsPercentage() throws LessException {
    LessHarness h = harness();

    h.evalFails("ispercentage()", ARG_COUNT);

    h.evalEquals("ispercentage(1%)", TRUE);
    h.evalEquals("ispercentage(-10.79%)", TRUE);

    h.evalEquals("ispercentage(1)", FALSE);
    h.evalEquals("ispercentage(#ff0)", FALSE);
  }

  @Test
  public void testIsPixel() throws LessException {
    LessHarness h = harness();

    h.evalEquals("ispixel(1px)", TRUE);
    h.evalEquals("ispixel(3.14px)", TRUE);
    h.evalEquals("ispixel(3)", FALSE);
    h.evalEquals("ispixel('foo')", FALSE);
  }

  // TODO: testIsRuleset

  @Test
  public void testIsString() throws LessException {
    LessHarness h = harness();

    h.evalEquals("isstring('foo')", TRUE);
    h.evalEquals("isstring(@string)", TRUE);
    h.evalEquals("isstring(12)", FALSE);
    h.evalEquals("isstring(@number)", FALSE);
  }

  @Test
  public void testIsUnit() throws LessException {
    LessHarness h = harness();

    h.evalFails("isunit()", ARG_COUNT);
    h.evalFails("isunit(11px)", ARG_COUNT);
    h.evalFails("isunit(11px)", ARG_COUNT);

    h.evalEquals("isunit(11px, px)", TRUE);
    h.evalEquals("isunit(11px, 'px')", TRUE);
    h.evalEquals("isunit(4rem, rem)", TRUE);
    h.evalEquals("isunit(7.8%, '%')", TRUE);

    h.evalEquals("isunit(2.2%, px)", FALSE);
    h.evalEquals("isunit(33px, rem)", FALSE);
    h.evalEquals("isunit(56px, '%')", FALSE);
    h.evalEquals("isunit(1234, em)", FALSE);
    h.evalEquals("isunit(#ff0, pt)", FALSE);
    h.evalEquals("isunit('mm', mm)", FALSE);

    h.evalEquals("isunit(1px, #f00)", FALSE);
  }

  @Test
  public void testIsUrl() throws LessException {
    LessHarness h = harness();

    h.evalEquals("isurl(url('foo'))", TRUE);
    h.evalEquals("isurl(url(http://foo.com/))", TRUE);
    h.evalEquals("isurl(xurl(foo))", FALSE);
    h.evalEquals("isurl(1)", FALSE);
  }

  private LessHarness harness() {
    GenericBlock defs = defs(
        def("@color", color("#aaa")),
        def("@number", dim(12)),
        def("@string", quoted('"', false, "foo"))
    );
    return new LessHarness(LessSyntax.FUNCTION_CALL, defs);
  }

}
