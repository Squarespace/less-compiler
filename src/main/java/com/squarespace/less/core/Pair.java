package com.squarespace.less.core;


/**
 * Holds a key-value pair.
 */
public class Pair<K, V> {

  private final K key;
  private final V val;

  private Pair(K k, V v) {
    this.key = k;
    this.val = v;
  }

  public static <K, V> Pair<K, V> of(K key, V val) {
    return new Pair<>(key, val);
  }

  public K key() {
    return key;
  }

  public V val() {
    return val;
  }
}
