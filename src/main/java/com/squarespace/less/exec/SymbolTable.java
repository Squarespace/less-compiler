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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import com.squarespace.less.core.LessInternalException;
import com.squarespace.less.core.TypeRef;


/**
 * Generic table mapping symbol strings to values. Raises an error when an attempt
 * is made to register a symbol more than once.
 */
public abstract class SymbolTable<V> {

  private final Map<String, V> table;

  private final TypeRef<V> typeRef;

  private boolean inUse = false;

  public SymbolTable(TypeRef<V> typeRef, int numBuckets) {
    this.table = new HashMap<>(numBuckets);
    this.typeRef = typeRef;
  }

  public void setInUse() {
    this.inUse = true;
  }

  public void register(Registry<V> source) {
    registerClass(source);
  }

  public V get(String symbol) {
    return table.get(symbol);
  }

  protected void put(String symbol, V value) {
    if (inUse) {
      throw new LessInternalException("Attempt to add a symbol after table in use.");
    }
    if (table.containsKey(symbol)) {
      throw new LessInternalException("A symbol named '" + symbol + "' is already registered!");
    }
    table.put(symbol, value);
  }

  public abstract void registerSymbol(Object impl);

  private void registerClass(Registry<V> source) {
    Field[] fields = source.getClass().getDeclaredFields();
    for (Field field : fields) {
      if (!Modifier.isStatic(field.getModifiers())) {
        continue;
      }
      Class<?> type = field.getType();
      if (type.equals(typeRef.type())) {
        field.setAccessible(true);
        try {
          registerSymbol(field.get(source));
        } catch (IllegalAccessException e) {
          throw new LessInternalException("Failed to register source " + source, e);
        }
      }
    }
  }
}
