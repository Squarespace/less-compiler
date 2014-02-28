package com.squarespace.less.parse;

import java.util.ArrayList;
import java.util.List;


public class ParseletBuilder {

  private static final Parselet[] EMPTY = new Parselet[] { };
  
  private List<Parselet> chain = new ArrayList<>();
  
  public ParseletBuilder add(Parselet ... parselets) {
    for (Parselet parselet : parselets) {
      chain.add(parselet);
    }
    return this;
  }
  
  public Parselet[] build() {
    return chain.toArray(EMPTY);
  }
  
}
