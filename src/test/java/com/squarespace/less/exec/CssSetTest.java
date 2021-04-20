package com.squarespace.less.exec;

import static org.testng.Assert.assertEquals;

import java.util.Iterator;

import org.testng.annotations.Test;

public class CssSetTest {

  @Test
  public void testBasic() {
    CssSet<String> set = new CssSet<String>();
    set.add("foo");
    set.add("bar");
    set.add("foo");
    assertEquals(set.size(), 2);
    set.add("quux");
    assertEquals(set.size(), 3);

    Iterator<String> iter = set.iterator();
    assertEquals(iter.hasNext(), true);
    assertEquals(iter.next(), "bar");
    assertEquals(iter.hasNext(), true);
    assertEquals(iter.next(), "foo");
    assertEquals(iter.hasNext(), true);
    assertEquals(iter.next(), "quux");
    assertEquals(iter.hasNext(), false);
    assertEquals(iter.next(), null);

    assertEquals(set.toString(), "bar foo quux");
  }

  @Test
  public void testHashCollisions() {
    CssSet<Dummy> set = new CssSet<>(16);
    set.add(new Dummy("foo"));
    set.add(new Dummy("bar"));
    set.add(new Dummy("foo"));
    assertEquals(set.size(), 2);
    set.add(new Dummy("quux"));
    assertEquals(set.size(), 3);
  }

  @Test
  public void testGrow() {
    CssSet<String> set = new CssSet<>(16);
    for (int i = 0; i < 32; i++) {
      set.add("foo" + i);
    }
    assertEquals(set.size(), 32);
  }

  static class Dummy {
    final String val;
    Dummy(String s) {
      this.val = s;
    }

    @Override
    public int hashCode() {
      return 1;
    }

    @Override
    public boolean equals(Object obj) {
      return obj instanceof Dummy ? this.val.equals(((Dummy)obj).val) : false;
    }

    @Override
    public String toString() {
      return this.val;
    }
  }
}
