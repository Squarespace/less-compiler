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

package com.squarespace.less.exec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.squarespace.less.LessContext;
import com.squarespace.less.core.CartesianProduct;
import com.squarespace.less.core.LessUtils;
import com.squarespace.less.model.Element;
import com.squarespace.less.model.Mixin;
import com.squarespace.less.model.Selector;
import com.squarespace.less.model.Selectors;
import com.squarespace.less.model.TextElement;
import com.squarespace.less.model.ValueElement;


/**
 * Utility methods for manipulating {@link Selectors}.
 */
public class SelectorUtils {

  private SelectorUtils() {
  }

  /**
   * Given a list of ancestor selectors and a list of current selectors, combine them into a
   * single merged selector list.  The merge strategy is based on the way LESS language does
   * nested selector combination:
   *
   * 1. If a selector contains no wildcards, it is appended to each of the ancestors.
   * 2. Otherwise, we need to replace each wildcard element in the selector with
   *    the list of ancestors, and then return the cartesian product.
   */
  public static Selectors combine(Selectors ancestors, Selectors current) {
    Selectors result = new Selectors();
    for (Selector selector : current.selectors()) {

      // When no wildcard is present, the selector is prepended to the ancestors.
      if (!selector.hasWildcard()) {
        List<List<Selector>> inputs = new ArrayList<>(2);
        inputs.add(ancestors.selectors());
        inputs.add(Arrays.asList(selector));
        SelectorUtils.flatten(inputs, result);
        continue;
      }

      // Otherwise, substitute the ancestors after each wildcard element found.
      List<List<Selector>> inputs = new ArrayList<>();
      Selector temp = new Selector();
      for (Element elem : selector.elements()) {

        if (elem.isWildcard()) {
          // Only expand wildcards when there is at least 1 ancestor selector
          if (!ancestors.selectors().isEmpty()) {
            temp.add(elem);
            inputs.add(Arrays.asList(temp));
            inputs.add(ancestors.selectors());
            temp = new Selector();
          }

        } else {
          temp.add(elem);
        }
      }

      if (!temp.isEmpty()) {
        inputs.add(Arrays.asList(temp));
      }

      SelectorUtils.flatten(inputs, result);
    }
    return result;
  }

  /**
   * Generates a cartesian product from {@code selectors} and appends the flattened
   * selectors {@code result}.
   */
  public static void flatten(List<List<Selector>> selectors, Selectors result) {
    CartesianProduct<Selector> product = new CartesianProduct<>(selectors);
    while (product.hasNext()) {
      Selector flat = new Selector();
      for (Selector tmp : product.next()) {
        for (Element elem : tmp.elements()) {
          flat.add(elem);
        }
      }
      result.add(flat);
    }
  }

  /**
   * Constructs a list of strings from a selector, to enable simpler {@link Mixin} matching.
   */
  public static List<String> renderSelector(Selector selector) {
    return renderSelector(selector, null);
  }

  /**
   * Constructs a list of strings from a selector, to enable simpler {@link Mixin} matching.
   */
  public static List<String> renderSelector(Selector selector, LessContext context) {
    List<Element> elements = selector.elements();
    if (elements.isEmpty()) {
      return null;
    }

    // Scan the selector's elements to produce a mixin-friendly path.
    List<String> result = null;
    int size = elements.size();
    for (int i = 0; i < size; i++) {
      Element elem = elements.get(i);
      if (elem.isWildcard()) {
        if (i == 0) {
          continue;
        }
        return null;
      }

      // If the context is null we can't render any nodes, so we can only
      // build paths for text elements.
      if (context == null && !(elem instanceof TextElement)) {
        return null;
      }

      // We can render either of these elements
      boolean valid = (elem instanceof TextElement) || (elem instanceof ValueElement);
      if (!valid) {
        return null;
      }

      // Defer allocation as long as possible
      if (result == null) {
        result = LessUtils.initList(result, size);
      }
      if (elem instanceof TextElement) {
        result.add(((TextElement)elem).name());

      } else if (elem instanceof ValueElement) {
        String text = context.render(((ValueElement)elem).value());
        result.add(text);
      }

    }
    return result;
  }

}
