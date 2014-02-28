package com.squarespace.v6.template.less.core;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.core.CharPattern;
import com.squarespace.v6.template.less.core.Chars;
import com.squarespace.v6.template.less.parse.Mark;
import com.squarespace.v6.template.less.parse.Patterns;
import com.squarespace.v6.template.less.parse.Stream;


public class StreamTest {

  @Test
  public void testFind() {
    CharPattern matcher = new CharPattern('\n');
    Stream stm = new Stream("// abc\n");
    assertTrue(stm.seek(matcher));
    assertEquals(stm.peek(), Chars.EOF);

    stm = new Stream("// abc");
    assertFalse(stm.seek(Patterns.LINE_COMMENT_END));
    assertEquals(stm.peek(), Chars.EOF);

    matcher = new CharPattern('*', '/');
    stm = new Stream("  /* foo * bar */ baz");
    assertTrue(stm.seek(matcher));
    
    matcher = new CharPattern('a', 'a', 'b');
    stm = new Stream("aaaaaaaaaaaaaabxyz");
    assertTrue(stm.seek(matcher));
    assertEquals(stm.peek(), 'x');
  }
  
  @Test
  public void testFurthest() {
    Stream stm = new Stream("abcdefghijkl");
    Mark mark = stm.mark();
    stm.seek(8);
    stm.restore(mark);
    mark = stm.mark();
    stm.seek(4);
    stm.restore(mark);
    assertEquals(stm.furthest(), "ijkl");
  }
  
  @Test
  public void testPeek() {
    Stream stm = new Stream("abcde");
    assertEquals(stm.peek(), 'a');
    // Backwards seek not allowed. Leaves pointer unchanged.
    stm.seek(-5);
    assertEquals(stm.peek(), 'a');
    stm.seek(2);
    assertEquals(stm.peek(), 'c');
    stm.seek(5);
    assertEquals(stm.peek(), Chars.EOF);
  }
  
  @Test
  public void testPeekAhead() {
    Stream stm = new Stream("abcde");
    assertEquals(stm.peek(0), 'a');
    assertEquals(stm.peek(1), 'b');
    assertEquals(stm.peek(4), 'e');
    assertEquals(stm.peek(10), Chars.EOF);
  }
  
  @Test
  public void testMarkRestore() {
    Mark pos = new Mark();
    Stream stm = new Stream("abcde");
    stm.mark(pos);
    stm.seek(2);
    assertEquals(stm.peek(), 'c');
    stm.restore(pos);
    assertEquals(stm.peek(), 'a');

    stm.seek1();
    assertEquals(stm.peek(), 'b');
    stm.mark(pos);
    stm.seek(10);
    assertEquals(stm.peek(), Chars.EOF);
    stm.restore(pos);
    assertEquals(stm.peek(), 'b');
  }
  
  @Test
  public void testSeek() {
    Stream stm = new Stream("abc\ndef\nghi");
    assertEquals(stm.getLineOffset(), 0);
    stm.seek(5);
    assertEquals(stm.peek(), 'e');
    assertEquals(stm.getLineOffset(), 1);
    assertEquals(stm.getCharOffset(), 1);
    stm.seek(5);
    assertEquals(stm.peek(), 'i');
    assertEquals(stm.getLineOffset(), 2);
    assertEquals(stm.getCharOffset(), 2);
    stm.seek1();
    assertEquals(stm.peek(), Chars.EOF);
    assertEquals(stm.getLineOffset(), 2);
    assertEquals(stm.getCharOffset(), 3);
  }

  @Test
  public void testSeek1() {
    Stream stm = new Stream("abc");
    stm.seek1();
    assertEquals(stm.peek(), 'b');
    stm.seek1();
    stm.seek1();
    assertEquals(stm.peek(), Chars.EOF);
    stm.seek1();
    assertEquals(stm.peek(), Chars.EOF);
  }
  
  @Test
  public void testSeekTo() {
    Stream stm = new Stream("abcdef");
    stm.seekTo('c');
    assertEquals(stm.peek(), 'd');
    stm.seekTo('z');
    assertEquals(stm.peek(), Chars.EOF);
  }
  
  @Test
  public void testSeekIf() {
    Stream stm = new Stream("abc");
    stm.seekIf('a');
    assertEquals(stm.peek(), 'b');
    stm.seekIf('z');
    assertEquals(stm.peek(), 'b');
  }
  
  @Test
  public void testSkipWhitespace() {
    Stream stm = new Stream("\t\n\r \u000b \tx");
    stm.skipWs();
    assertEquals(stm.peek(), 'x');
  }
}
