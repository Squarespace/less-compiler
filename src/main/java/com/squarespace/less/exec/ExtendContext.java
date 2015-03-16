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

package com.squarespace.less.exec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.squarespace.less.core.HashPrefixTree;
import com.squarespace.less.core.HashPrefixTree.HPTKeyComparator;
import com.squarespace.less.core.HashPrefixTree.HPTMatch;
import com.squarespace.less.model.Combinator;
import com.squarespace.less.model.Element;
import com.squarespace.less.model.Extend;
import com.squarespace.less.model.ExtendList;
import com.squarespace.less.model.Selector;
import com.squarespace.less.model.Selectors;


/**
 * Indexes all extend lists and performs extend resolution and
 * selector search and replace.  Use of the {@link HashPrefixTree}
 * has the advantage of reducing the number of overall comparisons for
 * selectors which have common prefixes and overlaps.
 */
public class ExtendContext {

  /**
   * When matching the selector's first element's combinator we treat null
   * and DESC as equivalent.
   */
  private final static HPTKeyComparator<Element> MAIN_COMPARATOR = new HPTKeyComparator<Element>() {
    @Override
    public boolean keysEqual(int queryKeyIndex, Element queryKey, Element treeKey) {
      if (queryKeyIndex == 0) {
        // Ignore the prefix null/DESC combinator.
        Combinator queryCombinator = queryKey.combinator();
        Combinator treeCombinator = treeKey.combinator();
        if ((queryCombinator == null || queryCombinator == Combinator.DESC)
            && (treeCombinator == null || treeCombinator == Combinator.DESC)) {
          return queryKey.equalsIgnoreCombinator(treeKey);
        }
      }
      return queryKey.hashCode() == treeKey.hashCode() && queryKey.equals(treeKey);
    }
  };

  /**
   * When performing a partial match, if the query selector's first element's
   * combinator is null or DESC we ignore the tree's combinator.
   */
  private final static HPTKeyComparator<Element> PARTIAL_COMPARATOR = new HPTKeyComparator<Element>() {
    @Override
    public boolean keysEqual(int queryKeyIndex, Element queryKey, Element treeKey) {
      if (queryKeyIndex == 0) {
        // Ignore the tree key's null/DESC combinator.
        Combinator treeCombinator = treeKey.combinator();
        if (treeCombinator == null || treeCombinator == Combinator.DESC) {
          return queryKey.equalsIgnoreCombinator(treeKey);
        }
      }
      return queryKey.hashCode() == treeKey.hashCode() && queryKey.equals(treeKey);
    }
  };

  /**
   * Index of full match extend lists. The HPT's storage is initialized on
   * demand, to minimize overhead for Media blocks that contain no extended
   * selector groups.
   */
  private final HashPrefixTree<Element, Selector> mainIndex = new HashPrefixTree<>(MAIN_COMPARATOR);

  /**
   * Index of search/replace extend lists. The HPT's storage is initialized on
   * demand, to minimize overhead for Media blocks that contain no extended
   * selector groups.
   */
  private final HashPrefixTree<Element, Selector> partialIndex = new HashPrefixTree<>(PARTIAL_COMPARATOR);

  /**
   * Index the selector by its extend list.
   */
  public void index(Selector selector) {
    for (Extend extend : selector.extendList().values()) {
      insert(selector, extend);
    }
  }

  /**
   * Index the selectors by the given extend list.
   */
  public void index(Selectors selectors, ExtendList extendList) {
    for (Selector selector : selectors.selectors()) {
      for (Extend extend : extendList.values()) {
        insert(selector, extend);
      }
    }
  }

  /**
   * Extend the selector group with new selectors, by performing two
   * different searches:
   *
   *  - match each full selector against the main extend index.
   *  - match all subsequences of the full selector against the partial
   *    index and perform search/replace.
   */
  public List<Selector> extend(Selectors selectors, List<Selector> collector) {
    for (Selector selector : selectors.selectors()) {
      List<Selector> matches = resolve(selector);
      if (matches != null && !matches.isEmpty()) {
        if (collector == null) {
          collector = new ArrayList<>();
        }
        collector.addAll(matches);
      }

      List<Selector> partial = searchReplace(selector);
      if (partial != null && !partial.isEmpty()) {
        if (collector == null) {
          collector = new ArrayList<>();
        }
        collector.addAll(partial);
      }
    }
    return collector;
  }

  /**
   * Perform full selector matching against the index. Returns a list of
   * selectors to append.
   */
  public List<Selector> resolve(Selector selector) {
    List<Selector> result = mainIndex.search(selector.elements());
    return (result == null) ? Collections.<Selector>emptyList() : result;
  }

  /**
   * Perform search and replace on the given selector against the index.
   * Returns a list of selectors to append.
   */
  public List<Selector> searchReplace(Selector selector) {
    List<Element> elements = selector.elements();
    List<HPTMatch<Selector>> matches = partialIndex.searchSubsequences(elements);
    if (matches == null) {
      return Collections.emptyList();
    }

    // Based on the (start, end) range of each match found, we build a new
    // selector, replacing the match-covered range with the match values.
    //
    // Selector: ["a", "> b", "> c", "d", "e"]
    //    Match: start=1, end=3, values=[ ["x", "+ y", "> z"] ]
    //   Result: ["a", "> x", "+ y", "> z", "d", "e"]

    int size = elements.size();
    List<Selector> result = new ArrayList<>();
    for (HPTMatch<Selector> match : matches) {
      int start = match.start();
      int end = match.end();
      for (Selector replacement : match.values()) {

        // Build a new selector.
        Selector build = new Selector();
        for (int i = 0; i < size; i++) {
          if (i == start) {
            // When we reach the point where we need to search/replace,
            // append the match's replacement elements.

            // If the first element of the replacement has a combinator of
            // null/DESC, we use the combinator on the first element we're replacing.

            List<Element> replacements = replacement.elements();
            int j = 0;
            Combinator origCombinator = elements.get(i).combinator();
            if (origCombinator != Combinator.DESC) {
              Element first = replacements.get(0).copy(origCombinator);
              build.add(first);
              j++;
            }
            for (; j < replacements.size(); j++) {
              build.add(replacements.get(j));
            }
            continue;
          }

          // If we're in a segment not covered by the match, emit the original
          // selectors elements.
          if (i < start || i >= end) {
            build.add(elements.get(i));
          }
        }
        result.add(build);
      }
    }
    return result;
  }

  /**
   * Inserts the given selector into the index using the extend's selector
   * as the key.  The index is selected based on whether this extend
   * is "all" or normal.
   */
  private void insert(Selector selector, Extend extend) {
    HashPrefixTree<Element, Selector> index = mainIndex;
    if (extend.matchAll()) {
      index = partialIndex;
    }
    index.insert(extend.selector().elements()).append(selector);
  }

}
