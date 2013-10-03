package com.squarespace.v6.template.less.core;

import java.util.Random;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.core.CharClass;


public class CharClassTest {

  @Test
  public void testBasic() {
    for (int i = 0; i < 50; i++) {
      testIt(10000, 0x80);
    }
  }

  private void testIt(int iters, int limit) {
    Random r = new Random();
    char[] chars = new char[iters];
    for (int i = 0; i < iters; i++) {
      chars[i] = (char)r.nextInt(limit);
    }
    
    long start, elapsed;
    int x = 0;

    start = System.nanoTime();
    for (int i = 0; i < iters; i++) {
      x += CharClass.whitespace(chars[i]) ? 1 : 0;
    }
    elapsed = System.nanoTime() - start;
    System.out.println(" table = " + elapsed);

//    start = System.nanoTime();
//    for (int i = 0; i < iters; i++) {
//      x += Chars.whitespace(chars[i]) ? 1 : 0;
//    }
//    elapsed = System.nanoTime() - start;
//    System.out.println("method = " + elapsed);

    
    System.out.println("\nx = " + x + "\n");
  }

}
