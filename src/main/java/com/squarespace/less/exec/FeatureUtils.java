package com.squarespace.less.exec;

import java.util.Arrays;
import java.util.List;

import com.squarespace.less.core.CartesianProduct;
import com.squarespace.less.model.Expression;
import com.squarespace.less.model.Features;
import com.squarespace.less.model.Keyword;
import com.squarespace.less.model.Node;


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
    for (Node node : current.features()) {
      List<List<Node>> inputs = Arrays.asList(ancestors.features(), Arrays.asList(node));
      CartesianProduct<Node> product = new CartesianProduct<>(inputs);
      while (product.hasNext()) {
        Expression expn = new Expression();
        List<Node> nodes = product.next();
        int size = nodes.size();
        for (int i = 0; i < size; i++) {
          if (i > 0) {
            expn.add(new Keyword("and"));
          }
          expn.add(nodes.get(i));
        }
        
        result.add(expn);
      }
    }
    return result;
  }
    
}
