package com.squarespace.less.match;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds a double-array trie (DAT) for fast incremental lookup of key / value pairs. We
 * do not store the actual value in the trie, only its index in the entries list provided
 * at build time.
 *
 * WARNING: building large tries at runtime can be time-consuming. You can pre-build the
 * arrays, embed them into a class and construct the DAT at runtime from these components.
 */
class DATBuilder {

  /**
   * Double-array trie fields.
   */
  private int[] base;
  private int[] check;

  /**
   * Indices, preserving original order.
   */
  private int[] indices;

  // The following fields are temporary, used only for building the DAT.

  private Map<String, Integer> indexmap;
  private List<String> input;
  private boolean[] used;
  private int pos;
  private final boolean ignoreCase;

  DATBuilder(List<String> input) {
    this(input, false);
  }

  DATBuilder(List<String> input, boolean ignoreCase) {
    this.input = input;
    this.ignoreCase = ignoreCase;
  }

  /**
   * Base array.
   */
  public int[] base() {
    return base;
  }

  /**
   * Check array.
   */
  public int[] check() {
    return check;
  }

  /**
   * Values indices.
   */
  public int[] indices() {
    return indices;
  }

  /**
   * Build a double-array trie, populating the 'base', 'check', and 'values' arrays.
   */
  public void build() {
    // Copy and sort the input keys
    List<String> sorted = new ArrayList<>();

    // Preserve indices of input order.
    this.indexmap = new HashMap<>();
    for (int i = 0; i < input.size(); i++) {
      String key = input.get(i);
      if (ignoreCase) {
        key = key.toLowerCase();
      }
      if (indexmap.containsKey(key)) {
        throw new IllegalArgumentException(
            "keys must be unique. duplicate found: '" + key + "'");
      }
      indexmap.put(key, i);
      sorted.add(key);
    }

    // Keys should be sorted due to the way the tree is built.
    Collections.sort(sorted);

    N root = new N();
    root.left = 0;
    root.right = sorted.size();
    root.depth = 0;

    // Size of the trie depends on the nature of the characters in the keys.
    int size = 65000;
    while (true) {
      try {
        alloc(size);
        // Get the siblings of the root node. This is the set of first
        // characters for all strings.
        List<N> siblings = tree(sorted, root);
        build(sorted, siblings);
        break;
      } catch (ArrayIndexOutOfBoundsException e) {
        // Size was too conservative, increase it by a bit
        size += size / 5;
      }
    }

    trim();

    // Clear the temporary variables
    this.indexmap = null;
    this.input = null;
    this.used = null;
  }

  /**
   * Trim arrays to minimal size.
   */
  private void trim() {
    int len = base.length;
    while (len >= 0 && base[len - 1] == 0) {
      len--;
    }

    int[] newbase = new int[len];
    System.arraycopy(base, 0, newbase, 0, len);
    this.base = newbase;

    int[] newcheck = new int[len];
    System.arraycopy(check, 0, newcheck, 0, len);
    this.check = newcheck;

    int[] newindices = new int[len];
    System.arraycopy(indices, 0, newindices, 0, len);
    this.indices = newindices;
  }

  private void alloc(int sz) {
    base = new int[sz];
    check = new int[sz];
    indices = new int[sz];
    used = new boolean[sz];
    pos = 0;
    base[0] = 1;
    Arrays.fill(indices, -1);
  }

  /**
   * <pre>
   * To construct the tree, we recursively locate all siblings having
   * the given 'parent' prefix.
   *
   * For the strings ["ANT", "AT", "OAKS", "OAR"] this produces the tree:
   *
   * [root]
   *    │
   *    ├─ A
   *    │  ├─ N
   *    │  │  └─ T
   *    │  │     └─ 0
   *    │  │
   *    │  └─ T
   *    │     └─ 0
   *    └─ O
   *       └─ A
   *          ├─ K
   *          │  └─ S
   *          │     └─ 0
   *          └─ R
   *             └─ 0
   *
   * The siblings are all characters at the same depth under the
   * same prefix, e.g. {'N', 'T'} are siblings immediately under 'A',
   * and the siblings {'K', 'R'} share the same prefix "OA".
   *
   * The zeros distinguish prefixes from full matches, e.g. the trie
   * does not contain the key "AN" even though it is a prefix of "ANT".
   *
   * To construct the tree we scan all keys under the parent in order
   * from left to right. For the strings in the above example this routine
   * scans from top to bottom, with tree depth 1-4 and computed siblings
   * on the right:
   *
   *  depth         inputs       sibling sets
   *  ----------------------------------------------
   *    1:  'A' 'A' 'O' 'O'  → {'A',       'O'}
   *    2:  'N' 'T' 'A' 'A'  → {'N', 'T'} {'A'}
   *    3:  'T'     'K' 'R'  → {'T'}      {'K', 'R'}
   *    4:          'S'      →            {'S'}
   *
   * </pre>
   */
  private List<N> tree(List<String> keys, N parent) {
    List<N> sibs = new ArrayList<>();
    int prev = 0;

    for (int i = parent.left; i < parent.right; i++) {
      String key = keys.get(i);

      // Check if all chars for this key have been scanned
      if (key.length() < parent.depth) {
        continue;
      }

      // Locate the character in this string at the current depth,
      // e.g. at root depth 0 we're using the first character of each string.
      int curr = 0;
      if (key.length() != parent.depth) {
        curr = (int)key.charAt(parent.depth);
      }

      // If this is the first time we've seen this character, create
      // a sibling node at this level.
      if (curr != prev || sibs.isEmpty()) {
        N n = new N();
        n.depth = parent.depth + 1;
        n.code = curr;
        n.left = i;

        // Check if this is the last character in the current key and map
        // it to the index of its corresponding value.
        char last_char = key.charAt(key.length() - 1);
        if (curr == 0 || curr == last_char) {
          n.index = indexmap.get(key);
        }
        if (!sibs.isEmpty()) {
          // Set right point of previous sibling to the current sibling
          sibs.get(sibs.size() - 1).right = i;
        }
        sibs.add(n);
      }

      prev = curr;
    }
    if (!sibs.isEmpty()) {
      sibs.get(sibs.size() - 1).right = parent.right;
    }
    return sibs;
  }

  /**
   * Build the DAT by indexing the given siblings.
   */
  private int build(List<String> keys, List<N> siblings) {
    int sz = siblings.size();

    N nzero = siblings.get(0);

    // Find the starting offset for this level
    int index = nzero.code > pos ? nzero.code : pos;
    boolean is_first = true;

    // Scan for a block of empty slots to index the siblings.
    int begin = 0;
    loop:
    while (true) {
      index++;

      if (check[index] != 0) {
        continue;
      }
      if (is_first) {
        pos = index;
        is_first = false;
      }

      begin = index - nzero.code;
      if (used[begin]) {
        continue;
      }

      for (int i = 1; i < sz; i++) {
        N n = siblings.get(i);
        if (check[begin + n.code] != 0) {
          continue loop;
        }
      }

      break;
    }

    used[begin] = true;

    // Initialize the 'check' cell for each sibling
    for (int i = 0; i < sz; i++) {
      N n = siblings.get(i);
      check[begin + n.code] = begin;
    }

    // Iterate over the siblings and recursively index them
    for (int i = 0; i < sz; i++) {
      // Get the siblings that are a prefix of 'n'
      N n = siblings.get(i);
      List<N> newsibs = tree(keys, n);

      // Pointer to the cell for the current node
      int j = begin + n.code;

      if (newsibs.isEmpty()) {
        // No siblings for this prefix, so we've reached a leaf.
        base[j] = -1;
        check[j] = 1;
        indices[j] = n.index;

      } else {
        // Create the link to the next level
        int h = build(keys, newsibs);
        base[j] = h;
      }
    }

    return begin;
  }

  /**
   * Node in the temporary tree.
   */
  static class N {

    int code;

    int left;

    int right;

    int depth;

    int index;

    @Override
    public String toString() {
      return "N('" + (char)code + "' (" + code + ") " + " L=" + left + " R=" + right
          + " D=" + depth + " I=" + index + ")";
    }

  }

}