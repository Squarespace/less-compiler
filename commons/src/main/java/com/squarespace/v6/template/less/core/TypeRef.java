package com.squarespace.v6.template.less.core;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;


public class TypeRef<T> implements Comparable<TypeRef<T>> {

  private final Type type;
  
  public TypeRef() {
    Type superType = getClass().getGenericSuperclass();
    type = ((ParameterizedType)superType).getActualTypeArguments()[0];
  }

  public Type type() {
    return type;
  }
  
  @Override
  public int compareTo(TypeRef<T> o) {
    return 0;
  }

}
