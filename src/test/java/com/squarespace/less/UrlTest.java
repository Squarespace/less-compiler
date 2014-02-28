package com.squarespace.less;

import static com.squarespace.less.parse.Parselets.FUNCTION_CALL;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.less.LessException;
import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;


public class UrlTest extends LessTestBase {

  @Test
  public void testEquals() {
    assertEquals(url(quoted('"', false, "http://foo.com")), url(quoted('"', false, "http://foo.com")));

    assertNotEquals(url(quoted('"', false, "http://foo.com")), null);
    assertNotEquals(url(quoted('"', false, "http://foo.com")), anon("http://foo.com"));
    assertNotEquals(url(quoted('"', false, "http://foo.com")), url(quoted('"', false, "http://bar.com")));
  }

  @Test
  public void testModelReprSafety() {
    url(quoted('"', false, "http://squarespace.com/@{page}")).toString();
  }
  
  @Test
  public void testUrl() throws LessException {
    LessHarness h = new LessHarness(FUNCTION_CALL);

    h.parseEquals("url('http://foo.com/@{bar}')", 
        url(quoted('\'', false, anon("http://foo.com/"), var("@bar", true))));

    h.parseEquals("url(http://glonk.com)",
        url(anon("http://glonk.com")));
  }

}
