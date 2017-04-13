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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * HashPrefixTree is a structure where each key is represented by a list of
 * parts each of type {@code K}.  Keys that share the same prefix will
 * reuse the same interior nodes.  The tree supports searching full matching,
 * prefix matching, and searching by all subsequences of a key.
 *
 * Additionally, each key can have one or more values associated with it.
 *
 * Each level in the tree uses a simple hash table that uses chaining
 * for hash collision overflow.
 *
 * Keys can only be added to the HashPrefixTree, not removed. Removal is currently
 * not a requirement.
 */
public class HashPrefixTree<K, V> {

  /**
   * Initial capacity of the root node.
   */
  private static final int ROOT_CAPACITY = 32;

  /**
   * Initial capacity of interior nodes.
   */
  private static final int INTERIOR_CAPACITY = 4;

  /**
   * Load factor determining the sparseness of each hash table.
   */
  private static final float LOAD_FACTOR = 0.75f;

  /**
   * Comparator for key equivalence.
   */
  private final HPTKeyComparator<K> comparator;

  /**
   * Seed value to mix into keys' hash codes.
   */
  private final int hashSeed;

  /**
   * Root of the tree.
   */
  private HPTNode<K, V> root;

  /**
   * Sequence of key ids for this tree.
   */
  private int keyIdSequence;

  /**
   * Construct a tree with the given key comparator, using the current
   * system timestamp as the hash seed.
   */
  public HashPrefixTree(HPTKeyComparator<K> comparator) {
    this(comparator, (int)System.currentTimeMillis());
  }

  /**
   * Construct a tree with the given key comparator, using the user-provided
   * hash seed.
   */
  public HashPrefixTree(HPTKeyComparator<K> comparator, int hashSeed) {
    this.comparator = comparator;
    this.hashSeed = hashSeed;
  }

  /**
   * Returns the root of the tree.
   */
  public HPTNode<K, V> root() {
    return root;
  }

  /**
   * Inserts the given key and returns the corresponding node.
   */
  public HPTNode<K, V> insert(List<K> key) {
    check(key);
    init();
    int size = key.size();
    HPTNode<K, V> current = root;
    for (int i = 0; i < size; i++) {
      HPTNode<K, V> result = find(current, i, key.get(i), true);
      current.maxDepth = Math.max(current.maxDepth, size - i);
      current = result;
    }
    return current;
  }

  /**
   * Search for the key and return the list of values, or null if the key
   * is not found or has no associated values.  This method also avoids
   * searching for keys that are not long enough to result in a full match,
   * based on the tree depth.
   */
  public HPTNode<K, V> search(List<K> key) {
    check(key);
    int size = key.size();

    // Start at the root, and only search lower in the tree if it is
    // large enough to produce a match.
    HPTNode<K, V> current = root;
    for (int i = 0; i < size; i++) {
      if (current == null || (size - i) > current.maxDepth()) {
        return null;
      }
      current = find(current, i, key.get(i), false);
    }
    if (current == null || current.values == null) {
      return null;
    }
    return current.values.isEmpty() ? null : current;
  }

  /**
   * Match all subsequences of the given key.
   *
   * Given the key ["a", "b", "c", "d"] this will scan subsequences in the order
   * below. Searches marked with "current=root" indicate the search starts back
   * at the root.
   * <pre>
   * Step  Loop state                          Search key
   *
   *  1.   [current=root,   start=0, i=0]      ["a"]
   *  2.   [current=level1, start=0, i=1]      ["a", "b"]
   *  3.   [current=level2, start=0, i=2]      ["a", "b", "c"]
   *  4.   [current=level3, start=0, i=3]      ["a", "b", "c", "d"]
   *
   *  5.   [current=root,   start=1, i=1]      ["b"]
   *  6.   [current=level1, start=1, i=2]      ["b", "c"]
   *  7.   [current=level2, start=1, i=3]      ["b", "c", "d"]
   *
   *  8.   [current=root,   start=2, i=2]      ["c"]
   *  9.   [current=level1, start=2, i=3]      ["c", "d"]
   *
   * 10.   [current=root,   start=3, i=3]      ["d"]
   * </pre>
   * Only subsequences that find at least one value will result in a match. Given
   * the expected sparseness of the tree, the vast majority of these searches
   * will fail fast.  For example, if the root does not contain "a" searching would
   * skip to step 5 above.
   */

  public List<HPTMatch<V>> searchSubsequences(List<K> key) {
    return searchSubsequences(key, null);
  }

  public List<HPTMatch<V>> searchSubsequences(List<K> key, Set<Integer> dupeFilter) {
    check(key);

    // List of matches initialized on first match
    List<HPTMatch<V>> matches = null;

    int size = key.size();
    int start = 0;

    // Outer loop moves the start index towards the end of the key.
    while (start < size) {

      // Start at the root for each starting index.
      HPTNode<K, V> current = root;
      for (int i = start; i < size; i++) {

        K keyPart = key.get(i);
        current = find(current, start - i, keyPart, false);

        // Nothing found, move to next start index.
        if (current == null) {
          break;
        }

        // Check if the current node has values as we move lower into
        // the tree.
        if (current.values != null && !current.values.isEmpty()) {
          if (matches == null) {
            matches = new ArrayList<>();
          }

          // If a dupe filter is present, avoid duplicate matches.
          if (dupeFilter == null || !dupeFilter.contains(current.keyId)) {
            // Add the match start/end indices, where end is exclusive.
            matches.add(new HPTMatch<>(start, i + 1, current.keyId, current.values));
          }
        }
      }

      start++;
    }
    return matches;
  }

  /**
   * Makes sure the key is valid for insertion, searching.
   */
  private void check(List<K> key) {
    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("key must be a list of size >= 1");
    }
  }

  /**
   * Initializes the tree's storage on first insert.
   */
  private void init() {
    if (root == null) {
      root = buildRoot();
    }
  }

  /**
   * Locate a node in the {@code node}'s hash table. If the {@code create} parameter
   * is true we create missing nodes.
   */
  private HPTNode<K, V> find(HPTNode<K, V> node, int keyIndex, K keyPart, boolean create) {

    // Ensure the table is initialized and properly sized
    if (create) {
      expand(node, LOAD_FACTOR);
    }

    // No table allocated, bail out.
    if (node == null || node.children == null) {
      return null;
    }

    // Hash the key to obtain its index in the hash table, then search the
    // chain to find the first match.
    int index = hashKey(keyPart) & (node.children.length - 1);

    // Search the table and overflow chain.
    HPTNode<K, V> result = node.children[index];
    while (result != null) {
      if (comparator.keysEqual(keyIndex, keyPart, result.keyPart)) {
        return result;
      }
      result = result.next;
    }

    // Create the missing node if requested, and move it to the head of the
    // collision overflow chain.
    if (create) {
      return create(node, index, keyPart);
    }
    return result;
  }

  private HPTNode<K, V> create(HPTNode<K, V> parent, int index, K keyPart) {
    HPTNode<K, V> result = new HPTNode<>(keyIdSequence++, keyPart);
    result.next = parent.children[index];
    parent.children[index] = result;
    parent.size++;
    return result;
  }

  /**
   * Expands the {@code node}'s child array, using the load factor to determine
   * the degree of sparseness.
   */
  private void expand(HPTNode<K, V> node, float loadFactor) {
    if (node.children == null) {
      node.children = reallocate(INTERIOR_CAPACITY);
      return;
    } else {
      double threshold = Math.floor(node.children.length * loadFactor) - 1;
      if (node.size < threshold) {
        return;
      }
    }

    // Resize the array
    HPTNode<K, V>[] original = node.children;
    int capacity = node.children.length * 2;
    node.children = reallocate(capacity);

    // Recompute the hashes of all members and add them to the new array.
    int mask = capacity - 1;
    for (int i = original.length - 1; i >= 0; i--) {
      HPTNode<K, V> elem = original[i];
      while (elem != null) {
        int index = hashKey(elem.keyPart) & mask;
        HPTNode<K, V> next = elem.next;
        elem.next = node.children[index];
        node.children[index] = elem;
        elem = next;
      }
    }
  }

  /**
   * Allocates an array of nodes of the given capacity.
   */
  @SuppressWarnings("unchecked")
  protected static <K, V> HPTNode<K, V>[] reallocate(int capacity) {
    return (HPTNode<K, V>[]) new HPTNode[capacity];
  }

  /**
   * Hash mixing function. Attempts to distribute bits of the hash value by XOR-ing
   * some of the higher-order bits. Also mixes in the seed value.
   */
  protected int hashKey(K key) {
    // Murmur3's mix + seed
    int h = hashSeed ^ key.hashCode();
    h ^= h >>> 16;
    h *= 0x85ebca6b;
    h ^= h >>> 13;
    h *= 0xc2b2ae35;
    h ^= h >>> 16;
    return h;
  }

  /**
   * Constructs the root of the tree with a larger initial capacity.
   */
  private HPTNode<K, V> buildRoot() {
    HPTNode<K, V> node = new HPTNode<K, V>(keyIdSequence++, null);
    node.children = reallocate(ROOT_CAPACITY);
    return node;
  }

  /**
   * A node of the tree.
   */
  public static class HPTNode<K, V> {

    /**
     * Unique id of this key within this tree structure. We use
     * this to filter out potential repeated matches.  For example,
     * when we're doing 2 separate queries that are logically
     * part of the same lookup, we want to avoid returning duplicate
     * results on successive calls
     */
    private final int keyId;

    /**
     * Part of the key.
     */
    private final K keyPart;

    /**
     * List of values associated with this node. This is populated only if
     * the key segment represents the end of the key.
     */
    private List<V> values;

    /**
     * Child node hash table.
     */
    private HPTNode<K, V>[] children;

    /**
     * Size of the child node hash table.
     */
    private int size;

    /**
     * Link to the next node which hashes to the same value. Used for
     * hash collision overflow.
     */
    private HPTNode<K, V> next;

    /**
     * Maximum depth of the tree below this point.  We track this as an
     * optimization, to avoid scanning lower levels of the tree that are
     * too shallow to produce a full match.
     */
    private int maxDepth;

    /**
     * Builds a new node for the given key segment.
     */
    public HPTNode(int keyId, K keyPart) {
      this.keyId = keyId;
      this.keyPart = keyPart;
    }

    public int keyId() {
      return keyId;
    }

    /**
     * Returns the maximum depth of the tree below this node.
     */
    public int maxDepth() {
      return maxDepth;
    }

    /**
     * Returns the size of this node's child hash table.
     */
    public int size() {
      return size;
    }

    /**
     * Returns the list of values associated with this node.
     */
    public List<V> values() {
      return values;
    }

    /**
     * Appends a value to this leaf node.
     */
    public HPTNode<K, V> append(V value) {
      if (values == null) {
        values = new ArrayList<>(2);
      }
      values.add(value);
      return this;
    }
  }

  /**
   * Represents a partial key match. Collects the start and end indices
   * of the key, representing the segment of the key which matched. The
   * end index is exclusive.  Also collects all values matched by this
   * key segment.
   */
  // TODO: make <K, V> and return nodes, not values.
  public static class HPTMatch<V> {

    private final int start;

    private final int end;

    private final int keyId;

    private final List<V> values;

    public HPTMatch(int start, int end, int keyId, List<V> values) {
      this.start = start;
      this.end = end;
      this.keyId = keyId;
      this.values = values;
    }

    /**
     * Start index of the segment of the key that matched.
     */
    public int start() {
      return start;
    }

    /**
     * End index of the segment of the key that matched, exclusive.
     */
    public int end() {
      return end;
    }

    /**
     * Unique id of the key corresponding to this match.
     */
    public int keyId() {
      return keyId;
    }

    /**
     * Values found by this match.
     */
    public List<V> values() {
      return values;
    }

    public String toString() {
      return "Match[" + start + ", " + end + "] = " + values;
    }

  }

  /**
   * Compares key segments, with knowledge about position within the key.
   */
  public interface HPTKeyComparator<K> {

    boolean keysEqual(int queryKeyIndex, K queryKey, K treeKey);

  }

}
