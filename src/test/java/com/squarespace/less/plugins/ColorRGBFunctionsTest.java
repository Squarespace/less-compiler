package com.squarespace.less.plugins;

import static com.squarespace.less.ExecuteErrorType.INVALID_ARG;

import org.testng.annotations.Test;

import com.squarespace.less.LessException;
import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.model.GenericBlock;
import com.squarespace.less.parse.Parselets;


public class ColorRGBFunctionsTest extends LessTestBase {

  @Test
  public void testFunctions() throws LessException {
    GenericBlock defs = defs(
        def("@one", dim(1)),
        def("@three", dim(3))
    );

    LessHarness h = new LessHarness(Parselets.FUNCTION_CALL, defs);

    // Color averaging
    h.evalEquals("average(#888, #444)", color("#666"));
    h.evalEquals("average(#000, #888)", color("#444"));

    // Color hex parsing
    h.evalEquals("color('#fff')", color("#fff"));
    h.evalEquals("color('#010203')", color("#010203"));

    // ALPHA
    h.evalEquals("alpha(rgba(10, 10, 10, .5))", dim(0.5));
    h.evalEquals("alpha(#aabbcc)", dim(1.0));

    // ARGB
    h.evalEquals("argb(rgba(1, 2, 3, 50%))", anon("#80010203"));

    // BLUE
    h.evalEquals("blue(rgb(50, 50, 37))", dim(37));

    // COLOR
    h.evalEquals("color('#aabbcc')", rgb(0xaa, 0xbb, 0xcc));

    // GREEN
    h.evalEquals("green(rgb(50, 37, 50))", dim(37));

    // RED
    h.evalEquals("red(rgb(37, 50, 50))", dim(37));

    // RGB
    h.evalEquals("rgb(@one, 2, @three)", color("#010203"));
    h.evalEquals("rgb(1000, 1000, 1000)", color("#fff"));
    h.evalFails("rgb('foo', 2, 3)", INVALID_ARG);

    // RGBA
    h.evalEquals("rgba(1, 2, 3, .5)", rgb(1, 2, 3, 0.5));
    h.evalEquals("rgba(@one, @one, @one, @one)", rgb(1, 1, 1, 1));
    h.evalFails("rgba(1, 1, 1, 'foo')", INVALID_ARG);
  }

}
