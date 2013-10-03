package com.squarespace.v6.template.less;

import static com.squarespace.v6.template.less.parse.Parselets.FUNCTION_CALL;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.core.LessHarness;
import com.squarespace.v6.template.less.core.LessTestBase;


public class UrlTest extends LessTestBase {

  @Test
  public void testEquals() {
    assertEquals(url(quoted('"', "http://foo.com")), url(quoted('"', "http://foo.com")));

    assertNotEquals(url(quoted('"', "http://foo.com")), null);
    assertNotEquals(url(quoted('"', "http://foo.com")), anon("http://foo.com"));
    assertNotEquals(url(quoted('"', "http://foo.com")), url(quoted('"', "http://bar.com")));
  }

  @Test
  public void testModelReprSafety() {
    url(quoted('"', "http://squarespace.com/@{page}")).toString();
  }
  
  @Test
  public void testUrl() throws LessException {
    LessHarness h = new LessHarness(FUNCTION_CALL);

    h.parseEquals("url('http://foo.com/@{bar}')", 
        url(quoted('\'', anon("http://foo.com/"), var("@bar", true))));

    h.parseEquals("url(http://glonk.com)",
        url(anon("http://glonk.com")));
  }

}
