package com.squarespace.v6.template.less.exec;


public interface Registry<V> {

  public void registerTo(SymbolTable<V> table);

}
