/**
 * Copyright, 2015, Squarespace, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.squarespace.less.core;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.testng.annotations.Test;

import com.squarespace.less.core.HashPrefixTree.HPTKeyComparator;
import com.squarespace.less.core.HashPrefixTree.HPTMatch;


public class HashPrefixTreeTest extends LessTestBase {

  private static final String SYMBOLS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

  private static final int SYMBOLS_LEN = SYMBOLS.length();

  private static final HPTKeyComparator<String> STRING_COMPARATOR = new HPTKeyComparator<String>() {
    @Override
    public boolean keysEqual(int keyIndex, String keyA, String keyB) {
      return keyA.hashCode() == keyB.hashCode() && keyA.equals(keyB);
    }
  };

  @Test
  public void testBasicSearch() {
    HashPrefixTree<String, Integer> tree = new HashPrefixTree<>(STRING_COMPARATOR, 1309);

    tree.insert(key("aa", "bb", "cc")).append(1);
    tree.insert(key("aa", "bb", "dd", "ee")).append(2);
    tree.insert(key("aa", "bb", "cc", "dd")).append(3);
    tree.insert(key("aa", "cc")).append(4);
    tree.insert(key("aa")).append(5);
    tree.insert(key("xx", "yy", "zz")).append(6).append(7).append(8);
    tree.insert(key("qq", "rr")).append(9);
    tree.insert(key("bb")).append(10);
    tree.insert(key("cc")).append(11);
    tree.insert(key("dd")).append(12);
    tree.insert(key("ee")).append(13);
    tree.insert(key("ff")).append(14);
    tree.insert(key("gg")).append(15);

    assertEquals(tree.search(key("aa", "bb", "cc")), asList(1));
    assertEquals(tree.search(key("aa", "bb", "dd", "ee")), asList(2));
    assertEquals(tree.search(key("aa", "bb", "cc", "dd")), asList(3));
    assertEquals(tree.search(key("aa", "cc")), asList(4));
    assertEquals(tree.search(key("aa")), asList(5));
    assertEquals(tree.search(key("xx", "yy", "zz")), asList(6, 7, 8));
    assertEquals(tree.search(key("qq", "rr")), asList(9));
    assertEquals(tree.search(key("bb")), asList(10));
    assertEquals(tree.search(key("cc")), asList(11));
    assertEquals(tree.search(key("dd")), asList(12));
    assertEquals(tree.search(key("ee")), asList(13));
    assertEquals(tree.search(key("ff")), asList(14));
    assertEquals(tree.search(key("gg")), asList(15));
  }

  @Test
  public void testFill() {
    HashPrefixTree<String, Integer> tree = new HashPrefixTree<>(STRING_COMPARATOR, 1309);
    for (int i = 0; i < SYMBOLS_LEN; i++) {
      tree.insert(key("one", "two", SYMBOLS.substring(i))).append(i);
    }
    for (int i = 0; i < SYMBOLS_LEN; i++) {
      assertEquals(tree.search(key("one", "two", SYMBOLS.substring(i))), asList(i));
    }
  }

  @Test
  public void testRandomizedKeys() {
    int iters = 1000;
    int numKeys = 512;
    int maxDepth = 10;

    // Keys designed to produce failed searches by being 1 char shorted
    List<List<String>> missKeys = randomKeys(numKeys, new Random(1309), maxDepth, 9);

    for (int seed = 1; seed < iters; seed++) {
      Random random = new Random(seed);

      List<List<String>> keys = randomKeys(numKeys, random, maxDepth, 10);

      // Insert
      HashPrefixTree<String, Integer> tree = new HashPrefixTree<>(STRING_COMPARATOR, seed);
      for (int i = 0; i < keys.size(); i++) {
        tree.insert(keys.get(i)).append(i).append(i);
      }

      // Search
      for (int i = 0; i < keys.size(); i++) {
        assertEquals(tree.search(keys.get(i)), asList(i, i));
      }

      for (int i = 0; i < missKeys.size(); i++) {
        assertEquals(tree.searchSubsequences(missKeys.get(i)), null);
      }
    }
  }

  @Test
  public void testLongRandomizedKeys() {
    int seed = 1309;
    int numKeys = 1024;
    int maxDepth = 30;

    Random random = new Random(seed);
    List<List<String>> keys = randomKeys(numKeys, random, maxDepth, 30);

    List<String> miss = randomKey(random, 10, 5);

    HashPrefixTree<String, Integer> tree = new HashPrefixTree<>(STRING_COMPARATOR, seed);
    for (int i = 0; i < keys.size(); i++) {
      List<String> key = new ArrayList<>();
      key.add("root");
      key.addAll(keys.get(i));
      tree.insert(key).append(i);
    }

    for (int i = 0; i < keys.size(); i++) {
      List<String> key = new ArrayList<>();
      key.add("root");
      key.addAll(keys.get(i));
      assertEquals(tree.search(key), asList(i));
      assertEquals(tree.search(miss), null);
    }
  }

  @Test
  public void testSearchSubsequences() {
    int seed = 1309;
    HashPrefixTree<String, Integer> tree = new HashPrefixTree<>(STRING_COMPARATOR, seed);

    List<List<String>> keys = Arrays.asList(
        key("a"),
        key("a", "b"),
        key("a", "b", "c"),
        key("b", "c"),
        key("c")
      );

    // Insert every subsequence.
    for (int i = 0; i < keys.size(); i++) {
      tree.insert(keys.get(i)).append(i);
    }

    // Insert some other keys that should not match.
    tree.insert(key("d")).append(-1);
    tree.insert(key("a", "c")).append(-1);
    tree.insert(key("c", "d")).append(-1);
    tree.insert(key("b", "c", "d")).append(-1);

    // Search for all subsequences of the key ["a", "b", "c"]
    List<String> key = key("a", "b", "c");
    List<HPTMatch<Integer>> matches = tree.searchSubsequences(key);

    assertNotEquals(matches, null);
    assertEquals(matches.size(), 5);

    HPTMatch<Integer> match = matches.get(0);
    assertEquals(key.subList(match.start(), match.end()), asList("a"));
    assertEquals(match.values(), asList(0));

    match = matches.get(1);
    assertEquals(key.subList(match.start(), match.end()), asList("a", "b"));
    assertEquals(match.values(), asList(1));

    match = matches.get(2);
    assertEquals(key.subList(match.start(), match.end()), asList("a", "b", "c"));
    assertEquals(match.values(), asList(2));

    match = matches.get(3);
    assertEquals(key.subList(match.start(), match.end()), asList("b", "c"));
    assertEquals(match.values(), asList(3));

    match = matches.get(4);
    assertEquals(key.subList(match.start(), match.end()), asList("c"));
    assertEquals(match.values(), asList(4));
  }

  private List<List<String>> randomKeys(int numKeys, Random random, int maxLength, int segmentLength) {
    // Build keys based on current seed
    Set<List<String>> keySet = new HashSet<>();
    for (int i = 0; i < numKeys; i++) {
      keySet.add(randomKey(random, maxLength, segmentLength));
    }

    // May end up smaller than numKeys due to random dupes
    return new ArrayList<>(keySet);
  }

  private List<String> randomKey(Random random, int maxLength, int segmentLength) {
    List<String> key = new ArrayList<>();
    int len = random.nextInt(maxLength) + 1;
    for (int i = 0; i < len; i++) {
      key.add(randomString(random, segmentLength));
    }
    return key;
  }

  private String randomString(Random random, int len) {
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < len; i++) {
      buf.append(SYMBOLS.charAt(random.nextInt(SYMBOLS_LEN)));
    }
    return buf.toString();
  }

  private List<String> key(String ... key) {
    return Arrays.asList(key);
  }

}
