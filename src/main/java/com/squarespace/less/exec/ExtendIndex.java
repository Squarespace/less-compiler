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
import java.util.List;
import java.util.Set;

import com.squarespace.less.core.HashPrefixTree;
import com.squarespace.less.core.HashPrefixTree.HPTKeyComparator;
import com.squarespace.less.core.HashPrefixTree.HPTMatch;
import com.squarespace.less.core.HashPrefixTree.HPTNode;
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
 *
 * Each extend expression consists of a replacement selector and one or
 * more target selectors.
 */
public class ExtendIndex {

  /**
   * Compares the hashCode before the key part, to fail faster on average.
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
  private final HashPrefixTree<SelectorPart, Selector> exactTree = new HashPrefixTree<>(COMPARATOR);

  /**
   * Index of search/replace extend lists. The HPT's storage is initialized on
   * demand, to minimize overhead for Media blocks that contain no extended
   * selector groups.
   */
  private final HashPrefixTree<SelectorPart, Selector> partialTree = new HashPrefixTree<>(COMPARATOR);

  /**
   * Inverted index of extends by their target selector. This is used for extends
   * whose target selectors are other extends. We use this to resolve them against
   * one another after indexing is complete.
   */
  private final HashPrefixTree<SelectorPart, Extend> inverseTree = new HashPrefixTree<>(COMPARATOR);

  /**
   * Capture all extend lists found, so we can check if they extend other extends
   * post-indexing.
   */
  private final List<CapturedExtend> capturedExtends = new ArrayList<>();

  /**
   * Index the selector by its extend list. This is used to process an
   * immediate extend expression.
   */
  public void index(Selector selector) {
    ExtendList extendList = selector.extendList();
    capturedExtends.add(new CapturedExtend(extendList, selector));
    for (Extend extend : extendList.values()) {
      insert(selector, extend);
    }
  }

  /**
   * Index the selectors by the given extend list. This is used to process
   * a rule-level extend expression.
   */
  public void index(Selectors selectors, ExtendList extendList) {
    for (Selector selector : selectors.selectors()) {
      capturedExtends.add(new CapturedExtend(extendList, selector));
      for (Extend extend : extendList.values()) {
        insert(selector, extend);
      }
    }
  }

  /**
   * Return the list of captured extends index by this instance.
   */
  public List<CapturedExtend> capturedExtends() {
    return capturedExtends;
  }

  /**
   * Resolve the captured extends against this index.
   */
  public void resolveSelfExtends() {
    resolveSelfExtends(capturedExtends);
  }

  /**
   * This can be slightly confusing, but an extend can match another extend selector. Since
   * we have an inverted index of all extends, we can resolve them against each other in
   * advance.
   *
   * This method is called whenever we exit a given scope, having completed indexing
   * all extend expressions within that scope.
   *
   * Example:
   *
   *   .a .b .c { content: 1 }
   *   .x:extend(.b .c all) {}
   *   .replace:extend(.x);
   *
   * In the above, whatever (.x) matches .replace should too. However, .x is a partial
   * match, so it will produce (.a .b .c) which (.replace) will fail to match unless
   * we perform this self extends resolution.
   */
  public void resolveSelfExtends(List<CapturedExtend> captured) {
    for (CapturedExtend pair : captured) {
      for (Extend extend : pair.extendList.values()) {
        if (extend.matchAll()) {
          continue;
        }

        // Match the extend's target against the inverse index.
        HPTNode<SelectorPart, Extend> hit = inverseTree.search(extend.targetSelector().parts());
        if (hit == null) {
          continue;
        }

        // We found one or more matching extends, so add to the index.
        for (Extend match : hit.values()) {
          if (match.matchAll()) {
            partialTree.insert(match.targetSelector().parts()).append(pair.selector);
          } else {
            exactTree.insert(match.targetSelector().parts()).append(pair.selector);
          }
        }
      }
    }
  }

  /**
   * Perform an exact match against the extend index, using the given
   * selector as the query.
   */
  public HPTNode<SelectorPart, Selector> findExactMatch(Selector selector) {
    return exactTree.search(selector.parts());
  }

  /**
   * Perform a partial match against the extend index, using the given
   * selector as the query. Also pass in a duplicate filter to avoid
   * pulling in results we fetched on a previous query.
   */
  public List<HPTMatch<Selector>> findPartialMatch(Selector selector, Set<Integer> dupeFilter) {
    return partialTree.searchSubsequences(selector.parts(), dupeFilter);
  }

  /**
   * Inserts the given selector into the index using the extend's selector
   * as the key.  The index is selected based on whether this extend
   * is "all" or normal.
   */
  private void insert(Selector selector, Extend extend) {
    HashPrefixTree<SelectorPart, Selector> index = extend.matchAll() ? partialTree : exactTree;
    index.insert(extend.targetSelector().parts()).append(selector);
    inverseTree.insert(selector.parts()).append(extend);
  }

  public static class CapturedExtend {

    private final ExtendList extendList;

    private final Selector selector;

    public CapturedExtend(ExtendList extendList, Selector selector) {
      this.extendList = extendList;
      this.selector = selector;
    }

  }
}
