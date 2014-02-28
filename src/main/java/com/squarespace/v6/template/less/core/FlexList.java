package com.squarespace.v6.template.less.core;

import java.util.Arrays;
import java.util.NoSuchElementException;


/**
 * A stripped down ArrayList supporting a splice() method, allowing for editing
 * the list in place.  You can insert a list of elements, selectively overwriting
 * some of the existing members. Iteration over the elements in the list is
 * currently done manually, as the list contents and size can change mid-iteration.
 */
public class FlexList<E> {

  private static final int DEFAULT_CAPACITY = 10;
  
  private Object[] elems;
  
  private int size;
  
  public FlexList() {
    this(DEFAULT_CAPACITY);
  }
  
  public FlexList(int capacity) {
    elems = new Object[capacity];
  }

  private FlexList(E[] elems, int size, boolean copy) {
    this.elems = copy ? Arrays.copyOf(elems, size) : elems;
    this.size = size;
  }
  
  public int size() {
    return size;
  }
  
  public void clear() {
    size = 0;
  }

  @SafeVarargs
  public static final <T> FlexList<T> create(T ... objs) {
    return new FlexList<T>(objs, objs.length, false);
  }

  @SuppressWarnings("unchecked")
  public FlexList<E> copy() {
    return new FlexList<E>((E[])elems, size, true);
  }
  
  @SuppressWarnings("unchecked")
  public E get(int i) {
    return (E)elems[i];
  }
  
  public boolean isEmpty() {
    return size == 0;
  }
  
  public void set(int index, E elem) {
    elems[index] = elem;
  }
  
  public void append(E elem) {
    ensure(size + 1);
    elems[size++] = elem;
  }
  
  public void append(FlexList<E> other) {
    splice(size, 0, other);
  }
  
  public void push(E elem) {
    append(elem);
  }
  
  public E pop() {
    if (size == 0) {
      throw new NoSuchElementException();
    }
    size--;
    E elem = get(size);
    return elem;
  }

  @SuppressWarnings("unchecked")
  public void splice(int start, int num, FlexList<E> other) {
    splice(start, num, (E[])other.elems, other.size);
  }
  
  public void splice(int start, int num, E[] other) {
    splice(start, num, other, other.length);
  }
  
  /**
   * Splice elements into the list. Expands capacity if necessary, and relocates
   * elements that would otherwise be overwritten.
   * 
   * This method is tolerant of indices that fall outside the current bounds of
   * the list.  For example, start < 0 will insert at the beginning and 
   * start > size will insert at the end.
   * 
   * @param start  starting point for the insertion
   * @param num    number of elements past 'start' to overwrite
   * @param other  array of elements to copy
   * @param otherSize  number of elements from 'other' to copy
   */
  public void splice(int start, int num, E[] other, int otherSize) {
    if (other == null || otherSize < 0 || otherSize > other.length) {
      return;
    }

    num = num < 0 ? 0 : num;
    start = start < 0 ? 0 : (start > size ? size : start);

    // position of first element we're moving
    int first = start + num; 
    first = first < 0 ? 0 : (first > size ? size : first);

    // position where element will be moved to
    int dest = start + otherSize;

    // number of elements we're moving
    int count = size - first;
    
    // size of this list after operations are completed
    int newSize = dest + count;
    
    ensure(newSize);
    if (count > 0) {
      System.arraycopy(elems, first, elems, dest, count);
    }
    if (otherSize > 0) {
      System.arraycopy(other, 0, elems, start, otherSize);
    }
    size = newSize;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof FlexList) {
      FlexList<?> other = (FlexList<?>)obj;
      if (size != other.size) {
        return false;
      }
      for (int i = 0; i < size; i++) {
        Object e1 = elems[i];
        Object e2 = other.elems[i];
        boolean equal = e1 == null ? e2 == null : e1.equals(e2);
        if (!equal) {
          return false;
        }
      }
      return true;
    }
    return false;
  }
  
  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder("[");
    for (int i = 0; i < size; i++) {
      if (i > 0) {
        buf.append(", ");
      }
      buf.append(elems[i]);
    }
    buf.append(']');
    return buf.toString();
  }
  
  private void ensure(int minCapacity) {
    if (minCapacity - elems.length > 0) {
      grow(minCapacity);
    }
  }
  
  private void grow(int capacity) {
    elems = Arrays.copyOf(elems, capacity + (capacity >> 1));
  }

  @Override
  public int hashCode() {
    throw new UnsupportedOperationException("FlexList instances are not hashable.");
  }
  
}
