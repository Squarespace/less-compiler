package com.squarespace.less.exec;

import com.squarespace.less.core.TypeRef;


/**
 * Lookup table, holds registered functions by name.
 */
public class FunctionTable extends SymbolTable<Function> {

  private static final int NUM_BUCKETS = 64;
  
  private static final TypeRef<Function> TYPE_REF = new TypeRef<Function>() { };

  public FunctionTable() {
    super(TYPE_REF, NUM_BUCKETS);
  }
  
  public FunctionTable(int numBuckets) {
    super(TYPE_REF, numBuckets);
  }
  
  @Override
  public void registerSymbol(Object impl) {
    Function func = (Function)impl;
    put(func.name(), func);
  }

}
