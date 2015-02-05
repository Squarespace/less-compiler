/**
 * Copyright (c) 2014 SQUARESPACE, Inc.
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

import java.util.Arrays;
import java.util.NoSuchElementException;


/**
 * A stripped down ArrayList supporting a splice() method.
 *
 * A list of elements can be inserted, selectively overwriting some of the
 * existing members.
 *
 * Iteration over the elements in the list currently requires continually polling
 * for the list's size, as the contents and size can change mid-iteration.
 */
public class FlexList<E> {

  private static final int DEFAULT_CAPACITY = 10;

  private Object[] elems;

  private int size;

  /**
   * Constructs a list with the default initial capacity.
   */
  public FlexList() {
    this(DEFAULT_CAPACITY);
  }

  /**
   * Constructs a list with the given initial capacity.
   */
  public FlexList(int capacity) {
    elems = new Object[capacity];
  }

  /**
   * Private constructor. Either copies or references the array of
   * elements, with the given initial size.
   */
  private FlexList(E[] elems, int size, boolean copy) {
    this.elems = copy ? Arrays.copyOf(elems, size) : elems;
    this.size = size;
  }

  /**
   * Returns the current number of elements in the list.
   */
  public int size() {
    return size;
  }

  /**
   * Logical clear of the list. Just sets the size to zero.
   */
  public void clear() {
    size = 0;
  }

  /**
   * Constructs a list with {@code objs} as the initial contents.
   */
  public static final <T> FlexList<T> create(T[] objs) {
    return new FlexList<T>(objs, objs.length, false);
  }

  /**
   * Creates a shallow copy of this list, by copying the internal array.
   */
  @SuppressWarnings("unchecked")
  public FlexList<E> copy() {
    return new FlexList<E>((E[])elems, size, true);
  }

  /**
   * Gets the element at index {@code index}. Note: no bounds checking is done.
   */
  @SuppressWarnings("unchecked")
  public E get(int index) {
    return (E)elems[index];
  }

  /**
   * If the list is empty.
   */
  public boolean isEmpty() {
    return size == 0;
  }

  /**
   * Sets the element at {@code index}. Note: no bounds checking is done.
   */
  public void set(int index, E elem) {
    elems[index] = elem;
  }

  /**
   * Appends an element to the end of the list.
   */
  public void append(E elem) {
    ensureCapacity(size + 1);
    elems[size++] = elem;
  }

  /**
   * Appends all of {@code other}'s elements onto the end of this list.
   */
  public void append(FlexList<E> other) {
    splice(size, 0, other);
  }

  /**
   * Append an element to the end of the list, used in concert
   * with {@link #pop()} when using this list as a stack.
   */
  public void push(E elem) {
    append(elem);
  }

  /**
   * Pop an element off the end of the list, or raise an exception
   * if the list is empty.
   *
   * @throws  NoSuchElementException
   */
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

  /**
   * Splice all elements from {@code other} into the list.
   *
   * See {@link #splice(int, int, Object[], int)}
   */
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

    ensureCapacity(newSize);
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
  public int hashCode() {
    throw new UnsupportedOperationException("FlexList instances are not hashable.");
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

  /**
   * Grows the internal array if it is smaller than the {@code minCapacity}.
   */
  private void ensureCapacity(int minCapacity) {
    if (minCapacity - elems.length > 0) {
      grow(minCapacity);
    }
  }

  /**
   * Grow the array to the new {@code capacity}.
   */
  private void grow(int capacity) {
    elems = Arrays.copyOf(elems, capacity + (capacity >> 1));
  }

}
