package com.squarespace.v6.template.less.core;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.fail;

import java.util.NoSuchElementException;

import org.testng.annotations.Test;


public class FlexListTest {

  private static final FlexList<Object> ALPHA7 = FlexList.<Object>create('a', 'b', 'c', 'd', 'e', 'f', 'g');
  
  private static final FlexList<Object> ALPHA3 = FlexList.<Object>create('a', 'b', 'c');

  private static final Object[] DIGIT4 = new Object[] { 1, 2, 3, 4 };

  private static final Object[] DIGIT3 = new Object[] { 1, 2, 3 };
  
  private static final Object[] DIGIT2 = new Object[] { 1, 2 };
  
  private static final Object[] EMPTY = new Object[] { };

  @Test
  public void testUsageExample() {
    FlexList<Object> list = FlexList.<Object>create(1, 2, 3, 4, 5);

    Object[] insert = new Object[] { 'x', 'y' };
    int overwrite = 1;
    
    // List's size can change on any given iteration, so do not cache
    // the size in advance.
    for (int i = 0; i < list.size(); i++) {
      if (i == 1 || i == 4) {
        list.splice(i, overwrite, insert);
        
        // skip over the numInserted - numOverwritten
        i += insert.length - overwrite;
      }
    }
    assertEquals(list, FlexList.<Object>create(1, 'x', 'y', 3, 'x', 'y', 5));
  }

  @Test
  public void testEquals() {
    FlexList<Object> list1 = FlexList.<Object>create(1, 2, 3);
    FlexList<Object> list2 = FlexList.<Object>create(1, 2, 3);
    assertEquals(list1, list2);
    
    list2 = ALPHA3.copy();
    assertNotEquals(list1, list2);
  }
  
  @Test
  public void testReplace() {
    FlexList<Object> expected;
    
    FlexList<Object> list1 = ALPHA7.copy();
    list1.splice(2,  2, DIGIT4, 4);
    expected = FlexList.<Object>create('a', 'b', 1, 2, 3, 4, 'e', 'f', 'g');
    assertEquals(list1, expected);
    
    list1 = ALPHA7.copy();
    list1.splice(2,  1,  DIGIT3, 3);
    expected = FlexList.<Object>create('a', 'b', 1, 2, 3, 'd', 'e', 'f', 'g');
    assertEquals(list1, expected);

    list1 = ALPHA7.copy();
    list1.splice(4,  0,  DIGIT3, 3);
    expected = FlexList.<Object>create('a', 'b', 'c', 'd', 1, 2, 3, 'e', 'f', 'g');
    assertEquals(list1, expected);

    list1 = ALPHA3.copy();
    list1.splice(2, 0, DIGIT2, 2);
    expected = FlexList.<Object>create('a', 'b', 1, 2, 'c');
    assertEquals(list1, expected);
    
    list1 = ALPHA3.copy();
    list1.splice(3, 0, DIGIT2, 2);
    expected = FlexList.<Object>create('a', 'b', 'c', 1, 2);
    assertEquals(list1, expected);
  }
  
  @Test
  public void testDelete() {
    FlexList<Object> expected;
    
    FlexList<Object> list1 = ALPHA7.copy();
    list1.splice(2, 2, EMPTY);
    expected = FlexList.<Object>create('a', 'b', 'e', 'f', 'g');
    assertEquals(list1, expected);
    
    list1 = ALPHA7.copy();
    list1.splice(0, 5, EMPTY);
    expected = FlexList.<Object>create('f', 'g');
    assertEquals(list1, expected);
    
    list1 = ALPHA7.copy();
    list1.splice(0, 10, EMPTY);
    expected = FlexList.<Object>create();
    assertEquals(list1, expected);
}
  
  @Test
  public void testBounds() {
    FlexList<Object> expected;

    // Range appends
    FlexList<Object> list1 = ALPHA3.copy();
    list1.splice(10, 10, DIGIT3, 3);
    expected = FlexList.<Object>create('a', 'b', 'c', 1, 2, 3);
    assertEquals(list1, expected);

    // Range prepends
    list1 = ALPHA3.copy();
    list1.splice(-5, -5, DIGIT3, 3);
    expected = FlexList.<Object>create(1, 2, 3, 'a', 'b', 'c');
    assertEquals(list1, expected);

    // Range overwrites entire list
    list1 = ALPHA3.copy();
    list1.splice(-5, 50, DIGIT3, 3);
    expected = FlexList.<Object>create(1, 2, 3);
    assertEquals(list1, expected);

    // Range overwrites end of list
    list1 = ALPHA7.copy();
    list1.splice(2, 10, DIGIT3, 3);
    expected = FlexList.<Object>create('a', 'b', 1, 2, 3);
    assertEquals(list1, expected);
  }
  
  @Test
  public void testAppend() {
    FlexList<Object> expected;
    
    FlexList<Object> list1 = ALPHA3.copy();
    list1.append(ALPHA7);
    expected = FlexList.<Object>create('a', 'b', 'c', 'a', 'b', 'c', 'd', 'e', 'f', 'g');
    assertEquals(list1, expected);
  }

  @Test
  public void testStackMethods() {
    FlexList<Object> list = new FlexList<>();
    list.push('a');
    list.push('b');
    assertEquals(list.pop(), 'b');
    assertEquals(list.pop(), 'a');
    try {
      list.pop();
      fail("Expected NoSuchElementException to be thrown");
    } catch (NoSuchElementException e) {
      
    }
  }
  
}
