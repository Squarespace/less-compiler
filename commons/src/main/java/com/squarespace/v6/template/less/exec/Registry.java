package com.squarespace.v6.template.less.exec;


public interface Registry<V> {

  void registerTo(SymbolTable<V> table);

}
