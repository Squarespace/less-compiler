package com.squarespace.less.match;

/**
 * Double-array trie (DAT) for fast, incremental lookup of key / value pairs.
 * See the DATBuilder for the trie construction code.
 *
 * Note: this is intended to be built once and never modified, so it does not
 * support insertion or removal.
 *
 * See: "An Efficient Digital Search Algorithm by Using a Double-Array Structure"
 * J. AOE, 1989 https://dl.acm.org/doi/10.1109/32.31365
 *
 * This is a nice option for parsing since we can incrementally scan our input
 * string and compare each character against the trie, and aborting on the first miss.
 * Storage of the trie is compact, using 2 arrays instead of pointer-chasing down a
 * tree. A 3rd array stores the indices of the values for successful matches.
 *
 * For example, if our trie only holds lowercase strings a search for "APPLESAUCE"
 * will fail on the first character.
 *
 * This has advantages over a vanilla hashmap lookup as it avoids both copying and
 * hashing the substring used to query the map. This is great for speculative lookups
 * that may fail frequently.
 *
 * <pre>
 * For example, querying a vanilla hashmap containing only lowercase strings requires
 * that we copy and hash the substring:
 *
 *    String raw = "xxxxxxxAPPLESAUCExxxxx";
 *    String key = raw.substring(7, 17);
 *    Object value = MAP.get(key);
 *
 * Lookups in the trie require no allocations or hashing, and fail-fast on the first
 * non-matching character:
 *
 *    String raw = "xxxxxxxAPPLESAUCExxxxx";
 *    int i = DAT.get(raw, 7, 17);
 *    Object value = i == -1 ? null : VALUES[i];
 *
 * </pre>
 */
public class DAT {

  protected final int[] base;
  protected final int[] check;
  protected final int[] indices;
  protected final int length;

  /**
   * Construct a DAT from components that were pre-generated.
   */
  public DAT(int[] base, int[] check, int[] indices) {
    this.base = base;
    this.check = check;
    this.indices = indices;
    this.length = base.length;

    if (length != check.length || length != indices.length) {
      throw new RuntimeException("error, array dimension mismatch: " + base.length + ", "
          + check.length + ", " + indices.length);
    }
  }

  /**
   * Scan characters in 'seq' from 'pos' up to 'len', incrementally
   * searching the trie for a match. If we find a match, we'll
   * reach the state where 'base[s] == -1' and will return the
   * index of the corresponding value. Otherwise we return -1.
   *
   * <pre>
   *
   * Below is a trie containing the keys ["ABC", "AAB"]. Note that the
   * array range has been truncated to hide the unused cells:
   *
   *   index [  0,  1,  ...  65, 66, 67, 68, 69, 70, 71, 72]
   *         -----------------------------------------------
   *    base [  1,  0,  ...   0,  2,  3,  4, 70, -1, 72, -1]
   *   check [  0,  0,  ...   0,  1,  2,  2,  3,  1,  4,  1]
   * indices [ -1, -1,  ...  -1, -1, -1, -1, -1,  1, -1,  0]
   *
   *
   * Searching for the key "ABC" {65, 66, 67}:
   *
   *    s = base[0]            ; initial state s = 1
   *
   *    t = s + 'A'            ; t = (1 + 65)
   *    assert check[t] == s   ; check[66] == 1
   *    s = base[t]            ; s = 2
   *
   *    t = s + 'B'            ; t = (2 + 66)
   *    assert check[t] == s   ; check[68] == 2
   *    s = base[t]            ; s = 4
   *
   *    t = s + 'C'            ; t = (4 + 67)
   *    assert check[t] == s   ; check[71] == 4
   *    s = base[t]            ; s = 72
   *
   *    assert base[s] == -1   ; true, we have a match
   *    r = indices[s]         ; r = 0, values[r] == "ABC"
   *
   *
   * Searching for the missing key "ADC" {65, 68, 67}:
   *
   *    s = base[0]            ; initial state s = 1
   *
   *    t = s + 'A'            ; t = (1 + 65)
   *    assert check[t] == s:  ; check[66] == 1
   *    s = base[t]            ; s = 2
   *
   *    t = s + 'D'            ; t = (2 + 68)
   *    assert check[t] == s   ; check[70] != 2, fail!
   *
   * </pre>
   */
  public int get(String seq, int pos, int len) {
    // Initial state
    int s = base[0];

    // Iterate over all characters in substring seq[pos:len]
    for (int i = pos; i < len; i++) {
      // Compute next state on input character seq[i]
      int t = s + seq.charAt(i);

      // Sanity check that the next state is within bounds
      if (t >= length || t < 0) {
        return -1;
      }

      // Check that the state transition is valid
      if (s != check[t]) {
        return -1;
      }

      // Move to the next state
      s = base[t];
    }

    // If we reached a final state, return the index of the matched
    // key; otherwise return -1.
    return s < 0 ? -1 : base[s] == -1 ? indices[s] : -1;
  }

  /**
   * Same as get() above, but ignoring case. NOTE: the DAT must have
   * been built with "ignore case" enabled.
   */
  public int getIgnoreCase(String seq, int pos, int len) {
    int s = base[0];
    for (int i = pos; i < len; i++) {
      int t = s + Character.toLowerCase(seq.charAt(i));
      if (t >= length || t < 0) {
        return -1;
      }
      if (s != check[t]) {
        return -1;
      }
      s = base[t];
    }
    return s < 0 ? -1 : base[s] == -1 ? indices[s] : -1;
  }

}
