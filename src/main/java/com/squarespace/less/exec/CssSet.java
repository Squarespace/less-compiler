package com.squarespace.less.exec;

import java.util.Iterator;

/**
 * Implementation of a set that maintains insertion order without removal.
 */
public class CssSet<E> implements Iterable<E> {

  /**
   * Initial capacity of the set by default. MUST be a power of 2.
   */
  private static final int DEFAULT_CAPACITY = 32;

  /**
   * Percent of free slots we maintain.
   */
  private static final float DEFAULT_LOAD_FACTOR = 0.75f;

  protected Node<E> head;
  protected Node<E> tail;
  protected Node<E>[] table;
  protected int mask;
  protected int size;

  public CssSet() {
    this(DEFAULT_CAPACITY);
  }

  @SuppressWarnings("unchecked")
  public CssSet(int capacity) {
    Node<E> root = new Node<>(null);
    root.next = root;
    root.prev = root;
    this.head = root;
    this.tail = root;
    this.table = new Node[capacity];
    this.mask = table.length - 1;
    this.size = 0;
  }

  /**
   * Number of elements in the set.
   */
  public int size() {
    return size;
  }

  /**
   * Add a new element to the set. If the element already exists in the
   * set, move it to the end of the ordered list of nodes.
   */
  public void add(E elem) {
    grow();
    int ix = locate(elem);
    Node<E> e = null;
    if (table[ix] == null) {
      // Insert the element and increase set size
      e = new Node<>(elem);
      size++;
      table[ix] = e;

    } else {
      // Element exists, so remove it
      e = table[ix];
      this.remove(e);
    }

    // Insert / move the element to the end of the ordered list
    this.insertBefore(e, this.tail);
  }

  @Override
  public Iterator<E> iterator() {
    return new CssSetIterator<E>(this.head.next);
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    Node<E> n = this.head.next;
    while (n != this.tail) {
      if (buf.length() > 0) {
        buf.append(" ");
      }
      buf.append(n.elem);
      n = n.next;
    }
    return buf.toString();
  }

  /**
   * Locate an element's index in the table.
   */
  private int locate(E elem) {
    int ix = hash(elem.hashCode()) & mask;
    while (table[ix] != null) {
      if (elem.equals(table[ix].elem)) {
        return ix;
      }
      ix = (ix + 1) & mask;
    }
    return ix;
  }

  private Node<E> insertBefore(Node<E> e, Node<E> at) {
    Node<E> n = at.prev;
    at.prev = e;
    e.prev = n;
    e.next = at;
    n.next = e;
    return e;
  }

  private Node<E> remove(Node<E> n) {
    n.prev.next = n.next;
    n.next.prev = n.prev;
    return n;
  }

  /**
   * Grow the table to ensure size is below load factor threshold.
   */
  @SuppressWarnings("unchecked")
  private void grow() {
    // Check if storage is big enough
    if (size < Math.floor(table.length * DEFAULT_LOAD_FACTOR)) {
      return;
    }

    // Double the table size
    Node<E>[] old = table;
    table = new Node[table.length * 2];

    // Rehash the contents to new locations
    int newmask = table.length - 1;
    for (int i = old.length - 1; i >= 0; i--) {
      Node<E> elem = old[i];
      if (elem != null) {
        int ix = hash(elem.hashCode()) & newmask;
        while (table[ix] != null) {
          ix = (ix + 1) & newmask;
        }
        table[ix] = elem;
      }
    }
    this.mask = newmask;
  }

  /**
   * Murmur3 32-bit hash.
   */
  static int hash(int key) {
    key ^= key >>> 16;
    key *= 0x85ebca6b;
    key ^= key >>> 13;
    key *= 0xc2b2ae35;
    key ^= key >>> 16;
    return key;
  }

  public static class CssSetIterator<E> implements Iterator<E> {

    private Node<E> curr;

    private CssSetIterator(Node<E> node) {
      this.curr = node;
    }

    @Override
    public boolean hasNext() {
      return this.curr.elem != null;
    }

    @Override
    public E next() {
      E elem = this.curr.elem;
      this.curr = this.curr.next;
      return elem;
    }
  }

  /**
   * Internal node in our table / ordered list.
   */
  private static class Node<E> {
    final E elem;
    Node<E> prev;
    Node<E> next;

    Node(E elem) {
      this.elem = elem;
    }
  }
}
