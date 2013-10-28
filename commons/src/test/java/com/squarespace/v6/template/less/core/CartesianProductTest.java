package com.squarespace.v6.template.less.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import org.testng.Assert;
import org.testng.annotations.Test;


public class CartesianProductTest {

  @Test
  public void testBasics() {
    List<String> list1 = Arrays.asList("a", "b");
    List<String> list2 = Arrays.asList("1", "2", "3");
    CartesianProduct<String> product = new CartesianProduct<>(Arrays.asList(list1, list2));
    for (String elem1 : list1) {
      for (String elem2 : list2) {
        Assert.assertEquals(Arrays.asList(elem1, elem2), product.next());
      }
    }
    Assert.assertTrue(!product.hasNext());
    try {
      product.next();
      Assert.fail("Expected IllegalStateException");
    } catch (NoSuchElementException e) {
    }
  }
  
  @Test
  public void testOneEmpty() {
    List<String> list1 = Arrays.asList("a", "b");
    List<String> list2 = Collections.emptyList();
    List<String> list3 = Arrays.asList("1", "2");
    List<String> list4 = Collections.emptyList();
    CartesianProduct<String> product = new CartesianProduct<>(Arrays.asList(list1, list2, list3, list4));
    for (String elem1 : list1) {
      for (String elem2 : list3) {
        Assert.assertEquals(Arrays.asList(elem1, elem2), product.next());
      }
    }
    Assert.assertTrue(!product.hasNext());
  }
  
  @Test
  public void testAllEmpty() {
    List<String> list1 = Collections.emptyList();
    List<String> list2 = Collections.emptyList();
    List<String> list3 = Collections.emptyList();
    CartesianProduct<String> product = new CartesianProduct<>(Arrays.asList(list1, list2, list3));
    Assert.assertTrue(!product.hasNext());
  }
  
  @Test
  public void testNulls() {
    List<Integer> list1 = Arrays.asList(1, 2);
    List<Integer> list2 = Arrays.<Integer>asList(3, null);
    List<Integer> list3 = Arrays.<Integer>asList(null, 5);
    CartesianProduct<Integer> product = new CartesianProduct<>(Arrays.asList(list1, list2, list3));
    for (Integer elem1 : list1) {
      for (Integer elem2 : list2) {
        for (Integer elem3 : list3) {
          Assert.assertEquals(Arrays.asList(elem1, elem2, elem3), product.next());
        }
      }
    }
    Assert.assertTrue(!product.hasNext());
  }
  
}
