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
import com.squarespace.less.model.Extend;
import com.squarespace.less.model.ExtendList;
import com.squarespace.less.model.Selector;
import com.squarespace.less.model.SelectorPart;
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
  private final static HPTKeyComparator<SelectorPart> COMPARATOR = new HPTKeyComparator<SelectorPart>() {
    @Override
    public boolean keysEqual(int queryKeyIndex, SelectorPart queryKey, SelectorPart treeKey) {
      return queryKey.hashCode() == treeKey.hashCode() && queryKey.equals(treeKey);
    }
  };

  /**
   * Index of full match extend lists. The HPT's storage is initialized on
   * demand, to minimize overhead for Media blocks that contain no extended
   * selector groups.
   */
  private final HashPrefixTree<SelectorPart, Selector> mainIndex = new HashPrefixTree<>(COMPARATOR);

  /**
   * Index of search/replace extend lists. The HPT's storage is initialized on
   * demand, to minimize overhead for Media blocks that contain no extended
   * selector groups.
   */
  private final HashPrefixTree<SelectorPart, Selector> partialIndex = new HashPrefixTree<>(COMPARATOR);

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
    List<Selector> result = mainIndex.search(selector.parts());
    return (result == null) ? Collections.<Selector>emptyList() : result;
  }

  /**
   * Perform search and replace on the given selector against the index.
   * Returns a list of selectors to append.
   */
  public List<Selector> searchReplace(Selector selector) {
    List<SelectorPart> elements = selector.parts();
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
            for (SelectorPart part : replacement.parts()) {
              build.add(part);
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
    HashPrefixTree<SelectorPart, Selector> index = mainIndex;
    if (extend.matchAll()) {
      index = partialIndex;
    }
    index.insert(extend.selector().parts()).append(selector);
  }

}
