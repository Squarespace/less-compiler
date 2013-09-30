package com.squarespace.v6.template.less.core;

import static com.squarespace.v6.template.less.core.EncodeUtils.encodeURI;
import static com.squarespace.v6.template.less.core.EncodeUtils.encodeURIComponent;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;


public class EncodeUtilsTest {

  private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";

  private static final String ALPHANUM = LOWERCASE + LOWERCASE.toUpperCase() + "0123456789";
  
  @Test
  public void testEncodeURI() {
    
    // Alphanumerics
    assertEquals(encodeURI(ALPHANUM), ALPHANUM);
    
    // Whitelist characters
    assertEquals(encodeURI("!#$&'()*+,-./:;=?@_~"), "!#$&'()*+,-./:;=?@_~");

    // Single character escapes
    assertEquals(encodeURI("http://foo.com/"), "http://foo.com/");
    assertEquals(encodeURI(" . . ."), "%20.%20.%20.");
    assertEquals(encodeURI("%abc"), "%25abc");
    assertEquals(encodeURI("\u2018.\u2019.\u201a.\u201b"), "%e2%80%98.%e2%80%99.%e2%80%9a.%e2%80%9b");
    
    // Unicode surrogate pair escapes.
    assertEquals(encodeURI("\ud800\udc00"), "%f0%90%80%80");
    assertEquals(encodeURI("\ud900\udc01"), "%f1%90%80%81");
  }
  
  @Test
  public void testEncodeURIComponent() {
    
    // Alphanumerics
    assertEquals(encodeURI(ALPHANUM), ALPHANUM);

    // Whitelist characters
    assertEquals(encodeURIComponent("!'()*-._~"), "!'()*-._~");
    
    // encodeURI whitelist characters
    assertEquals(encodeURIComponent("#$&+,/:;?@"), "%23%24%26%2b%2c%2f%3a%3b%3f%40");
  }
  
}
