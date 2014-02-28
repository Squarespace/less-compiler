package com.squarespace.less.exec;


public interface Registry<V> {

  void registerTo(SymbolTable<V> table);

}
