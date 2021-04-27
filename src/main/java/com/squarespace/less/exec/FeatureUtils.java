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

import java.util.Arrays;
import java.util.List;

import com.squarespace.less.core.CartesianProduct;
import com.squarespace.less.model.Expression;
import com.squarespace.less.model.Features;
import com.squarespace.less.model.Keyword;
import com.squarespace.less.model.Node;


/**
 * Utility methods for working with media {@link Features}.
 */
public class FeatureUtils {

  private FeatureUtils() {
  }

  /**
   * Combines a list of features with its ancestors using a cartesian product.
   *
   * Examples:
   *  IN: @media a and b { @media c {
   * OUT: @media a and c, b and c {
   *
   *  IN: @media a, b { @media c, d {
   * OUT: @media a and c, b and c, a and d, b and d {
   */
  public static Features combine(Features ancestors, Features current) {
    Features result = new Features();
    List<Node> features = current.features();
    int size = features.size();
    for (int i = 0; i < size; i++) {
      Node node = features.get(i);
      List<List<Node>> inputs = Arrays.asList(ancestors.features(), Arrays.asList(node));
      CartesianProduct<Node> product = new CartesianProduct<>(inputs);
      while (product.hasNext()) {
        Expression expn = new Expression();
        List<Node> nodes = product.next();
        int jsize = nodes.size();
        for (int j = 0; j < jsize; j++) {
          if (j > 0) {
            expn.add(new Keyword("and"));
          }
          expn.add(nodes.get(j));
        }

        result.add(expn);
      }
    }
    return result;
  }

}
