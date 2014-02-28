package com.squarespace.v6.template.less.plugins;

import static com.squarespace.v6.template.less.core.Constants.FALSE;
import static com.squarespace.v6.template.less.core.Constants.TRUE;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.LessHarness;
import com.squarespace.v6.template.less.core.LessTestBase;
import com.squarespace.v6.template.less.model.GenericBlock;
import com.squarespace.v6.template.less.parse.Parselets;


public class TypeFunctionsTest extends LessTestBase {

  @Test
  public void testFunctions() throws LessException {
    GenericBlock defs = defs(
        def("@color", color("#aaa")),
        def("@number", dim(12)),
        def("@string", quoted('"', false, "foo"))
    );

    LessHarness h = new LessHarness(Parselets.FUNCTION_CALL, defs);
    
    // Colors
    h.evalEquals("iscolor(#123)", TRUE);
    h.evalEquals("iscolor(rgb(1, 2, 3))", TRUE);
    h.evalEquals("iscolor(@color)", TRUE);
    h.evalEquals("iscolor('foo')", FALSE);
    h.evalEquals("iscolor(@number)", FALSE);

    // Keywords
    h.evalEquals("iskeyword(foo)", TRUE);
    h.evalEquals("iskeyword(true)", TRUE);
    h.evalEquals("iskeyword(false)", TRUE);
    h.evalEquals("iskeyword(blue)", FALSE);
    h.evalEquals("iskeyword('abc')", FALSE);
    h.evalEquals("iskeyword(@color)", FALSE);
    
    // Numbers
    h.evalEquals("isnumber(3.14)", TRUE);
    h.evalEquals("isnumber(10px)", TRUE);
    h.evalEquals("isnumber('foo')", FALSE);
    h.evalEquals("isnumber(@number)", TRUE);
    h.evalEquals("isnumber(@color)", FALSE);
    
    // Ems
    h.evalEquals("isem(12.3em)", TRUE);
    h.evalEquals("isem(12.3)", FALSE);
    h.evalEquals("isem(1dpi)", FALSE);
    h.evalEquals("isem('foo')", FALSE);
    
    // Pixels
    h.evalEquals("ispixel(1px)", TRUE);
    h.evalEquals("ispixel(3.14px)", TRUE);
    h.evalEquals("ispixel(3)", FALSE);
    h.evalEquals("ispixel('foo')", FALSE);
    
    // Strings
    h.evalEquals("isstring('foo')", TRUE);
    h.evalEquals("isstring(@string)", TRUE);
    h.evalEquals("isstring(12)", FALSE);
    h.evalEquals("isstring(@number)", FALSE);
    
    // Urls
    h.evalEquals("isurl(url('foo'))", TRUE);
    h.evalEquals("isurl(url(http://foo.com/))", TRUE);
    h.evalEquals("isurl(xurl(foo))", FALSE);
    h.evalEquals("isurl(1)", FALSE);
  }

}
